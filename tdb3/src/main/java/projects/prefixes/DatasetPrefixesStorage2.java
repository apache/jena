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

package projects.prefixes;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Node ;

/** Like PrefixMapping, only for a dataset which can have different prefix maps for different graphs */

public interface DatasetPrefixesStorage2 // replaces DatasetPrefixStorage
{
    //Distinguished nodes:
    //  Default graph: Quad.defaultGraphIRI
    //  "whole dataset" : null 
    
    /* Get the prefix mapping of a prefix */
    public String get(Node graphNode, String prefix) ;
    
    /* Access to the storage - access by graph name */
    public Iterator<PrefixEntry> get(Node graphNode) ;

    /* Access to the storage - enumerate the graph nodes */
    public Iterator<Node> listGraphNodes() ;

    /** Add a prefix, overwites any existing association */
    public void add(Node graphNode, String prefix, String iriStr) ;
    
    /** Delete a prefix mapping */
    void delete(Node graphNode, String prefix) ;
    
    /** Delete prefix mappings for a specific graph name. */
    void deleteAll(Node graphNode) ;
    
    /** Abbreviate an IRI or return null */
    public String abbreviate(Node graphNode, String iriStr) ;
    
    public Pair<String, String> abbrev(Node graphNode,String uriStr) ;
    
    /** Expand a prefix named, return null if it can't be expanded */
    public String expand(Node graphNode, String prefixedName) ;

    /** Expand a prefix, return null if it can't be expanded */
    public String expand(Node graphNode, String prefix, String localName) ;

    /** Return whether there are any prefix mappings or not (any graph). */
    public boolean isEmpty() ;
    
    /** Return the number of mappings. */
    public int size() ;

}

