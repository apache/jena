/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.yarcdata.urika.hadoop.rdf.io.input;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for blocked NTriples input
 * @author rvesse
 *
 */
public class BlockedNTriplesInputTest extends AbstractBlockedTripleInputFormatTests {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.NTRIPLES;
    }

    @Override
    protected String getFileExtension() {
        return ".nt";
    }

    @Override
    protected InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new BlockedNTriplesInputFormat();
    }

}
