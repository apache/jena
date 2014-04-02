/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;

/**
 * An abstract implementation of an output format for line based tuple formats
 * where the key is ignored and only the tuple values will be output
 * 
 * @author rvesse
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Tuple value type
 * @param <T>
 *            Writable node tuple type
 * 
 */
public abstract class AbstractNodeTupleOutputFormat<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        FileOutputFormat<TKey, T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeTupleOutputFormat.class);

    @Override
    public RecordWriter<TKey, T> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException {
        Configuration config = context.getConfiguration();
        boolean isCompressed = getCompressOutput(context);
        CompressionCodec codec = null;
        String extension = this.getFileExtension();
        if (isCompressed) {
            Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(context, GzipCodec.class);
            codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, config);
            extension += codec.getDefaultExtension();
        }
        Path file = getDefaultWorkFile(context, extension);
        LOG.info("Writing output to file " + file);
        FileSystem fs = file.getFileSystem(config);
        if (!isCompressed) {
            FSDataOutputStream fileOut = fs.create(file, false);
            return this.getRecordWriter(new OutputStreamWriter(fileOut), config);
        } else {
            FSDataOutputStream fileOut = fs.create(file, false);
            return this.getRecordWriter(new OutputStreamWriter(codec.createOutputStream(fileOut)), config);
        }
    }

    /**
     * Gets the file extension to use for output
     * 
     * @return File extension including the '.'
     */
    protected abstract String getFileExtension();

    /**
     * Gets the record writer to use
     * 
     * @param writer
     *            Writer to write output to
     * @param config
     *            Configuration
     * @return Record writer
     */
    protected abstract RecordWriter<TKey, T> getRecordWriter(Writer writer, Configuration config);

}
