/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.rdfjson;

import org.apache.hadoop.io.compress.GzipCodec;

/**
 * Tests for GZipped RDF/JSON input
 * 
 * @author rvesse
 * 
 */
public class GZippedRdfJsonInputTest extends AbstractCompressedRdfJsonInputFormatTests {

    /**
     * Creates new tests
     */
    public GZippedRdfJsonInputTest() {
        super(".rj.gz", new GzipCodec());
    }
}
