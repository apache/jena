/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for NQuads input
 * 
 * @author rvesse
 * 
 */
public class WholeFileNQuadsInputTest extends AbstractWholeFileQuadInputFormatTests {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.NQUADS;
    }

    @Override
    protected String getFileExtension() {
        return ".nq";
    }

    @Override
    protected InputFormat<LongWritable, QuadWritable> getInputFormat() {
        return new WholeFileNQuadsInputFormat();
    }

}
