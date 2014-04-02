/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.turtle;

import org.apache.hadoop.io.compress.DefaultCodec;

/**
 * Tests for Deflated NTriples input
 * 
 * @author rvesse
 * 
 */
public class DeflatedTurtleInputTest extends AbstractCompressedTurtleInputFormatTests {

    /**
     * Creates new tests
     */
    public DeflatedTurtleInputTest() {
        super(".nt.deflate", new DefaultCodec());
    }
}
