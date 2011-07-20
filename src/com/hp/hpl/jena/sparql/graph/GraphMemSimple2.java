/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.graph;

import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.impl.SimpleEventManager ;
import com.hp.hpl.jena.graph.query.QueryHandler ;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Filter ;

public class GraphMemSimple2 extends GraphBase2
{
    private Set<Triple> triples = new HashSet<Triple>() ;
    
    public GraphMemSimple2() {}
    
    @Override
    public void performAdd( Triple t )
    { triples.add(t) ; }

    @Override
    public void performDelete( Triple t ) 
    { triples.remove(t) ; }
    
    @Override
    public QueryHandler queryHandler()
    {
        return new SimpleQueryHandler(this) ;
    }

    @Override
    protected PrefixMapping createPrefixMapping()
    {
        return new PrefixMappingImpl() ;
    }

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
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
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
            return tripleContained(tMatch, t) ;
        }
        
    }
    
}
/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */