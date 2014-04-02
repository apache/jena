/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.rdfjson;

import org.apache.hadoop.io.compress.DefaultCodec;

/**
 * Tests for Deflated RDF/JSON input
 * 
 * @author rvesse
 * 
 */
public class DeflatedRdfJsonInputTest extends AbstractCompressedRdfJsonInputFormatTests {

    /**
     * Creates new tests
     */
    public DeflatedRdfJsonInputTest() {
        super(".rj.deflate", new DefaultCodec());
    }
}
