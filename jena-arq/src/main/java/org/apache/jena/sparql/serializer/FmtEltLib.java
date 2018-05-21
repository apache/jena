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

package org.apache.jena.sparql.serializer;

import static org.apache.jena.graph.Node.ANY;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.vocabulary.RDF;

/** Place to move some of the code from FormatterElement */ 
class FmtEltLib {
    
    /*package*/ static Node rdfFirst = RDF.Nodes.first;
    /*package*/ static Node rdfRest  = RDF.Nodes.rest;
    /*package*/ static Node rdfNil   = RDF.Nodes.nil;

    // Cautious list finder.  
    //  The list must be like the parser creates them:
    //   Adjacent triples, in first, rest order.

    // It does support embedded lists.
    // It does not support bnode structures i.e. [:prop :obj]
    
    // Ths code simply finds lists - it does not perform checks as to their suitablity to print in ()-form.
    
    /*package*/ static TriplesListBlock createTriplesListBlock(BasicPattern bgp) {
        TriplesListBlock tlb = new TriplesListBlock();
        List<Triple> triples = bgp.getList();
        for ( int idx = 0 ; idx < triples.size() ; idx++ ) {
            Triple t = triples.get(idx);
            if ( idx == triples.size() - 1 )
                // Can't be a following triple.
                break;
            // null -> ANY
            if ( matches(t, ANY, rdfFirst, ANY) ) {
                Node consCell = t.getSubject();
                int numTriples = collectList(consCell, idx, triples, tlb);
                if ( numTriples > 0 ) {
                    // Skip triples.
                    idx = idx + numTriples - 1 ;
                } else {
                    // Play safe
                    // skip to (? rdf:rest rdf:nil) (if any).
                    for ( idx = idx + 1 ; idx < triples.size() ; idx++ ) {
                        Triple t2 = triples.get(idx);
                        if ( matches(t2, ANY, rdfRest, rdfNil) ) 
                            break;
                    }
                }
            }
        }
        return tlb;
    }
    
    private static Node nullAsAny(Node n) {
        return n == null ? ANY : n ; 
    }

    /*package*/ static boolean matches(Triple t, Node s, Node p, Node o) {
        s = nullAsAny(s) ;
        p = nullAsAny(p) ;
        o = nullAsAny(o) ;
        if ( s != ANY && ! Objects.equals(s, t.getSubject()) )
            return false ;
        if ( p != ANY && ! Objects.equals(p, t.getPredicate()) )
            return false ;
        if ( o != ANY && ! Objects.equals(o, t.getObject()) )
            return false ;
        return true ;
    }

    /*package*/ static int collectList(Node consCell, int idx, List<Triple> triples, TriplesListBlock tlb) {
        Set<Triple> listTriples = new LinkedHashSet<>();
        TriplesListBlock block1 = collectList1(consCell, idx, triples, listTriples, tlb);
        if ( block1 == null )
            // Failed.
            return -1;
        if ( ! FormatterElement.FMT_FREE_STANDING_LISTS ) {
            // Reject free standiang lists.
            int inCount = count(triples, ANY, ANY, consCell);
            int outCount = count(triples, consCell, ANY, ANY);
            if ( inCount == 0 && outCount == 2 )
                return -1;
        }

        int numTriples = block1.triplesInLists.size();
        tlb.merge(block1);
        return numTriples;
    }

    /**
     * Spot parser pattern of adjacent "first-rest" pairs.
     * Collect elements of a well-formed list else null.
     * {@code triplesInList}.
     */
    /*package*/ static TriplesListBlock collectList1(Node consCell, int idx, List<Triple> triples, Set<Triple> triplesInList, TriplesListBlock tlb) {
        // This list - accumulate separately because we aren't sure it is well-formed yet.
        TriplesListBlock thisList = new TriplesListBlock();
        List<Node> elts = new ArrayList<>();
        thisList.listElementsMap.put(consCell, elts);
        
        for ( ;; ) {
            if ( idx + 1 >= triples.size() )
                // Last triple - can't be an rdf:first, rdf:rest pair.
                return null;
            Triple t1 = triples.get(idx);
            consCell = t1.getSubject();
            
            Triple t2 = triples.get(idx + 1);
            
            // -- Checks on t1
            // t1 : (consCell rdf:first element) 
            if ( ! matches(t1, consCell, rdfFirst, ANY) )
                return null;
            
            // ---- Possible compound value. 
            // Second triple is rdf:rest, or rdf:first for a list in a list.
            // or arbitrary triples for [:p :q] in a list.  
            // We don't handle the latter case because programatic can make this anything.  

            final boolean ListsInLists = true ;
            if ( ListsInLists ) {
                if ( rdfFirst.equals(t2.getPredicate()) && t1.getObject().equals(t2.getSubject()) ) {
                    // Recursion.
                    int numProcessed = collectList(t2.getSubject(), idx + 1, triples, thisList); // -1
                    if ( numProcessed < 0 )
                        return null;
                    // Not "-1" - this loop does not have autoincrement.  
                    idx = idx + numProcessed ;
                    // idx: Posn of the rdf:nil. Probe to see if t2 is an "rdf:rest" to consider.
                    t2 = triples.get(idx + 1);
                }
            }
            
            // -- Checks on t2
            // t2 : (consCell rdf:rest element)
            if ( ! matches(t2, consCell, rdfRest, ANY) )
                return null;
            // -- Check consCell - no other triples or one if a subject list.
            int outCount = count(triples, consCell, ANY, ANY) ;
            if ( outCount != 2 ) {
                // Head cell also be a subject list. in which case the first cell of the list can have a count of 3. 
                if ( outCount == 3 && ! elts.isEmpty() ) 
                    return null;
            }
                
            
            int inCount = count(triples, ANY, ANY, consCell) ;
            if ( inCount != 1 ) {
                // Head cell can also be zero : subject or free standing list head.
                if ( outCount == 0 && ! elts.isEmpty() ) 
                    return null;
            }
                
            
            Node elt = t1.getObject();
            thisList.triplesInLists.add(t1);
            thisList.triplesInLists.add(t2);
            elts.add(elt);
            if ( matches(t2, ANY, ANY, rdfNil) ) {
                return thisList;
            }
            idx += 2;
        }
    }
    
    /*package*/ static int count(List<Triple> triples, Node s, Node p, Node o) {
        int count = 0 ;
        for ( Triple t : triples ) {
            if ( matches(t, s, p, o) )
                count++;
        }
        return count;
    }
}
