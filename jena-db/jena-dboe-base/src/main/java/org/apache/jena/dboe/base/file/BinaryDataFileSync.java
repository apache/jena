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

/** Add synchronization to all BinaryDataFile operations.
 *  This gives the correct thread-safe operation
 *  but isn't necessarily the best way to do it.
 */
public class BinaryDataFileSync implements BinaryDataFile {
    private final BinaryDataFile other;

    public BinaryDataFileSync(BinaryDataFile other) {
        this.other = other;
    }

    @Override
    synchronized
    public void open() { other.open(); }

    @Override
    synchronized
    public boolean isOpen() { return other.isOpen(); }

    @Override
    synchronized
    public int read(long posn, byte[] b) {
        return other.read(posn, b);
    }

    @Override
    synchronized
    public int read(long posn, byte[] b, int start, int length) {
        return other.read(posn, b, start, length);
    }

    @Override
    synchronized
    public long write(byte[] b) {
        return other.write(b);
    }

    @Override
    synchronized
    public long write(byte[] b, int start, int length) {
        return other.write(b, start, length);
    }

    @Override
    synchronized
    public long length() {
        return other.length();
    }

    @Override
    synchronized
    public void truncate(long length) { other.truncate(length); }

    @Override
    synchronized
    public void sync() { other.sync(); }

    @Override
    synchronized
    public void close() { other.close(); }
}

