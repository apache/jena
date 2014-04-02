/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.trig;

import org.apache.hadoop.io.compress.BZip2Codec;

/**
 * Tests for BZipped TriG input
 * 
 * @author rvesse
 * 
 */
public class BZippedTriGInputTest extends AbstractCompressedTriGInputFormatTests {

    /**
     * Creates new tests
     */
    public BZippedTriGInputTest() {
        super(".trig.bz2", new BZip2Codec());
    }
}
