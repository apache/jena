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

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * Abstract output format which takes pairs with Node keys and arbitrary values
 * and writes them as a simple line based text file
 * 
 * @author rvesse
 * 
 * @param <TValue> Value type
 */
public abstract class AbstractNodeOutputFormat<TValue> extends FileOutputFormat<NodeWritable, TValue> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeOutputFormat.class);

    @Override
    public RecordWriter<NodeWritable, TValue> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException {
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
    protected String getFileExtension() {
        return ".nodes";
    }

    /**
     * Gets the record writer to use
     * 
     * @param writer
     *            Writer to write output to
     * @param config
     *            Configuration
     * @return Record writer
     */
    protected abstract RecordWriter<NodeWritable, TValue> getRecordWriter(Writer writer, Configuration config);
}
