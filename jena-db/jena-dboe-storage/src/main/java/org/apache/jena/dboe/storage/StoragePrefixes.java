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

package org.apache.jena.dboe.storage;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.dboe.storage.prefixes.PrefixEntry;
import org.apache.jena.graph.Node;

/** Like PrefixMapping, only for a dataset which can have different prefix maps for different graphs.
 * There is a distinguished name {@linkplain Prefixes#nodeDataset} that means the prefixes
 * apply to the dataset as a whole, not a specific graph.
 */
public interface StoragePrefixes
{
    /* Get the prefix mapping of a prefix */
    public String get(Node graphNode, String prefix);

    /* Access to the storage - access by graph name */
    public Iterator<PrefixEntry> get(Node graphNode);

    /* Access to the storage - enumerate the graph nodes */
    public Iterator<Node> listGraphNodes();

    /** Add a prefix, overwrites any existing association */
    public void add(Node graphNode, String prefix, String iriStr);

    /** Delete a prefix mapping */
    void delete(Node graphNode, String prefix);

    /** Delete prefix mappings for a specific graph name. */
    void deleteAll(Node graphNode);

    /**
     * All the mappings.
     * This includes the "dataset" mapping with Node {@link Prefixes#nodeDataset}.
     */
    public default Iterator<Pair<Node, PrefixEntry>> listMappings() {
        return Iter.flatMap(listGraphNodes(),
            // graph names to iterator of pairs for this name.
            n->Iter.map(get(n), prefixEntry -> Pair.create(n, prefixEntry))
            );
    }

    /** Return whether there are any prefix mappings or not (any graph). */
    public boolean isEmpty();

    /** Return the number of mappings. */
    public int size();

}

