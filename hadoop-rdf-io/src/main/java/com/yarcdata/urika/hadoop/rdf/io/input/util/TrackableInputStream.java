/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.util;

import java.io.InputStream;

/**
 * An input stream that tracks the number of bytes read
 * 
 * @author rvesse
 * 
 */
public abstract class TrackableInputStream extends InputStream {

    /**
     * Gets the number of bytes read
     * 
     * @return Number of bytes read
     */
    public abstract long getBytesRead();

}