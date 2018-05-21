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

package org.apache.jena.sparql.serializer;

import java.util.*;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/** Internal record of list details. */
/*package*/ class TriplesListBlock {
    Map<Node, List<Node>> listElementsMap = new HashMap<>();
    // Triples in lists.
    Set<Triple>           triplesInLists  = new LinkedHashSet<>();

    /*package*/ void merge(TriplesListBlock other) {
        listElementsMap.putAll(other.listElementsMap);
        triplesInLists.addAll(other.triplesInLists);
    }
        
    @Override
    public String toString() {
        return Iter.asString(listElementsMap.keySet(), ", ") + "\n" + "{"+ Iter.asString(triplesInLists.iterator(), "\n")+"}";
            
    }
}