/**
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

package org.apache.jena.tdb.store.bulkloader3;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.openjena.atlas.AtlasException;
import org.openjena.atlas.lib.Closeable;
import org.openjena.atlas.lib.Pair;

public class PairInputStream implements Iterator<Pair<byte[], byte[]>>, Closeable {

    private DataInputStream in ;
    private Pair<byte[], byte[]> slot = null ;
    
    public PairInputStream(InputStream in) {
        this.in = DataStreamFactory.createDataInputStream(in) ;
        slot = readNext() ;
    }

    @Override
    public boolean hasNext() {
        return slot != null ;
    }

    @Override
    public Pair<byte[], byte[]> next() {
        Pair<byte[], byte[]> result = slot ;
        slot = readNext() ;
        return result ;
    }
    
    private Pair<byte[], byte[]> readNext() {
        try {
            byte left[] = new byte[in.readInt()] ;
            in.readFully(left) ;
            byte right[] = new byte[in.readInt()] ;
            in.readFully(right) ;
            return new Pair<byte[], byte[]> (left, right) ;
        } catch (IOException e) {
            return null ;
        }
    }

    @Override
    public void remove() {
        throw new AtlasException("Method not implemented.") ;
    }

    @Override
    public void close() {
        try {
            in.close() ;
        } catch (IOException e) {
            new AtlasException(e) ;
        }        
    }
    
}

