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

package org.apache.jena.sparql.util.graph;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;

/** Minimal interface to find by pattern */
public interface Findable
{
    /** Return an iterator over all triples matching the {@code (s,p,o)} pattern. 
     * Each element of {@code (s,p,o)} can be concrete, or the wildcard {@code Node.ANY}. 
     */  
    public Iterator<Triple> find(Node s, Node p, Node o) ;
    
    /** Return whether any triple matches the (s,p,o) pattern. 
     * Each element of {@code (s,p,o)} can be concrete, or the wildcard {@code Node.ANY}. 
     */  
    public boolean contains(Node s, Node p, Node o) ;
    
    /** Return the number of triples matching the (s,p,o) pattern. 
     */  
    public default int count(Node s, Node p, Node o) {
        return (int)Iter.count(find(s,p,o));
    }
}
