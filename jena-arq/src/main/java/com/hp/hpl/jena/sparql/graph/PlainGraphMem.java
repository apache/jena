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

package com.hp.hpl.jena.sparql.graph;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.impl.SimpleEventManager ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Filter ;

/** A lightweight implementation of graph that uses syntactic identity
 * for find(), that is, .equals(), not .sameValueAs(), and also compares
 * language tags canonically (as lowercase).
 * 
 * Suitable for small graph only (no indexing of s/p/o) */

public class PlainGraphMem extends SmallGraphMem
{
    public PlainGraphMem() {}
    
    // In a normal memory graph, 
    // TripleMatchFilter uses
    //   Triple.matches uses
    //     Node.matches uses
    //       Literal.matches uses 
    //         sameValueAs
    // This version uses equalsNode
    
    // @Override
    @Override
    public boolean graphBaseContains( Triple t ) 
    {
        if ( t.isConcrete() )
            return triples.contains( t ) ;
        
        ClosableIterator<Triple> it = find( t );
        try {
            for ( ; it.hasNext() ; )
            {
                Triple t2 = it.next() ;
                if ( tripleContained(t, t2) )
                    return true ;
            }
        } finally { it.close(); }
        return false ;
    }
    
    @Override
    public ExtendedIterator<Triple> graphBaseFind( TripleMatch m ) 
    {
        Iterator<Triple> iter = triples.iterator() ;
        return 
            SimpleEventManager.notifyingRemove( this, iter ) 
            .filterKeep ( new TripleMatchFilterEquality( m.asTriple() ) );
    }
    
    static boolean tripleContained(Triple patternTriple, Triple dataTriple)
    {
        return
            equalNode(patternTriple.getSubject(),   dataTriple.getSubject()) &&
            equalNode(patternTriple.getPredicate(), dataTriple.getPredicate()) &&
            equalNode(patternTriple.getObject(),    dataTriple.getObject()) ;
    }
    
    private static boolean equalNode(Node m, Node n)
    {
        // m should not be null unless .getMatchXXXX used to get the node.
        // Language tag canonicalization
        n = fixupNode(n) ;
        m = fixupNode(m) ;
        return (m==null) || (m == Node.ANY) || m.equals(n) ;
    }
    
    private static Node fixupNode(Node node)
    {
        if ( node == null || node == Node.ANY )
            return node ;

        // RDF says ... language tags should be canonicalized to lower case.
        if ( node.isLiteral() )
        {
            String lang = node.getLiteralLanguage() ;
            if ( lang != null && ! lang.equals("") )
                node = Node.createLiteral(node.getLiteralLexicalForm(),
                                          lang.toLowerCase(),
                                          node.getLiteralDatatype()) ;
        }
        return node ; 
    }
    
    static class TripleMatchFilterEquality extends Filter<Triple>
    {
        final protected Triple tMatch;
    
        /** Creates new TripleMatchFilter */
        public TripleMatchFilterEquality(Triple tMatch) 
            { this.tMatch = tMatch; }
        
        @Override
        public boolean accept(Triple t)
        {
            return PlainGraphMem.tripleContained(tMatch, t) ;
        }
        
    }
}
