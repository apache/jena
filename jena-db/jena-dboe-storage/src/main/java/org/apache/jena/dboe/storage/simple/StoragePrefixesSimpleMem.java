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

package org.apache.jena.dboe.storage.simple;

import static org.apache.jena.riot.system.PrefixLib.canonicalGraphName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixEntry;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.PrefixMapZero;

/** In-memory dataset prefixes */
public class StoragePrefixesSimpleMem implements StoragePrefixes {
    // Effectively this a map of maps
    private Map<Node, PrefixMap> map = new HashMap<Node, PrefixMap>();

    public StoragePrefixesSimpleMem() {}

    @Override
    public String get(Node graphNode, String prefix) {
        graphNode = canonicalGraphName(graphNode);
        PrefixMap pmap = map.get(graphNode);
        if ( pmap == null )
            return null;
        return pmap.get(prefix);
    }

    @Override
    public Iterator<PrefixEntry> get(Node graphNode) {
        graphNode = canonicalGraphName(graphNode);
        PrefixMap pmap = map.get(graphNode);
        if ( pmap == null )
            return Iter.nullIterator();
        return
            pmap.getMapping().entrySet().stream()
                .map(e->PrefixEntry.create(e.getKey(), e.getValue()))
                .iterator();
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return map.keySet().iterator();
    }

    /** Add a prefix, overwrites any existing association */
    @Override
    public void add(Node graphNode, String prefix, String iriStr) {
        accessForUpdate(graphNode).add(prefix, iriStr);
    }

    /** Delete a prefix mapping */
    @Override
    public void delete(Node graphNode, String prefix) {
        access(graphNode).delete(prefix);
    }

    @Override
    public void deleteAll(Node graphNode) {
        access(graphNode).clear();
    }

    @Override
    public boolean isEmpty() {
        if ( map.isEmpty() )
            return true;
        // All must be empty
        return map.entrySet().stream().allMatch((e) -> e.getValue().isEmpty());
    }

    @Override
    public int size() {
        return 0;
    }

    protected PrefixMap createPrefixMap() {
        return PrefixMapFactory.create();
    }

    // Access or return a fresh, empty.
    private PrefixMap accessForUpdate(Node graphName) {
        graphName = canonicalGraphName(graphName);
        PrefixMap pmap = map.get(graphName);
        if ( pmap == null ) {
            pmap = createPrefixMap();
            map.put(graphName, pmap);
        }
        return pmap;
    }

    // Access or return the empty, dummy mapping.
    private PrefixMap access(Node graphName) {
        graphName = canonicalGraphName(graphName);
        PrefixMap pmap = map.get(graphName);
        if ( pmap == null )
            return PrefixMapZero.empty;
        return pmap;
    }
}
