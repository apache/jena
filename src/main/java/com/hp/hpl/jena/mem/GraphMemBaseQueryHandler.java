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

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    A QueryHandler for GraphMemBase's subclasses, exploiting those classes
    indexes to implement objectsFor, predicatesFor, and subjectsFor efficiently
    for the (ANY, ANY) case used in listSubjects(), listPredicates(), and
    listObjects().
    
    @author kers
*/
public class GraphMemBaseQueryHandler extends SimpleQueryHandler
    {
    protected final TripleStore store;

    public GraphMemBaseQueryHandler( GraphMemBase graph )
        { super( graph ); this.store = graph.store; }

    @Override public ExtendedIterator<Node> objectsFor( Node s, Node p )
        { return bothANY( s, p ) ? findObjects() : super.objectsFor( s, p ); }

    @Override public ExtendedIterator<Node> predicatesFor( Node s, Node o )
        { return bothANY( s, o ) ? findPredicates() : super.predicatesFor( s, o ); }

    @Override public ExtendedIterator<Node> subjectsFor( Node p, Node o )
        { return bothANY( p, o ) ? findSubjects() : super.subjectsFor( p, o ); }

    /**
         Answer true iff both <code>a</code> and <code>b</code> are ANY wildcards
         or are null (legacy). 
    */
    private boolean bothANY( Node a, Node b )
        { return (a == null || a.equals( Node.ANY )) && (b == null || b.equals( Node.ANY )); }

    public ExtendedIterator<Node> findPredicates()
        { return store.listPredicates(); }

    public ExtendedIterator<Node> findObjects()
        { return store.listObjects(); }

    public ExtendedIterator<Node> findSubjects()
        { return store.listSubjects(); }

    }
