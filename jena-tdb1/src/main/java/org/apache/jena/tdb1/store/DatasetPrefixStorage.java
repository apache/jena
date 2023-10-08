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

package org.apache.jena.tdb1.store;

import java.util.Map ;
import java.util.Set ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.riot.system.PrefixMap;

/**
 * Abstract of prefix storage for graphs in an RDF dataset. This is more general than
 * a set of prefixes for a dataset; it has multiple prefix mappings by graph name.
 */
public interface DatasetPrefixStorage extends Closeable, Sync
{
    /** Return the set of graph names for which there might be prefix mappings */
    public Set<String> graphNames() ;

    /** Get the URI string associated with a prefix string for a specific graph (or null for dataset, not a specific graph) */
    public String readPrefix(String graphName, String prefix) ;
    /** Get the prefix string associated with a URI string for a specific graph (or null for dataset, not a specific graph) */
    public String readByURI(String graphName, String uriStr) ;

    /** Return the mappings for a specific graph.  Do not change this map */
    public Map<String, String> readPrefixMap(String graphName) ;

    /** Add a prefix mapping for a specific graph (or null for dataset, not a specific graph) */
    public void insertPrefix(String graphName, String prefix, String uri) ;

//    /** Copy the prefixes for a graph into the given {@link PrefixMapping}
//     * @param graphName Graph name
//     * @param pmap      Destination.
//     */
//    public void loadPrefixMapping(String graphName, PrefixMapping pmap) ;

    /** Remove the association of a prefix for a specific graph */
    public void removeFromPrefixMap(String graphName, String prefix) ;

    /** Remove all associations for a specific graph. */
    public void removeAllFromPrefixMap(String graphName) ;

    /** Return a PrefixMapping for the dataset. */
    public PrefixMap getPrefixMap() ;

    /** Return a PrefixMapping for a named graph */
    public PrefixMap getPrefixMap(String graphName) ;
}
