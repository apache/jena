/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.turtle;

import org.apache.hadoop.io.compress.GzipCodec;

/**
 * Tests for GZipped NTriples input
 * 
 * @author rvesse
 * 
 */
public class GZippedTurtleInputTest extends AbstractCompressedTurtleInputFormatTests {

    /**
     * Creates new tests
     */
    public GZippedTurtleInputTest() {
        super(".nt.gz", new GzipCodec());
    }
}
