/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.hadoop.rdf.io.input.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A tracked input stream which can is a wrapper around another input stream and
 * can report the number of bytes read
 * 
 * 
 * 
 */
public class TrackedInputStream extends TrackableInputStream {

    protected InputStream input;
    protected long bytesRead = 0, lastMark;

    /**
     * Creates a new tracked input stream
     * 
     * @param input
     *            Input stream to track
     */
    public TrackedInputStream(InputStream input) {
        if (input == null)
            throw new NullPointerException("Input cannot be null");
        this.input = input;
    }

    @Override
    public int read() throws IOException {
        int read = this.input.read();
        if (read >= 0)
            this.bytesRead++;
        return read;
    }

    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }

    @Override
    public int available() throws IOException {
        return this.input.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.input.mark(readlimit);
        this.lastMark = this.bytesRead;
    }

    @Override
    public boolean markSupported() {
        return this.input.markSupported();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) return 0;
        int read = this.input.read(b, off, len);
        if (read > 0)
            this.bytesRead += read;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.input.reset();
        this.bytesRead = this.lastMark;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n == 0)
            return 0;
        long skipped = 0;
        byte[] buffer = new byte[16];
        int readSize = Math.min(buffer.length, n > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) n);
        int read;
        do {
            if (n - skipped > readSize) {
                read = this.input.read(buffer, 0, readSize);
            } else {
                read = this.input.read(buffer, 0, (int) (n - skipped));
            }
            if (read > 0) {
                this.bytesRead += read;
                skipped += read;
            }
        } while (skipped < n && read >= 0);

        return skipped;
    }
}
