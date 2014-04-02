/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.trig;

import org.apache.hadoop.io.compress.DefaultCodec;

/**
 * Tests for Deflated TriG input
 * 
 * @author rvesse
 * 
 */
public class DeflatedTriGInputTest extends AbstractCompressedTriGInputFormatTests {

    /**
     * Creates new tests
     */
    public DeflatedTriGInputTest() {
        super(".trig.deflate", new DefaultCodec());
    }
}
