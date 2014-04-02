/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.InputFormat;

import com.yarcdata.urika.hadoop.rdf.io.input.BlockedNTriplesInputFormat;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract compressed blocked NTriples input tests
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractCompressedBlockedNTriplesInputFormatTests extends
        AbstractCompressedWholeFileNTriplesInputFormatTests {

    /**
     * Creates new tests
     * 
     * @param ext
     *            File extension
     * @param codec
     *            Compression codec
     */
    public AbstractCompressedBlockedNTriplesInputFormatTests(String ext, CompressionCodec codec) {
        super(ext, codec);
    }

    @Override
    protected final InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new BlockedNTriplesInputFormat();
    }
}