/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.compress.DefaultCodec;

/**
 * Tests for deflated blocked NTriples input
 * 
 * @author rvesse
 * 
 */
public class DeflatedBlockedNTriplesInput extends AbstractCompressedBlockedNTriplesInputFormatTests {

    /**
     * Creates new tests
     */
    public DeflatedBlockedNTriplesInput() {
        super(".nt.deflate", new DefaultCodec());
    }
}
