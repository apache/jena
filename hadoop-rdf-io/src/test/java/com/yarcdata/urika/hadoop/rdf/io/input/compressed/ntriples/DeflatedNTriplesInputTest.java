/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.compress.DefaultCodec;


/**
 * Tests for deflated NTriples input
 * 
 * @author rvesse
 * 
 */
public class DeflatedNTriplesInputTest extends AbstractCompressedNTriplesInputFormatTests {

    /**
     * Creates new tests
     */
    public DeflatedNTriplesInputTest() {
        super(".nt.deflate", new DefaultCodec());
    }
}
