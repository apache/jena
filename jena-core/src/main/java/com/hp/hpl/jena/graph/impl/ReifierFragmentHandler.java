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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
     ReifierFragmentHandler: instances of this class handle fragments of reifications,
     ie the triples (tag rdf:subject/predicate/object X) and (tag rdf:type Statement).
     They are delivered from FragmentHandler instances and remain bound to
     their originating instance.
     
*/
public interface ReifierFragmentHandler
    {
    /**
         If this handler clashed with the complete reification of <code>reified</code>,
         because its predicate and the given object aren't the same as that of
         <code>reified</code>, add all five fragments to the its underlying 
         fragmentsMap and answer <code>true</code>; otherwise answer
         <code>false</code>.
         
         @param fragmentObject the object of the reification fragment
         @param reified the completely reified triple
         @return true iff the fragment clashed with the triple
    */
    public abstract boolean clashedWith( Node tag, Node fragmentObject, Triple reified );

    /**
         If this <code>fragment</code> completes a reification for <code>tag</code>,
         remove all the fragments from the underlying fragmentsMap and answer the
         reified triple; otherwise add this fragment to the map and answer
         <code>null</code>.
         
         @param fragment the new fragment to consider
         @param tag the tag for the reification [equals the fragment subject]
         @param object the object of the fragment. Hmm.
         @return the reified triple, if there is one, otherwise null
    */
    public abstract Triple reifyIfCompleteQuad( Triple fragment, Node tag, Node object );

    /**
     * @param tag
     * @param already
     * @param fragment
     * @return
     */
    public abstract Triple removeFragment( Node tag, Triple already, Triple fragment );
    }
