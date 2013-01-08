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

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.graph.impl.TripleStore ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class GraphMem extends GraphMemBase
{
    public GraphMem()
    { super(  ); }

    @Override protected TripleStore createTripleStore()
    { return new GraphTripleStoreMem( this ); }

    @Override protected void destroy()
    { store.close(); }

    @Override public void performAdd( Triple t )
    { store.add( t ); }

    @Override public void performDelete( Triple t )
    { store.delete( t ); }

    @Override public int graphBaseSize()  
    { return store.size(); }

    @Override protected GraphStatisticsHandler createStatisticsHandler()
    { return new GraphMemStatisticsHandler( (GraphTripleStoreMem) store ); }

    /**
        The GraphMemStatisticsHandler exploits the existing TripleStoreMem
        indexes to deliver statistics information for single-concrete-node queries
        and for trivial cases of two-concrete-node queries.        
     */
    protected static class GraphMemStatisticsHandler implements GraphStatisticsHandler
    {
        protected final GraphTripleStoreMem store;

        public GraphMemStatisticsHandler( GraphTripleStoreMem store )
        { this.store = store; }

        private static class C 
        {
            static final int NONE = 0;
            static final int S = 1, P = 2, O = 4;
            static final int SP = S + P, SO = S + O, PO = P + O;
            static final int SPO = S + P + O;
        }

        /**
            Answer a good estimate of the number of triples matching (S, P, O)
            if cheaply possible.

            <p>If there are any reifier triples, return -1. (We may be able to
            improve this later.)

            <p>If only one of S, P, O is concrete, answers the number of triples
            with that value in that field.

            <p>If two of S, P, P are concrete and at least one of them has no
            corresponding triples, answers 0.

            <p>Otherwise answers -1, ie, no information available. (May change;
            the two degenerate cases might deserve an answer.)

         	@see com.hp.hpl.jena.graph.GraphStatisticsHandler#getStatistic(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
         */
        @Override
        public long getStatistic( Node S, Node P, Node O )
        {
            int concrete = (S.isConcrete() ? C.S : 0) + (P.isConcrete() ? C.P : 0) + (O.isConcrete() ? C.O : 0);
            switch (concrete)
            {
                case C.NONE:
                    return store.size();

                case C.S:
                    return countInMap( S, store.getSubjects() );

                case C.SP:
                    return countsInMap( S, store.getSubjects(), P, store.getPredicates() );

                case C.SO:
                    return countsInMap( S, store.getSubjects(), O, store.getObjects() );

                case C.P:
                    return countInMap( P, store.getPredicates() );

                case C.PO:
                    return countsInMap( P, store.getPredicates(), O, store.getObjects() );

                case C.O:
                    return countInMap( O, store.getObjects() );

                case C.SPO:
                    return store.contains( Triple.create( S, P, O ) ) ? 1 : 0;
            }
            return -1;
        }

        public long countsInMap( Node a, NodeToTriplesMapMem mapA, Node b, NodeToTriplesMapMem mapB )
        {
            long countA = countInMap( a, mapA ), countB = countInMap( b, mapB );
            return countA == 0 || countB == 0 ? 0 : -1L;
        }

        public long countInMap( Node n, NodeToTriplesMapMem map )
        {
            TripleBunch b = map.get( n.getIndexingValue() );
            return b == null ? 0 : b.size();
        }
    }

    /**
         Answer an ExtendedIterator over all the triples in this graph that match the
         triple-pattern <code>m</code>. Delegated to the store.
     */
    @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch m ) 
    { return store.find( m.asTriple() ); }

    /**
         Answer true iff this graph contains <code>t</code>. If <code>t</code>
         happens to be concrete, then we hand responsibility over to the store.
         Otherwise we use the default implementation.
     */
    @Override public boolean graphBaseContains( Triple t )
    { return t.isConcrete() ? store.contains( t ) : super.graphBaseContains( t ); }

    /**
        Clear this GraphMem, ie remove all its triples (delegated to the store).
     */
    @Override public void clear()
    { 
        clearStore(); 
        getEventManager().notifyEvent(this, GraphEvents.removeAll ) ;   
    }
    
    /**
    Clear this GraphMem, ie remove all its triples (delegated to the store).
     */
    public void clearStore()
    { 
        store.clear();
    }
    
}
