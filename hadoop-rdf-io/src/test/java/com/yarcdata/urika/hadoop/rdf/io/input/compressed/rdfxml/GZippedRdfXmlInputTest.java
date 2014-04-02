/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.rdfxml;

import org.apache.hadoop.io.compress.GzipCodec;

/**
 * Tests for GZipped RDF/XML input
 * 
 * @author rvesse
 * 
 */
public class GZippedRdfXmlInputTest extends AbstractCompressedRdfXmlInputFormatTests {

    /**
     * Creates new tests
     */
    public GZippedRdfXmlInputTest() {
        super(".rdf.gz", new GzipCodec());
    }
}
