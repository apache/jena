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

package org.apache.jena.tdb2.store.nodetable;

import java.util.Objects;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/** A file transport that supports random access read and
 *  buffered append write.
 *  <p>
 *  Adapter TTransport -&gt; BinaryDataFile
 */
public class TReadAppendFileTransport extends TTransport {
    private BinaryDataFile file;
    private long readPosn = -1;

    public TReadAppendFileTransport(BinaryDataFile file) {
        Objects.requireNonNull(file);
        this.file = file;
    }

    @Override
    public boolean isOpen() {
        return file.isOpen();
    }

    @Override
    public void open() {
        file.open();
    }

    @Override
    public void close() {
        file.close();
    }

    public void truncate(long posn) {
        file.truncate(posn);
    }

    public BinaryDataFile getBinaryDataFile() { return file; }

    public long readPosition() {
        return readPosn;
    }

    public void readPosition(long posn) {
        readPosn = posn;
    }

    @Override
    public int read(byte[] buf, int off, int len) {
        int x = file.read(readPosn, buf, off, len);
        readPosn += x;
        return x;
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        file.write(buf, off, len);
    }

    @Override
    public void flush()  {
        file.sync();
    }

    // libthrift 0.14.0
    @Override
    public TConfiguration getConfiguration() {
        throw new NotImplementedException("TReadAppendFileTransport.getConfiguration");
        //return null;
    }

    @Override
    public void updateKnownMessageSize(long size) throws TTransportException {
    }

    @Override
    public void checkReadBytesAvailable(long numBytes) throws TTransportException {
    }
}

