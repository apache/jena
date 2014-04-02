/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link NTriplesInputFormat}
 * 
 * @author rvesse
 * 
 */
public class NTriplesInputTest extends AbstractTriplesInputFormatTests {

    @Override
    protected InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new NTriplesInputFormat();
    }

    @Override
    protected String getFileExtension() {
        return ".nt";
    }
}
