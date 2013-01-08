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

/**
    Interface for triple matching; may become obsolete. <i>do not assume this is
    stable</i>. Triple matches are defined by subject, predicate, and object, and
    may be converted to triples [which in fact subsume the work of TripleMatch].
*/
public interface TripleMatch {
    
    /** If it is known that all triples selected by this filter will
     *  have a common subject, return that node, otherwise return null */    
    Node getMatchSubject();
    
    /** If it is known that all triples selected by this match will
     *  have a common predicate, return that node, otherwise return null */
    Node getMatchPredicate();
    
    /** If it is known that all triples selected by this match will
     *  have a common object, return that node, otherwise return null */
    Node getMatchObject();

    /**
        Answer a Triple capturing this match.
    */
    Triple asTriple();
}
