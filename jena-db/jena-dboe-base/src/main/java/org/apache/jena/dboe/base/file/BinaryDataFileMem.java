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

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;

/** Implementation of {@link BinaryDataFile} in memory for testing
 * and development use. Raw performance is not an objective.
 *
 * <li>This implementation is thread-safe.
 */
public class BinaryDataFileMem implements BinaryDataFile {

    private boolean readMode;
    private SegmentedMemBuffer storage;

    public BinaryDataFileMem() {
    }

    @Override
    synchronized
    public void open() {
        if ( storage != null )
            throw new RuntimeIOException("Already open");
        storage = new SegmentedMemBuffer();
        readMode = true;
    }

    @Override
    synchronized
    public boolean isOpen() {
        return storage != null;
    }

    @Override
    synchronized
    public int read(long posn, byte[] b, int start, int length) {
        checkOpen();
        switchToReadMode();
        return storage.read(posn, b, start, length);
    }

    @Override
    synchronized
    public long write(byte[] b, int start, int length) {
        checkOpen();
        switchToWriteMode();
        long x = storage.length();
        storage.write(x, b, start, length);
        return x;
    }

    @Override
    synchronized
    public void truncate(long length) {
        if ( length < 0 )
            IO.exception(String.format("truncate: bad length : %d", length));
        checkOpen();
        switchToWriteMode();
        storage.truncate(length);
    }

    @Override
    synchronized
    public void sync() {
        checkOpen();
        storage.sync();
    }

    @Override
    synchronized
    public void close() {
        if ( ! isOpen() )
            return;
        storage.close();
        storage = null;
    }

    @Override
    synchronized
    public long length() {
        return storage.length();
    }

    private void switchToReadMode() {
        readMode = true;
    }

    private void switchToWriteMode() {
        readMode = false;
    }

    private void checkOpen() {
        if ( ! isOpen() )
            throw new RuntimeIOException("Not open");
    }
}

