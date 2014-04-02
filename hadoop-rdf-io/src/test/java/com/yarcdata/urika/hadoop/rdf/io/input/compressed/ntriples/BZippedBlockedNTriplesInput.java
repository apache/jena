/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.compress.BZip2Codec;

/**
 * Tests for BZipped blocked NTriples input
 * 
 * @author rvesse
 * 
 */
public class BZippedBlockedNTriplesInput extends AbstractCompressedBlockedNTriplesInputFormatTests {

    /**
     * Creates new tests
     */
    public BZippedBlockedNTriplesInput() {
        super(".nt.bz2", new BZip2Codec());
    }
}
