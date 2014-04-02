/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;

/**
 * An abstract record reader for arbitrary RDF which provides support for
 * selecting the actual record reader to use based on detecting the RDF language
 * from the file name
 * 
 * @author rvesse
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
    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException, InterruptedException {
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
