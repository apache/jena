/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for RDF/JSON output
 * 
 * @author rvesse
 * 
 */
public class RdfJsonOutputTest extends AbstractTripleOutputFormatTests {

    @Override
    protected String getFileExtension() {
        return ".rj";
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.RDFJSON;
    }

    @Override
    protected OutputFormat<NullWritable, TripleWritable> getOutputFormat() {
        return new RdfJsonOutputFormat<NullWritable>();
    }

}
