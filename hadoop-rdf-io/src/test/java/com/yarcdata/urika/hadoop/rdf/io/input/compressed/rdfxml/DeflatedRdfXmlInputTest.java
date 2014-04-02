/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.rdfxml;

import org.apache.hadoop.io.compress.DefaultCodec;

/**
 * Tests for Deflated RDF/XML input
 * 
 * @author rvesse
 * 
 */
public class DeflatedRdfXmlInputTest extends AbstractCompressedRdfXmlInputFormatTests {

    /**
     * Creates new tests
     */
    public DeflatedRdfXmlInputTest() {
        super(".rdf.deflate", new DefaultCodec());
    }
}
