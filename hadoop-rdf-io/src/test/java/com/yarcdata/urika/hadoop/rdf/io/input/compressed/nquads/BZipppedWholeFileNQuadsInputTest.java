/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.nquads;

import org.apache.hadoop.io.compress.BZip2Codec;

/**
 * Tests for BZipped NQuads input
 * 
 * @author rvesse
 * 
 */
public class BZipppedWholeFileNQuadsInputTest extends AbstractCompressedWholeFileNQuadsInputFormatTests {

    /**
     * Creates new tests
     */
    public BZipppedWholeFileNQuadsInputTest() {
        super(".nq.bz2", new BZip2Codec());
    }
}
