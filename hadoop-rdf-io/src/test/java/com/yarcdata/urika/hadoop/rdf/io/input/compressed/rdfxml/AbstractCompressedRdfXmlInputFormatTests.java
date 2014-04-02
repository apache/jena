/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.rdfxml;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.io.input.RdfXmlInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.compressed.AbstractCompressedWholeFileTripleInputFormatTests;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract compressed RDF/XML input tests
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractCompressedRdfXmlInputFormatTests extends
 AbstractCompressedWholeFileTripleInputFormatTests {

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
    public AbstractCompressedRdfXmlInputFormatTests(String ext, CompressionCodec codec) {
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
        return Lang.RDFXML;
    }

    @Override
    protected final InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new RdfXmlInputFormat();
    }

}