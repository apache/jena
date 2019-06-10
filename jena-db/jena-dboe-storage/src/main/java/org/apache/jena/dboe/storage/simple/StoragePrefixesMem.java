/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.dboe.storage.simple;

import static org.apache.jena.dboe.storage.prefixes.PrefixLib.canonicalGraphName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.prefixes.PrefixEntry;
import org.apache.jena.dboe.storage.prefixes.PrefixMapI;
import org.apache.jena.dboe.storage.prefixes.PrefixesFactory;
import org.apache.jena.graph.Node;

/** In-memory dataset prefixes */

public class StoragePrefixesMem implements StoragePrefixes {
    // Effectively this a map of maps
    private Map<Node, PrefixMapI> map = new HashMap<Node, PrefixMapI>();

    public StoragePrefixesMem() {}

    @Override
    public String get(Node graphNode, String prefix) {
        graphNode = canonicalGraphName(graphNode);
        PrefixMapI pmap = map.get(graphNode);
        if ( pmap == null )
            return null;
        return pmap.get(prefix);
    }

    @Override
    public Iterator<PrefixEntry> get(Node graphNode) {
        graphNode = canonicalGraphName(graphNode);
        PrefixMapI pmap = map.get(graphNode);
        if ( pmap == null )
            return Iter.nullIterator();
        if ( pmap.getPrefixMapStorage() != null )
            //If implemented as a map of other-implementation PrefixMapI
            return pmap.getPrefixMapStorage().iterator();
        return pmap.iterator();
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

    protected PrefixMapI createPrefixMap() {
        return PrefixesFactory.createMem();
    }

    // Access or return a fresh, empty.
    private PrefixMapI accessForUpdate(Node graphName) {
        graphName = canonicalGraphName(graphName);
        PrefixMapI pmap = map.get(graphName);
        if ( pmap == null ) {
            pmap = createPrefixMap();
            map.put(graphName, pmap);
        }
        return pmap;
    }

    // Access or return the empty, dummy mapping.
    private PrefixMapI access(Node graphName) {
        graphName = canonicalGraphName(graphName);
        PrefixMapI pmap = map.get(graphName);
        if ( pmap == null )
            return PrefixesFactory.empty();
        return pmap;
    }
}
