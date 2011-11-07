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

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
  a Fragments object is represented by four sets, one for each of the reification
  predicates. The slots are array elements because, sadly, it's easier to dynamically
  choose a slot by number than any other way I could think of.
*/
public class Fragments
        { 
        /**
            A GetSlots allows to extract one of the sets of nodes in the Fragments.
         	@author kers
        */
        public interface GetSlot { public Set<Node> get( Fragments f ); }

        Set<Node> subjects = new HashSet<Node>();
        Set<Node> predicates = new HashSet<Node>();
        Set<Node> objects = new HashSet<Node>();
        Set<Node> types = new HashSet<Node>();
        
        /**
            the Node the fragments are about. 
        */
        private Node anchor;
        
        /**
            a fresh Fragments object remembers the node n and starts
            off with all sets empty. (In use, at least one of the slots will
            then immediately be updated - otherwise there was no reason
            to create the Fragments in the first place ...)
        */
        public Fragments( Node n ) 
            { this.anchor = n; }
            
        public Fragments( Node n, Triple t )
            {
            this( n );
            addTriple( t ); 
            }
            
        public int size()
            { return subjects.size() + predicates.size() + objects.size() + types.size(); }
        
        /**
            true iff this is a complete fragment; every component is present with exactly
            one value, so n unambiguously reifies (subject, predicate, object).
        */
        public boolean isComplete()
            { return subjects.size() == 1 && predicates.size() == 1 && objects.size() == 1 && types.size() == 1; }
            
        /**
            true iff this is an empty fragment; no reificational assertions have been made
            about n. (Hence, in use, the Fragments object can be discarded.)
        */
        public boolean isEmpty()
            { return subjects.isEmpty() && predicates.isEmpty() && objects.isEmpty() && types.isEmpty(); }
            
        /**
            remove the node n from the set specified by slot which.
        */
        public void remove( SimpleReifierFragmentHandler w, Node n )
            { w.which.get( this ).remove( n ); }
            
        /**
            add the node n to the slot identified by which).
       */
        public void add( SimpleReifierFragmentHandler w, Node n )
            { w.which.get(  this ).add( n ); }
            
        /**
            include into g all of the reification components that this Fragments
            represents.
        */
        public void includeInto( GraphAdd g )
            {
            includeInto( g, RDF.Nodes.subject, SimpleReifierFragmentsMap.SUBJECTS_index );
            includeInto( g, RDF.Nodes.predicate, SimpleReifierFragmentsMap.PREDICATES_index );
            includeInto( g, RDF.Nodes.object, SimpleReifierFragmentsMap.OBJECTS_index );
            includeInto( g, RDF.Nodes.type, SimpleReifierFragmentsMap.TYPES_index );
            }
            
        /**
            include into g all of the (n, p[which], o) triples for which
            o is an element of the slot <code>which</code> corresponding to
            predicate.
        */
        private void includeInto( GraphAdd g, Node predicate, Fragments.GetSlot which )
            {
            Iterator<Node> it = which.get( this ).iterator();
            while (it.hasNext()) g.add( Triple.create( anchor, predicate, it.next() ) );
            }
            
        /**
            add to this Fragments the entire reification quad needed to
            reify the triple t.
            @param t: Triple the (S, P, O) triple to reify
            @return this with the quad for (S, P, O) added
        */
        public Fragments addTriple( Triple t )
            {
            subjects.add( t.getSubject() );
            predicates.add( t.getPredicate() );
            objects.add( t.getObject() );
            types.add( RDF.Nodes.Statement );
            return this;
            }
            
        /** 
            precondition: isComplete() 
        <p>
            return the single Triple that this Fragments represents; only legal if
            isComplete() is true.    
        */        
        Triple asTriple()
            { return Triple.create( only( subjects ), only( predicates ), only( objects ) ); }
                   
        /**
            precondition: s.size() == 1
        <p>
            utiltity method to return the only element of a singleton set.
        */
        private Node only( Set<Node> s )
            { return s.iterator().next(); }
            
        /**
            return a readable representation of this Fragment for debugging purposes.
        */
        @Override public String toString()
            { return anchor + " s:" + subjects + " p:" + predicates + " o:" + objects + " t:" + types; }

        }
