/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

/**
 * Abstract tests for blocked triple input formats
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractBlockedQuadInputFormatTests extends AbstractWholeFileQuadInputFormatTests {

    @Override
    protected boolean canSplitInputs() {
        return true;
    }
}
