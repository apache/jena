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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Abstract tests for {@link TrackableInputStream} implementations
 * 
 * 
 * 
 */
public abstract class AbstractTrackableInputStreamTests {

    protected static final int KILO = 1024;
    protected static final int BYTES_PER_KB = KILO;
    protected static final int BYTES_PER_MB = BYTES_PER_KB * KILO;

    /**
     * Gets the instance to test using the given input as the stream to track
     * 
     * @param input
     *            Input Stream
     * @return Trackable Input Stream
     */
    protected abstract TrackableInputStream getInstance(InputStream input);

    /**
     * Generates an input stream containing the given number of bytes
     * 
     * @param length
     *            Number of bytes
     * @return Input stream
     */
    protected final InputStream generateData(int length) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(length);
        byte b = (byte) 'b';
        for (int i = 0; i < length; i++) {
            output.write(b);
        }
        return new ByteArrayInputStream(output.toByteArray());
    }

    protected final void testSingleByteRead(int length) throws IOException {
        InputStream input = this.generateData(length);
        try ( TrackableInputStream trackable = this.getInstance(input) ) {
            long count = 0;
            while (trackable.read() >= 0) {
                count++;
            }
            Assert.assertEquals(length, count);
            Assert.assertEquals(length, trackable.getBytesRead());
        }
    }

    /**
     * Test reading byte by byte
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_single_01() throws IOException {
        this.testSingleByteRead(0);
    }

    /**
     * Test reading byte by byte
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_single_02() throws IOException {
        this.testSingleByteRead(100);
    }

    /**
     * Test reading byte by byte
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_single_03() throws IOException {
        // 1KB
        this.testSingleByteRead(BYTES_PER_KB);
    }

    /**
     * Test reading byte by byte
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_single_04() throws IOException {
        // 1 MB
        this.testSingleByteRead(BYTES_PER_MB);
    }

    protected final void testMultiByteRead(int length, int bufferSize) throws IOException {
        if (bufferSize < 1)
            throw new IllegalArgumentException("bufferSize must be >= 1");
        InputStream input = this.generateData(length);
        try (TrackableInputStream trackable = this.getInstance(input)) {
            long count = 0;
            byte[] buffer = new byte[bufferSize];
            long read;
            do {
                read = trackable.read(buffer);
                if (read > 0)
                    count += read;
            } while (read >= 0);
            Assert.assertEquals(length, count);
            Assert.assertEquals(length, trackable.getBytesRead());
        }
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_01() throws IOException {
        this.testMultiByteRead(0, 1);
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_02() throws IOException {
        this.testMultiByteRead(0, 16);
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_03() throws IOException {
        this.testMultiByteRead(0, BYTES_PER_KB);
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_04() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, 1);
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_05() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, 16);
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_06() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, BYTES_PER_KB);
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_07() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, 1);
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_08() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, 16);
    }

    /**
     * Test reading multiple bytes i.e. calling {@link InputStream#read(byte[])}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_09() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, BYTES_PER_KB);
    }

    protected final void testMultiByteRead(int length, int bufferSize, int readSize) throws IOException {
        if (bufferSize < 1)
            throw new IllegalArgumentException("bufferSize must be >= 1");
        if (readSize < 1 || readSize > bufferSize)
            throw new IllegalArgumentException("readSize must be >= 1 and <= bufferSize");
        InputStream input = this.generateData(length);
        try (TrackableInputStream trackable = this.getInstance(input)) {
            long count = 0;
            byte[] buffer = new byte[bufferSize];
            long read;
            do {
                read = trackable.read(buffer, 0, readSize);
                if (read > 0)
                    count += read;
            } while (read >= 0);
            Assert.assertEquals(length, count);
            Assert.assertEquals(length, trackable.getBytesRead());
        }
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_01() throws IOException {
        this.testMultiByteRead(0, 1, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_02() throws IOException {
        this.testMultiByteRead(0, 16, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_03() throws IOException {
        this.testMultiByteRead(0, 16, 16);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_04() throws IOException {
        this.testMultiByteRead(0, BYTES_PER_KB, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_05() throws IOException {
        this.testMultiByteRead(0, BYTES_PER_KB, 16);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_06() throws IOException {
        this.testMultiByteRead(0, BYTES_PER_KB, BYTES_PER_KB);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_07() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, 1, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_08() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, 16, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_09() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, 16, 16);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_10() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, BYTES_PER_KB, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_11() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, BYTES_PER_KB, 16);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_12() throws IOException {
        // 1KB
        this.testMultiByteRead(BYTES_PER_KB, BYTES_PER_KB, BYTES_PER_KB);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_13() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, 1, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_14() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, 16, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_15() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, 16, 16);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_16() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, BYTES_PER_KB, 1);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_17() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, BYTES_PER_KB, 16);
    }

    /**
     * Test reading multiple bytes while reading less than the buffer size bytes
     * i.e. calling {@link InputStream#read(byte[], int, int)}
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_read_multiple_partial_18() throws IOException {
        // 1MB
        this.testMultiByteRead(BYTES_PER_MB, BYTES_PER_KB, BYTES_PER_KB);
    }

    protected final void testSkip(int length, long skipSize) throws IOException {
        if (skipSize < 1)
            throw new IllegalArgumentException("skipSize must be >= 1");
        InputStream input = this.generateData(length);
        try(TrackableInputStream trackable = this.getInstance(input)) {
            long count = 0;
            long skipped;
            do {
                skipped = trackable.skip(skipSize);
                if (skipped > 0)
                    count += skipped;
            } while (skipped > 0);
            Assert.assertEquals(length, count);
            Assert.assertEquals(length, trackable.getBytesRead());
        }
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_01() throws IOException {
        this.testSkip(0, 1);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_02() throws IOException {
        this.testSkip(100, 1);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_03() throws IOException {
        this.testSkip(100, 16);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_04() throws IOException {
        this.testSkip(100, BYTES_PER_KB);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_05() throws IOException {
        // 1KB
        this.testSkip(BYTES_PER_KB, 1);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_06() throws IOException {
        // 1KB
        this.testSkip(BYTES_PER_KB, 16);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_07() throws IOException {
        // 1KB
        this.testSkip(BYTES_PER_KB, BYTES_PER_KB);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_08() throws IOException {
        // 1KB
        this.testSkip(BYTES_PER_KB, BYTES_PER_MB);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_09() throws IOException {
        // 1 MB
        this.testSkip(BYTES_PER_MB, 1);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_10() throws IOException {
        // 1 MB
        this.testSkip(BYTES_PER_MB, 16);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_11() throws IOException {
        // 1 MB
        this.testSkip(BYTES_PER_MB, BYTES_PER_KB);
    }

    /**
     * Test skipping
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_skip_single_12() throws IOException {
        // 1 MB
        this.testSkip(BYTES_PER_MB, BYTES_PER_MB);
    }

    /**
     * Tests behaviour after closing
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_post_close_01() throws IOException {
        InputStream input = this.generateData(0);
        @SuppressWarnings("resource")
        TrackableInputStream trackable = this.getInstance(input);
        trackable.close();
        Assert.assertEquals(-1, trackable.read());
    }
    
    /**
     * Tests behaviour after closing
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_post_close_02() throws IOException {
        InputStream input = this.generateData(0);
        @SuppressWarnings("resource")
        TrackableInputStream trackable = this.getInstance(input);
        trackable.close();
        Assert.assertEquals(0, trackable.read(new byte[0]));
    }
    
    /**
     * Tests behaviour after closing
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_post_close_03() throws IOException {
        InputStream input = this.generateData(0);
        @SuppressWarnings("resource")
        TrackableInputStream trackable = this.getInstance(input);
        trackable.close();
        Assert.assertEquals(-1, trackable.read(new byte[1]));
    }
    
    /**
     * Tests behaviour after closing
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_post_close_04() throws IOException {
        InputStream input = this.generateData(0);
        @SuppressWarnings("resource")
        TrackableInputStream trackable = this.getInstance(input);
        trackable.close();
        Assert.assertEquals(0, trackable.read(new byte[16], 0, 0));
    }
    
    /**
     * Tests behaviour after closing
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_post_close_05() throws IOException {
        InputStream input = this.generateData(0);
        @SuppressWarnings("resource")
        TrackableInputStream trackable = this.getInstance(input);
        trackable.close();
        Assert.assertEquals(-1, trackable.read(new byte[16], 0, 8));
    }
    
    /**
     * Tests behaviour after closing
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_post_close_06() throws IOException {
        InputStream input = this.generateData(0);
        @SuppressWarnings("resource")
        TrackableInputStream trackable = this.getInstance(input);
        trackable.close();
        Assert.assertEquals(0, trackable.skip(0));
    }
    
    /**
     * Tests exceptions are thrown trying to perform actions after closing the
     * input
     * 
     * @throws IOException
     */
    @Test
    public final void trackable_input_post_close_07() throws IOException {
        InputStream input = this.generateData(0);
        @SuppressWarnings("resource")
        TrackableInputStream trackable = this.getInstance(input);
        trackable.close();
        Assert.assertEquals(0, trackable.skip(1));
    }
}
