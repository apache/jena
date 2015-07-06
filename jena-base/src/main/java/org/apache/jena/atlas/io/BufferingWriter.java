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

package org.apache.jena.atlas.io ;

import java.io.IOException ;
import java.io.Writer ;

import org.apache.jena.atlas.io.IO ;

/**
 * A buffering writer. Like BufferedWriter but with no synchronization. A
 * "synchronized" per character can be expensive.
 * <p>
 * The standard java.io classes have hidden synchronization so in some very
 * critical situations, this can be expensive.
 * </p>
 * This class is not thread safe.
 */

public final class BufferingWriter extends Writer {
    // Default sizes
    private static final int SIZE      = 8 * 1024 ;      // Unit size in bytes.
    private static final int BLOB_SIZE = SIZE / 2 ;      // Large object size,
                                                          // worse case, bytes
    // Sizes for this instance
    private final int        blockSize ;
    private final int        blobSize ;

    private char[]           buffer    = new char[SIZE] ;
    private int              idx       = 0 ;
    private Writer           out ;

    /** Create a buffering writer */
    public BufferingWriter(Writer dest) {
        this(dest, SIZE, BLOB_SIZE) ;
    }

    /** Create a buffering writer */
    public BufferingWriter(Writer dest, int size) {
        this(dest, size, size/2) ;
    }

    /** Create a buffering writer */
    public BufferingWriter(Writer dest, int size, int blobSize) {
        this.out = dest ;
        this.blockSize = size ;
        this.blobSize = blobSize ;
    }

    /**
     * Output a string
     * @param string Characters
     */
    public void output(String string) {
        output(string, 0, string.length()) ;
    }
    
    /**
     * Output a string
     * 
     * @param string Characters
     * @param off    Starting point in the string
     * @param length Length
     */
    public void output(String string, int off, int length) {
        boolean largeBlob = (length > blobSize) ;

        // There is no space or too big
        if ( largeBlob || (blockSize - idx) < length )
            flushBuffer() ;
        // If too big, do directly.
        if ( largeBlob /* too big */) {
            try { out.write(string, off, length) ; }
            catch (IOException ex) { IO.exception(ex) ; }
            return ;
        }
        int n = string.length() ;
        string.getChars(off, (n + off), buffer, idx) ;
        idx += n ;
    }

    /** Output an array of characters */
    public void output(char chars[]) {
        output(chars, 0, chars.length) ;
    }

    /**
     * Output an array of characters
     * 
     * @param chars Characters
     * @param start Start
     * @param length Length
     */
    public void output(char chars[], int start, int length) {
        boolean largeBlob = (length > blobSize) ;

        // There is no space or too big
        if ( largeBlob || (blockSize - idx) < length )
            flushBuffer() ;
        // If too big, do directly.
        if ( largeBlob /* too big */) {
            try { out.write(chars) ; }
            catch (IOException ex) { IO.exception(ex) ; }
            return ;
        }
        System.arraycopy(chars, start, buffer, idx, length) ;
        idx += length ;
    }

    /** Output a single character */
    public void output(char ch) {
        if ( blockSize == idx )
            flushBuffer() ;
        buffer[idx++] = ch ;
    }

    private void flushBuffer() {
        if ( idx > 0 ) {
            try { out.write(buffer, 0, idx) ; }
            catch (IOException ex) { IO.exception(ex) ; }
            idx = 0 ;
        }

    }
    
    // ---- Writer

    @Override
    public void close() {
        flushBuffer() ;
        IO.close(out) ;
    }

    @Override
    public void flush() {
        flushBuffer() ;
        IO.flush(out) ;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        output(cbuf, off, len) ;
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length) ;
    }

    @Override
    public void write(String string, int off, int len) throws IOException {
        output(string, off, len) ;
    }

    @Override
    public void write(String string) throws IOException {
        output(string, 0, string.length()) ;
    }

    @Override
    public void write(int ch) throws IOException {
        output((char)ch) ;
    }
}
