/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.rdfxml;

import org.apache.hadoop.io.compress.BZip2Codec;

/**
 * Tests for BZipped RDF/XML input
 * 
 * @author rvesse
 * 
 */
public class BZippedRdfXmlInputTest extends AbstractCompressedRdfXmlInputFormatTests {

    /**
     * Creates new tests
     */
    public BZippedRdfXmlInputTest() {
        super(".rdf.bz2", new BZip2Codec());
    }
}
