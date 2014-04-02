/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for RDF/XML output
 * 
 * @author rvesse
 * 
 */
public class RdfXmlOutputTest extends AbstractTripleOutputFormatTests {

    @Override
    protected String getFileExtension() {
        return ".rdf";
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.RDFXML;
    }

    @Override
    protected OutputFormat<NullWritable, TripleWritable> getOutputFormat() {
        return new RdfXmlOutputFormat<NullWritable>();
    }

}
