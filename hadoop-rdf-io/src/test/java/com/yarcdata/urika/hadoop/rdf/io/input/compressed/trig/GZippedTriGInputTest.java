/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.trig;

import org.apache.hadoop.io.compress.GzipCodec;

/**
 * Tests for GZipped TriG input
 * 
 * @author rvesse
 * 
 */
public class GZippedTriGInputTest extends AbstractCompressedTriGInputFormatTests {

    /**
     * Creates new tests
     */
    public GZippedTriGInputTest() {
        super(".trig.gz", new GzipCodec());
    }
}
