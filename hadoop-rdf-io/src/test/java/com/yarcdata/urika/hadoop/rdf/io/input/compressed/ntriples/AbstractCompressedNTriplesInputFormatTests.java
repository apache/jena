/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.InputFormat;

import com.yarcdata.urika.hadoop.rdf.io.input.NTriplesInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.compressed.AbstractCompressedTriplesInputFormatTests;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract compressed NTriples input tests
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractCompressedNTriplesInputFormatTests extends AbstractCompressedTriplesInputFormatTests {

    private String ext;
    private CompressionCodec codec;

    /**
     * Creates new tests
     * 
     * @param ext
     *            File extension
     * @param codec
     *            Compression codec
     */
    public AbstractCompressedNTriplesInputFormatTests(String ext, CompressionCodec codec) {
        this.ext = ext;
        this.codec = codec;
    }

    @Override
    protected final String getFileExtension() {
        return this.ext;
    }

    @Override
    protected final CompressionCodec getCompressionCodec() {
        return this.codec;
    }

    @Override
    protected final InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new NTriplesInputFormat();
    }

}