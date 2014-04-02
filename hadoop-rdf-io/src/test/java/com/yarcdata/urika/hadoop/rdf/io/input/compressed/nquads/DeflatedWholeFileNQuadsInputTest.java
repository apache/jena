/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.nquads;

import org.apache.hadoop.io.compress.DefaultCodec;

/**
 * Tests for deflated NQuads input
 * 
 * @author rvesse
 * 
 */
public class DeflatedWholeFileNQuadsInputTest extends AbstractCompressedWholeFileNQuadsInputFormatTests {

    /**
     * Creates new tests
     */
    public DeflatedWholeFileNQuadsInputTest() {
        super(".nq.deflate", new DefaultCodec());
    }
}
