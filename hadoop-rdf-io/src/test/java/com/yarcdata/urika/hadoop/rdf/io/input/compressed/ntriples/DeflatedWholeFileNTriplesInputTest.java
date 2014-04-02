/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.compress.DefaultCodec;


/**
 * Tests for deflated NTriples input
 * 
 * @author rvesse
 * 
 */
public class DeflatedWholeFileNTriplesInputTest extends AbstractCompressedWholeFileNTriplesInputFormatTests {

    /**
     * Creates new tests
     */
    public DeflatedWholeFileNTriplesInputTest() {
        super(".nt.deflate", new DefaultCodec());
    }
}
