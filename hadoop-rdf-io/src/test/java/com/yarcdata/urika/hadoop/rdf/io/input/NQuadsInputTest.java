/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.yarcdata.urika.hadoop.rdf.io.input;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;

import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for the NQuads input format
 * @author rvesse
 *
 */
public class NQuadsInputTest extends AbstractQuadsInputFormatTests {

    @Override
    protected InputFormat<LongWritable, QuadWritable> getInputFormat() {
        return new NQuadsInputFormat();
    }

    @Override
    protected String getFileExtension() {
        return ".nq";
    }

}
