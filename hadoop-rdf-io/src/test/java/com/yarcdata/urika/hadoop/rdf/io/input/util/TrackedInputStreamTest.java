/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.util;

import java.io.InputStream;

/**
 * Tests for the {@link TrackedInputStream}
 * 
 * @author rvesse
 * 
 */
public class TrackedInputStreamTest extends AbstractTrackableInputStreamTests {

    @Override
    protected TrackableInputStream getInstance(InputStream input) {
        return new TrackedInputStream(input);
    }

}
