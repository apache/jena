/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A block input stream which can is a wrapper around another input stream which
 * restricts reading to a specific number of bytes and can report the number of
 * bytes read
 * <p>
 * The class assumes that the underlying input stream has already been seeked to
 * the appropriate start point
 * </p>
 * 
 * @author rvesse
 * 
 */
public final class BlockInputStream extends TrackedInputStream {

    private long limit = Long.MAX_VALUE;

    /**
     * Creates a new tracked input stream
     * 
     * @param input
     *            Input stream to track
     * @param limit
     *            Maximum number of bytes to read from the stream
     */
    public BlockInputStream(InputStream input, long limit) {
        super(input);
        if (limit < 0)
            throw new IllegalArgumentException("limit must be >= 0");
        this.limit = limit;
    }

    @Override
    public int read() throws IOException {
        if (this.bytesRead >= this.limit) {
            return -1;
        }
        return super.read();
    }

    @Override
    public int available() throws IOException {
        if (this.bytesRead >= this.limit) {
            return 0;
        }
        return super.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        } else if (this.bytesRead >= this.limit) {
            return -1;
        } else if (len > this.limit - this.bytesRead) {
            len = (int) (this.limit - this.bytesRead);
        }
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        if (n == 0) {
            return 0;
        } else if (this.bytesRead >= this.limit) {
            return -1;
        } else if (n > this.limit - this.bytesRead) {
            n = this.limit - this.bytesRead;
        }
        return super.skip(n);
    }
}
