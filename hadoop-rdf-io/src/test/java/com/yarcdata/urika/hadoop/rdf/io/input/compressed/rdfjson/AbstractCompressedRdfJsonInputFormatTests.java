/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.rdfjson;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.io.input.RdfJsonInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.compressed.AbstractCompressedWholeFileTripleInputFormatTests;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract compressed RDF/JSON input tests
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractCompressedRdfJsonInputFormatTests extends AbstractCompressedWholeFileTripleInputFormatTests {

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
    public AbstractCompressedRdfJsonInputFormatTests(String ext, CompressionCodec codec) {
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
        return Lang.RDFJSON;
    }

    @Override
    protected final InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new RdfJsonInputFormat();
    }

}