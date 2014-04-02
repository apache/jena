/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import com.yarcdata.urika.hadoop.rdf.io.HadoopIOConstants;
import com.yarcdata.urika.hadoop.rdf.io.input.AbstractNodeTupleInputFormatTests;
import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;

/**
 * @author rvesse
 * 
 * @param <TValue>
 * @param <T>
 */
public abstract class AbstractCompressedNodeTupleInputFormatTests<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractNodeTupleInputFormatTests<TValue, T> {

    @Override
    protected Configuration prepareConfiguration() {
        Configuration config = super.prepareConfiguration();
        config.set(HadoopIOConstants.IO_COMPRESSION_CODECS, this.getCompressionCodec().getClass().getCanonicalName());
        return config;
    }

    @Override
    protected Writer getWriter(File f) throws IOException {
        CompressionCodec codec = this.getCompressionCodec();
        if (codec instanceof Configurable) {
            ((Configurable) codec).setConf(this.prepareConfiguration());
        }
        FileOutputStream fileOutput = new FileOutputStream(f, false);
        OutputStream output = codec.createOutputStream(fileOutput);
        return new OutputStreamWriter(output);
    }

    /**
     * Gets the compression codec to use
     * 
     * @return Compression codec
     */
    protected abstract CompressionCodec getCompressionCodec();

    /**
     * Indicates whether inputs can be split, defaults to false for compressed
     * input tests
     */
    @Override
    protected boolean canSplitInputs() {
        return false;
    }
}
