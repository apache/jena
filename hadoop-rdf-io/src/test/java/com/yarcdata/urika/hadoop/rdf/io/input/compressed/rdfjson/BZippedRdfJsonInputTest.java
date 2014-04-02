/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.rdfjson;

import org.apache.hadoop.io.compress.BZip2Codec;

/**
 * Tests for BZipped RDF/JSON input
 * 
 * @author rvesse
 * 
 */
public class BZippedRdfJsonInputTest extends AbstractCompressedRdfJsonInputFormatTests {

    /**
     * Creates new tests
     */
    public BZippedRdfJsonInputTest() {
        super(".rj.bz2", new BZip2Codec());
    }
}
