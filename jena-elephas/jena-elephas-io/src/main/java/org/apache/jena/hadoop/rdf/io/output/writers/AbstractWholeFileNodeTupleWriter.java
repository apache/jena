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

package org.apache.jena.hadoop.rdf.io.output.writers;

import java.io.IOException;
import java.io.Writer;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract implementation of a record writer that writes records to whole
 * file formats.
 * <p>
 * It is important to note that the writer does not actually write any output
 * until the {@link #close(TaskAttemptContext)} method is called as it must
 * write the entire output in one go otherwise the output would be invalid. Also
 * writing in one go increases the chances that the writer will be able to
 * effectively use the syntax compressions of the RDF serialization being used.
 * </p>
 * <p>
 * The implementation only writes the value portion of the key value pair since
 * it is the value portion that is used to convey the node tuples
 * </p>
 * 
 * 
 * 
 * @param <TKey>
 * @param <TValue>
 * @param <T>
 */
public abstract class AbstractWholeFileNodeTupleWriter<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        RecordWriter<TKey, T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractWholeFileNodeTupleWriter.class);

    private Writer writer;

    protected AbstractWholeFileNodeTupleWriter(Writer writer) {
        if (writer == null)
            throw new NullPointerException("writer cannot be null");
        this.writer = writer;
    }

    @Override
    public final void write(TKey key, T value) {
        LOG.debug("write({}={})", key, value);
        this.add(value);
    }

    /**
     * Adds the tuple to the cache of tuples that will be written when the
     * {@link #close(TaskAttemptContext)} method is called
     * 
     * @param value
     */
    protected abstract void add(T value);

    @Override
    public void close(TaskAttemptContext context) throws IOException {
        if (this.writer != null) {
            this.writeOutput(writer);
            this.writer.close();
            this.writer = null;
        }
    }

    /**
     * Writes the cached tuples to the writer, the writer should not be closed
     * by this method implementation
     * 
     * @param writer
     *            Writer
     */
    protected abstract void writeOutput(Writer writer);

}
