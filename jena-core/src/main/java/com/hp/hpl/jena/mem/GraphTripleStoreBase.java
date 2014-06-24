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

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.util.iterator.*;

public abstract class GraphTripleStoreBase implements TripleStore
    {
    protected final Graph parent;
    protected NodeToTriplesMapBase subjects;
    protected NodeToTriplesMapBase predicates;
    protected NodeToTriplesMapBase objects;
    
    protected GraphTripleStoreBase
        ( Graph parent,
        NodeToTriplesMapBase subjects,
        NodeToTriplesMapBase predicates,
        NodeToTriplesMapBase objects
        )
        { 
        this.parent = parent; 
        this.subjects = subjects; this.objects = objects; this.predicates = predicates;
        }   
    
    /**
        Destroy this triple store - discard the indexes.
    */
     @Override
    public void close()
         { subjects = predicates = objects = null; }
     
     /**
          Add a triple to this triple store.
     */
     @Override
    public void add( Triple t )
         {
         if (subjects.add( t ))
             {
             predicates.add( t );
             objects.add( t ); 
             }
         }
     
     /**
          Remove a triple from this triple store.
     */
     @Override
    public void delete( Triple t )
         {
         if (subjects.remove( t ))
             {
             predicates.remove( t );
             objects.remove( t ); 
             }
         }
     
     /**
          Clear this store, ie remove all triples from it.
     */
     @Override
    public void clear()
         {
         subjects.clear();
         predicates.clear();
         objects.clear();
         }

     /**
          Answer the size (number of triples) of this triple store.
     */
     @Override
    public int size()
         { return subjects.size(); }
     
     /**
          Answer true iff this triple store is empty.
     */
     @Override
    public boolean isEmpty()
         { return subjects.isEmpty(); }
     
     @Override
    public ExtendedIterator<Node> listSubjects()
         { return expectOnlyNodes( subjects.domain() ); }
     
     @Override
    public ExtendedIterator<Node> listPredicates()
         { return expectOnlyNodes( predicates.domain() ); }
    
     private ExtendedIterator<Node> expectOnlyNodes( Iterator<Object> elements )
        { return WrappedIterator.createNoRemove( elements ).mapWith( expectNode ); }
     
     private static final Map1<Object, Node> expectNode = new Map1<Object, Node>()
         { @Override
        public Node map1( Object o ) { return (Node) o; }};
     
     @Override
    public ExtendedIterator<Node> listObjects()
         {
         return new ObjectIterator( objects.domain() )
             {
             @Override protected Iterator <Triple>iteratorFor( Object y )
                 { return objects.iteratorForIndexed( y ); }
             };
         }
     
     /**
          Answer true iff this triple store contains the (concrete) triple <code>t</code>.
     */
     @Override
    public boolean contains( Triple t )
         { return subjects.containsBySameValueAs( t ); }
     
     public boolean containsByEquality( Triple t )
         { return subjects.contains( t ); }
     
     /** 
         Answer an ExtendedIterator returning all the triples from this store that
         match the pattern <code>m = (S, P, O)</code>.
         
         <p>Because the node-to-triples maps index on each of subject, predicate,
         and (non-literal) object, concrete S/P/O patterns can immediately select
         an appropriate map. Because the match for literals must be by sameValueAs,
         not equality, the optimisation is not applied for literals. [This is probably a
         Bad Thing for strings.]
         
         <p>Practice suggests doing the predicate test <i>last</i>, because there are
         "usually" many more statements than predicates, so the predicate doesn't
         cut down the search space very much. By "practice suggests" I mean that
         when the order went, accidentally, from S/O/P to S/P/O, performance on
         (ANY, P, O) searches on largish models with few predicates declined
         dramatically - specifically on the not-galen.owl ontology.
     */
     @Override
    public ExtendedIterator<Triple> find( TripleMatch tm )
         {
         Triple t = tm.asTriple();
         Node pm = t.getPredicate();
         Node om = t.getObject();
         Node sm = t.getSubject();
             
         if (sm.isConcrete())
             return new StoreTripleIterator( parent, subjects.iterator( sm, pm, om ), subjects, predicates, objects );
         else if (om.isConcrete())
             return new StoreTripleIterator( parent, objects.iterator( om, sm, pm ), objects, subjects, predicates );
         else if (pm.isConcrete())
             return new StoreTripleIterator( parent, predicates.iterator( pm, om, sm ), predicates, subjects, objects );
         else
             return new StoreTripleIterator( parent, subjects.iterateAll(), subjects, predicates, objects );
         }
    }
