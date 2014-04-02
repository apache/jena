/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.compress.GzipCodec;

/**
 * Tests for GZipped blocked NTriples input
 * 
 * @author rvesse
 * 
 */
public class GZippedBlockedNTriplesInput extends AbstractCompressedBlockedNTriplesInputFormatTests {

    /**
     * Creates new tests
     */
    public GZippedBlockedNTriplesInput() {
        super(".nt.gz", new GzipCodec());
    }
}
