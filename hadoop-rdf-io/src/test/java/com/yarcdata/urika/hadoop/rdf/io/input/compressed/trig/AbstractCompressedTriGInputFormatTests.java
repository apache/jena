/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.trig;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.io.input.TriGInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.compressed.AbstractCompressedWholeFileQuadInputFormatTests;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract compressed Turtle input tests
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractCompressedTriGInputFormatTests extends
        AbstractCompressedWholeFileQuadInputFormatTests {

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
    public AbstractCompressedTriGInputFormatTests(String ext, CompressionCodec codec) {
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
    protected final Lang getRdfLanguage() {
        return Lang.TRIG;
    }

    @Override
    protected final InputFormat<LongWritable, QuadWritable> getInputFormat() {
        return new TriGInputFormat();
    }

}