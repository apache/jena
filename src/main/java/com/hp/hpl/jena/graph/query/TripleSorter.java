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

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    Interface for things that can sort triples (for optimising queries).
    
 */
public interface TripleSorter
    {
    /**
        Sort the array triples and return the reorganised array. A new array may be returned
        or the existing array reorganised in-place. The result array may have a different
        size from the original; the requirement is just that a query using the result must
        deliver the same results as one using the original. We hope, of course, that the
        performance of the query is improved ...
    */
    public Triple [] sort( Triple [] triples );
    
    /**
        A TripleSorter that does not alter the triple array at all.
     */
    public static final TripleSorter dontSort = new TripleSorter()
        { @Override
        public Triple []  sort( Triple [] ts ) { return ts; } };        
    }
