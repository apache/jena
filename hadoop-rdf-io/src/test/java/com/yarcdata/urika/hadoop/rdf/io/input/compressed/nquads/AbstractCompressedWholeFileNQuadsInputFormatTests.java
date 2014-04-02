/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.nquads;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.io.input.WholeFileNQuadsInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.compressed.AbstractCompressedWholeFileQuadInputFormatTests;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract compressed whole file NTriples input tests
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractCompressedWholeFileNQuadsInputFormatTests extends
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
    public AbstractCompressedWholeFileNQuadsInputFormatTests(String ext, CompressionCodec codec) {
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
        return Lang.NQUADS;
    }

    @Override
    protected final InputFormat<LongWritable, QuadWritable> getInputFormat() {
        return new WholeFileNQuadsInputFormat();
    }

}