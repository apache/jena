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

package org.apache.jena.reasoner;

import org.apache.jena.graph.Triple ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * Minimal interface for preforming simple pattern find operations.
 * Should be implemented by reasoners, caches and related datastructures.
 */
public interface Finder {

    /**
     * Basic pattern lookup interface.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ClosableIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator<Triple> find(TriplePattern pattern);
    
    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results.
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    public ExtendedIterator<Triple> findWithContinuation(TriplePattern pattern, Finder continuation);

    /**
     * Return true if the given pattern occurs somewhere in the find sequence.
     */
    public boolean contains(TriplePattern pattern);
    
}
