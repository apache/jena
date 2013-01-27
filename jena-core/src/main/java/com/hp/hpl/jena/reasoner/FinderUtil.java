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

package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.*;

/**
 * Some simple helper methods used when working with Finders,
 * particularly to compose them into cascade sequences.
 * The cascades are designed to cope with null Finders as well.
 */
public class FinderUtil {
    
    /**
     * Create a continuation object which is a cascade of two
     * continuation objects.
     * @param first the first Graph/Finder to try
     * @param second the second Graph/Finder to try
     */
    public static Finder cascade(Finder first, Finder second) {
        if (first == null || (first instanceof FGraph && ((FGraph)first).getGraph() == null)) return second;
        if (second == null || (second instanceof FGraph && ((FGraph)second).getGraph() == null)) return first;
        return new Cascade(first, second);
    }
    
    /**
     * Create a continuation object which is a cascade of three
     * continuation objects.
     * @param first the first Graph/Finder to try
     * @param second the second Graph/Finder to try
     * @param third the third Graph/Finder to try
     */
    public static Finder cascade(Finder first, Finder second, Finder third) {
        return new Cascade(first, cascade(second, third));
    }
    
    /**
     * Create a continuation object which is a cascade of four
     * continuation objects.
     * @param first the first Graph/Finder to try
     * @param second the second Graph/Finder to try
     * @param third the third Graph/Finder to try
     * @param fourth the third Graph/Finder to try
     */
    public static Finder cascade(Finder first, Finder second, Finder third, Finder fourth) {
        return new Cascade(first, cascade(second, cascade(third, fourth)));
    }
    
    /**
     * Inner class used to implement cascades of two continuation objects
     */
    private static class Cascade implements Finder {
        /** the first Graph/Finder to try */
        Finder first;
        
        /** the second Graph/Finder to try */
        Finder second;
        
        /**
         * Constructor 
         */
        Cascade(Finder first, Finder second) {
            this.first = first;
            this.second = second;
        }
        
        /**
         * Basic pattern lookup interface.
         * @param pattern a TriplePattern to be matched against the data
         * @return a ClosableIterator over all Triples in the data set
         *  that match the pattern
         */
        @Override
        public ExtendedIterator<Triple> find(TriplePattern pattern) {
            if (second == null) {
                return first.find(pattern);
            } else if (first == null) {
                return second.find(pattern);
            } else {
                return first.findWithContinuation(pattern, second);
            }
        }
        
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
        @Override
        public ExtendedIterator<Triple> findWithContinuation(TriplePattern pattern, Finder continuation) {
            return (FinderUtil.cascade(first, second, continuation)).find(pattern);
        }

        /**
         * Return true if the given pattern occurs somewhere in the find sequence.
         */
        @Override
        public boolean contains(TriplePattern pattern) {
            ClosableIterator<Triple> it = find(pattern);
            boolean result = it.hasNext();
            it.close();
            return result;
        }

    }
}
