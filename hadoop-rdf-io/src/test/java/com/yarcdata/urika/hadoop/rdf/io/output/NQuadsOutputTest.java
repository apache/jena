/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for NQuads output format
 * 
 * @author rvesse
 * 
 */
public class NQuadsOutputTest extends AbstractQuadOutputFormatTests {

    @Override
    protected String getFileExtension() {
        return ".nq";
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.NQUADS;
    }

    @Override
    protected OutputFormat<NullWritable, QuadWritable> getOutputFormat() {
        return new NQuadsOutputFormat<NullWritable>();
    }

}
