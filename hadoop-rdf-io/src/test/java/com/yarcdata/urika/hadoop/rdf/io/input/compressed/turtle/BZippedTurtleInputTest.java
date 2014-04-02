/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.turtle;

import org.apache.hadoop.io.compress.BZip2Codec;

/**
 * Tests for BZipped NTriples input
 * 
 * @author rvesse
 * 
 */
public class BZippedTurtleInputTest extends AbstractCompressedTurtleInputFormatTests {

    /**
     * Creates new tests
     */
    public BZippedTurtleInputTest() {
        super(".nt.bz2", new BZip2Codec());
    }
}
