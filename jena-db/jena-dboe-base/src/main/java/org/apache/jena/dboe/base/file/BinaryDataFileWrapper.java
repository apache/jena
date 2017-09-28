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

public class BinaryDataFileWrapper implements BinaryDataFile {
    private final BinaryDataFile other ;
    
    public BinaryDataFileWrapper(BinaryDataFile other) {
        this.other = other ;
    }
    
    @Override
    public void open() { other.open() ; } 

    @Override
    public boolean isOpen() { return other.isOpen() ; }

    @Override
    public int read(long posn, byte[] b) {
        return other.read(posn, b) ;
    }

    @Override
    public int read(long posn, byte[] b, int start, int length) {
        return other.read(posn, b, start, length) ;
    }

    @Override
    public long write(byte[] b) {
        return other.write(b) ;
    }
    
    @Override
    public long write(byte[] b, int start, int length) {
        return other.write(b, start, length) ;
    }

    @Override
    public long length() {
        return other.length() ;
    }

    @Override
    public void truncate(long length) { other.truncate(length); }

    @Override
    public void sync() { other.sync(); }

    @Override
    public void close() { other.close(); }
}

