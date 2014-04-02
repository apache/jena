/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.IOException;
import java.io.Writer;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;

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
 * @author rvesse
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
    public final void write(TKey key, T value) throws IOException, InterruptedException {
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
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
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
