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

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.util.iterator.Filter;

/**
    A class to turn a triple (treated as a pattern) into a Filter.
    It used to take a TripleMatch but those are obsolete.
*/
public class TripleMatchFilter extends Filter<Triple> 
    {
    final protected Triple tMatch;

    /** Creates new TripleMatchFilter */
    public TripleMatchFilter(Triple tMatch) 
        { this.tMatch = tMatch; }

    /** 
         The object is wanted.
         @param t The object to accept or reject.  Must be a Triple
         @return true if the object is wanted.
    */
    @Override
    public boolean accept( Triple t ) 
        { return tMatch.matches( t ); }
    }
