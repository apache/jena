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

package org.apache.jena.dboe.base.file;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;

/** Implementation of {@link BinaryDataFile} using {@link RandomAccessFile}.
 *
 * <ul>
 * <li>No buffering of reads or writes provided.
 * <li>Not thread-safe.
 * </ul>
 *
 *  @see BinaryDataFileWriteBuffered
 */
public class BinaryDataFileRandomAccess implements BinaryDataFile {
    // On OpenJDK, RandomAccessFile and FileChannelImpl both
    // dive into native code.
    protected RandomAccessFile file;
    protected boolean readMode;
    protected long readPosition;
    protected long writePosition;
    private final String filename;

    public BinaryDataFileRandomAccess(String filename) {
       this.filename = filename;
    }

    @Override
    public void open() {
        if ( file != null )
            throw new RuntimeIOException("Already open");
        try {
            file = new RandomAccessFile(filename, "rw");
            writePosition = file.length();
            readPosition = 0;
            readMode = true;
        }
        catch (IOException e) { IO.exception(e); }
    }

    @Override
    public boolean isOpen() {
        return file != null;
    }

    @Override
    public int read(long posn, byte[] b, int start, int length) {
        checkOpen();
        switchToReadMode();
        seek(posn);
        try {
            int x = file.read(b, start, length);
            readPosition += x;
            return x;
        }
        catch (IOException ex) { IO.exception(ex); return -1; }
    }

    @Override
    public long write(byte[] b, int start, int length) {
        checkOpen();
        switchToWriteMode();
        long x = writePosition;
        try {
            file.write(b, start, length);
            writePosition += length;
        }
        catch (IOException ex) { IO.exception(ex); }
        return x;
    }

    // Move the RandomAccess file pointer.
    private void seek(long posn) {
        try { file.seek(posn); }
        catch (IOException ex) { IO.exception(ex); }
    }

    @Override
    public void truncate(long length) {
        checkOpen();
        switchToWriteMode();
        try { file.setLength(length); }
        catch (IOException ex) { IO.exception(ex); }
    }

    @Override
    public void sync() {
        checkOpen();
        flush$();
    }

    protected void flush$() {
        try { file.getFD().sync(); }
        catch (IOException ex) { IO.exception(ex); }
    }

    @Override
    public void close() {
        if ( ! isOpen() )
            return;
        try { file.close(); }
        catch (IOException ex) { IO.exception(ex); }
        file = null;
    }

    @Override
    public long length() {
        try { return file.length();}
        catch (IOException ex) { IO.exception(ex); return -1 ;}
    }

    protected void switchToReadMode() {
        if ( ! readMode )
            readMode = true;
    }

    protected void switchToWriteMode() {
        if ( readMode ) {
            readMode = false;
            seek(writePosition);
        }
    }

    protected void checkOpen() {
        if ( ! isOpen() )
            throw new RuntimeIOException("Not open");
    }
}

