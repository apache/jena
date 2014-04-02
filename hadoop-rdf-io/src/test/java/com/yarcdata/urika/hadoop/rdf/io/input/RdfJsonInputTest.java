/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for RDF/JSON input
 * 
 * @author rvesse
 * 
 */
public class RdfJsonInputTest extends AbstractWholeFileTripleInputFormatTests {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.RDFJSON;
    }

    @Override
    protected String getFileExtension() {
        return ".rj";
    }

    @Override
    protected InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new RdfJsonInputFormat();
    }

}
