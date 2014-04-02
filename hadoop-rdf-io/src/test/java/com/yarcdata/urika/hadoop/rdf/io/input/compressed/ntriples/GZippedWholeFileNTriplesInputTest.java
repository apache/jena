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
public class GZippedWholeFileNTriplesInputTest extends AbstractCompressedWholeFileNTriplesInputFormatTests {

    /**
     * Creates new tests
     */
    public GZippedWholeFileNTriplesInputTest() {
        super(".nt.gz", new GzipCodec());
    }

}
