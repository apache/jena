/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for NTriples output format
 * 
 * @author rvesse
 * 
 */
public class NTriplesOutputTest extends AbstractTripleOutputFormatTests {

    @Override
    protected String getFileExtension() {
        return ".nt";
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.NTRIPLES;
    }

    @Override
    protected OutputFormat<NullWritable, TripleWritable> getOutputFormat() {
        return new NTriplesOutputFormat<NullWritable>();
    }

}
