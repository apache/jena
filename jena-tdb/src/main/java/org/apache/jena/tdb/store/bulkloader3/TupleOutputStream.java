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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.openjena.atlas.AtlasException;
import org.openjena.atlas.lib.Sink;
import org.openjena.atlas.lib.Tuple;

public class TupleOutputStream implements Sink<Tuple<Long>> {

    private DataOutputStream out ;
    
    public TupleOutputStream(OutputStream out) {
        this.out = DataStreamFactory.createDataOutputStream(out) ;
    }

    @Override
    public void send(Tuple<Long> tuple) {
        Iterator<Long> iter = tuple.iterator() ;
        while ( iter.hasNext() ) {
            try {
                out.writeLong( iter.next() ) ;
            } catch (IOException e) {
                new AtlasException(e) ;
            }
        }
    }

    @Override
    public void flush() {
        try {
            out.flush() ;
        } catch (IOException e) {
            new AtlasException(e) ;
        }
    }

    @Override
    public void close() {
        try {
            out.close() ;
        } catch (IOException e) {
            new AtlasException(e) ;
        }
    }
    
}
