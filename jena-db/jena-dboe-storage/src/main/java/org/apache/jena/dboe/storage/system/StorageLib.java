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

package org.apache.jena.dboe.storage.system;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

public class StorageLib {

    private static final int DeleteBufferSize = 1000;

    /** General purpose "remove triples by pattern" code */
    public static void removeTriples(StorageRDF storage, Node s, Node p, Node o) {
        // Allocate buffer once.
        Triple[] buffer = new Triple[DeleteBufferSize];
        while (true) {
            Iterator<Triple> iter = Iter.limit(storage.find(s, p, o), DeleteBufferSize);
            // Get a slice
            int idx = 0;
            for (; idx < DeleteBufferSize; idx++ ) {
                if ( !iter.hasNext() )
                    break;
                buffer[idx] = iter.next();
            }
            // Delete them.
            for ( int i = 0; i < idx; i++ ) {
                storage.delete(buffer[i]);
                buffer[i] = null;
            }
            // Finished?
            if ( idx < DeleteBufferSize )
                break;
        }
    }

    /** General purpose "remove quads by pattern" code */
    public static void removeQuads(StorageRDF storage, Node g, Node s, Node p, Node o) {
        // Allocate buffer once.
        Quad[] buffer = new Quad[DeleteBufferSize];
        while (true) {
            Iterator<Quad> iter = Iter.limit(storage.find(g, s, p, o), DeleteBufferSize);
            // Get a slice
            int idx = 0;
            for (; idx < DeleteBufferSize; idx++ ) {
                if ( !iter.hasNext() )
                    break;
                buffer[idx] = iter.next();
            }
            // Delete them.
            for ( int i = 0; i < idx; i++ ) {
                storage.delete(buffer[i]);
                buffer[i] = null;
            }
            // Finished?
            if ( idx < DeleteBufferSize )
                break;
        }
    }
}
