/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract record reader for arbitrary RDF which provides support for
 * selecting the actual record reader to use based on detecting the RDF language
 * from the file name
 * 
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractRdfReader<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        RecordReader<LongWritable, T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRdfReader.class);

    private RecordReader<LongWritable, T> reader;

    @Override
    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException,
            InterruptedException {
        LOG.debug("initialize({}, {})", genericSplit, context);

        // Assuming file split
        if (!(genericSplit instanceof FileSplit))
            throw new IOException("This record reader only supports FileSplit inputs");

        // Find RDF language
        FileSplit split = (FileSplit) genericSplit;
        Path path = split.getPath();
        Lang lang = RDFLanguages.filenameToLang(path.getName());
        if (lang == null)
            throw new IOException("There is no registered RDF language for the input file " + path.toString());

        // Select the record reader and initialize
        this.reader = this.selectRecordReader(lang);
        this.reader.initialize(split, context);
    }

    /**
     * Selects the appropriate record reader to use for the given RDF language
     * 
     * @param lang
     *            RDF language
     * @return Record reader
     * @throws IOException
     *             Should be thrown if no record reader can be selected
     */
    protected abstract RecordReader<LongWritable, T> selectRecordReader(Lang lang) throws IOException;

    @Override
    public final boolean nextKeyValue() throws IOException, InterruptedException {
        return this.reader.nextKeyValue();
    }

    @Override
    public final LongWritable getCurrentKey() throws IOException, InterruptedException {
        return this.reader.getCurrentKey();
    }

    @Override
    public final T getCurrentValue() throws IOException, InterruptedException {
        return this.reader.getCurrentValue();
    }

    @Override
    public final float getProgress() throws IOException, InterruptedException {
        return this.reader.getProgress();
    }

    @Override
    public final void close() throws IOException {
        this.reader.close();
    }

}
