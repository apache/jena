/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for turtle input format
 * 
 * @author rvesse
 * 
 */
public class TurtleInputTest extends AbstractWholeFileTripleInputFormatTests {

    @Override
    protected final String getFileExtension() {
        return ".ttl";
    }

    @Override
    protected final Lang getRdfLanguage() {
        return Lang.TURTLE;
    }
    
    @Override
    protected InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new TurtleInputFormat();
    }
}
