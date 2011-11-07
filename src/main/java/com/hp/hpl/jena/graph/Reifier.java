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

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    This interface represents the type of things that can hold reified triples
    for a Jena Graph.
    
    @author kers
*/

public interface Reifier extends GetTriple
    {
    /**
         Answer an iterator over all the reification triples in this Reifier that match
         <code>m</code>.
    */
    ExtendedIterator<Triple> find( TripleMatch m );
    
    /**
         Answer an iterator over all the reification triples that this Reifier exposes
         (ie all if Standard, none otherwise) that match m.
    */
    ExtendedIterator<Triple> findExposed( TripleMatch m );
    
    /**
         Answer an iterator over the reification triples of this Reifier, or an empty 
         iterator - if showHidden is false, only the exposed triples, otherwise only
         the concealed ones.
    */
    ExtendedIterator<Triple> findEither( TripleMatch m, boolean showHidden );
    
    /**
         Answer the number of exposed reification quadlets held in this reifier.
    */
    int size();
    
    /**
        Answer this reifier's style.
    */
    ReificationStyle getStyle();
    
    /**
        get the Graph which uses this reifier.
    */
    Graph getParentGraph();
    
    /**
        note the triple _t_ as reified using _n_ as its representing node.
        If _n_ is already reifying something, a AlreadyReifiedException is thrown.
    */
    Node reifyAs( Node n, Triple t );
    
    /**
        true iff _n_ is associated with some triple.
    */
    boolean hasTriple( Node n );
    
    /**
        @return true iff there's > 0 mappings to this triple
    */
    boolean hasTriple( Triple t );
    
    /**
        return an iterator over all the nodes that are reifiying something in 
        this reifier.
    */
    ExtendedIterator<Node> allNodes();
    
    /**
        return an iterator over all the nodes that are reifiying t in 
        this reifier.
    */    
    ExtendedIterator<Node> allNodes( Triple t );
    
    /**
        remove any existing binding for _n_; hasNode(n) will return false
        and getTriple(n) will return null. This only removes *unique, single* bindings.
    */
    void remove( Node n, Triple t );
    
    /**
        remove all bindings which map to this triple.
    */
    void remove( Triple t );
    
    /**
        true iff the Reifier has handled an add of the triple _t_.
    */
    boolean handledAdd( Triple t );
    
    /**
        true iff the Reifier has handled a remove of the triple _t_.
    */
    boolean handledRemove( Triple t );

    /**
    	The reifier will no longer be used. Further operations on it are not defined
        by this interface.
    */
    void close();
    
    public static class Util
        {
        public static boolean isReificationPredicate( Node node )
            {
            return 
                node.equals( RDF.Nodes.subject )
                || node.equals( RDF.Nodes.predicate )
                || node.equals( RDF.Nodes.object )
                ;
            }

        public static boolean couldBeStatement( Node node )
            {
            return
                node.isVariable()
                || node.equals( Node.ANY )
                || node.equals( RDF.Nodes.Statement )
                ;
            }

        public static boolean isReificationType( Node P, Node O )
            { return P.equals( RDF.Nodes.type ) && couldBeStatement( O ); }
        }
    }
