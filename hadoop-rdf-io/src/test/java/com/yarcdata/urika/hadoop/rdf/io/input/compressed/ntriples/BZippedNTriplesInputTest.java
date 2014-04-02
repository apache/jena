/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.compress.BZip2Codec;


/**
 * Tests for BZipped NTriples input
 * 
 * @author rvesse
 * 
 */
public class BZippedNTriplesInputTest extends AbstractCompressedNTriplesInputFormatTests {

    /**
     * Creates new tests
     */
    public BZippedNTriplesInputTest() {
        super(".nt.bz2", new BZip2Codec());
    }
}
