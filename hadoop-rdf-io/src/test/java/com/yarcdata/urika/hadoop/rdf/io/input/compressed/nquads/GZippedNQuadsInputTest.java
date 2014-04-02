/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.nquads;

import org.apache.hadoop.io.compress.GzipCodec;

/**
 * Tests for GZipped NQuads input
 * 
 * @author rvesse
 * 
 */
public class GZippedNQuadsInputTest extends AbstractCompressedNQuadsInputFormatTests {

    /**
     * Creates new tests
     */
    public GZippedNQuadsInputTest() {
        super(".nq.gz", new GzipCodec());
    }

}
