/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.compress.GzipCodec;




/**
 * Tests for GZipped NTriples input
 * 
 * @author rvesse
 * 
 */
public class GZippedNTriplesInputTest extends AbstractCompressedNTriplesInputFormatTests {

    /**
     * Creates new tests
     */
    public GZippedNTriplesInputTest() {
        super(".nt.gz", new GzipCodec());
    }

}
