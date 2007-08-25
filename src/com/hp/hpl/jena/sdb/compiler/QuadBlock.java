/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import static com.hp.hpl.jena.sdb.iterator.Streams.apply;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintSerializable;
import com.hp.hpl.jena.sparql.util.PrintUtils;

import com.hp.hpl.jena.sdb.iterator.Action;

public class QuadBlock extends ArrayList<Quad> implements Iterable<Quad>, PrintSerializable
{
    // A nicer QuadPattern
    Node graphNode ;

    public QuadBlock() { super() ; }
    
    public QuadBlock(QuadBlock other)
    { 
        super(other) ;
        this.graphNode = other.graphNode ;
    }

    public QuadBlock(OpQuadPattern quadPattern)
    {
        super() ;
        // Needs two steps to allow the @SuppressWarnings on a statement.
        @SuppressWarnings("unchecked")
        List<Quad> q = quadPattern.getQuads() ;
        this.addAll(q) ;
        graphNode = quadPattern.getGraphNode() ;
    }

    /** Copy with no shared quads */
    @Override
    public QuadBlock clone()
    { 
        return new QuadBlock(this) ;
    }
    
    static private Quad[] qArrayTemplate = new Quad[0] ;
    
    public Quad[] asArray()
    {
        return this.toArray(qArrayTemplate) ;
    }
    
    public Node getGraphNode() { return graphNode ; }

    public boolean contains(Quad pattern)
    { return contains(pattern.getGraph(), pattern.getSubject(), pattern.getPredicate(),pattern.getObject()) ; } 

    public boolean contains(Node g, Node s, Node p, Node o)
    { return findFirst(g, s, p, o) >= 0 ; } 
    
    public int findFirst(Quad pattern)
    {
        return findFirst(0, pattern) ;
    }

    public int findFirst(Node g, Node s, Node p, Node o)
    {
        return findFirst(0, g, s, p, o) ; 
    }
    
    public int findFirst(int start, Quad pattern)
    {
        return findFirst(start, pattern.getGraph(), pattern.getSubject(), pattern.getPredicate(),pattern.getObject()) ;
    }

    public int findFirst(int start, Node g, Node s, Node p, Node o)
    {
        if ( g == Node.ANY ) g = null ;
        if ( s == Node.ANY ) s = null ;
        if ( p == Node.ANY ) p = null ;
        if ( o == Node.ANY ) o = null ;

        for ( int i = start ; i < size() ; i++ )
        {
            Quad q = get(i) ;
            if ( matchOne(g,s,p,o,q) )
                return i ;
        }
        return -1 ;
    }

    public QuadBlock subBlock(int fromIndex, int toIndex)
    {
        QuadBlock slice = new QuadBlock() ;
        slice.addAll(subList(fromIndex, toIndex)) ;
        return slice ;  
    }

    public QuadBlock subBlock(int fromIndex)
    {
        QuadBlock slice = new QuadBlock() ;
        slice.addAll(subList(fromIndex, this.size())) ;
        return slice ;  
    }
    
    public Iterable<Quad> find(Quad pattern)
    {
        return find(pattern.getGraph(), pattern.getSubject(), pattern.getPredicate(),pattern.getObject()) ;
    }

    // Optimized for small quad blocks.

    public Iterable<Quad> find(Node g, Node s, Node p, Node o)
    {
        List<Quad> matches = new ArrayList<Quad>() ;

        if ( g == Node.ANY ) g = null ;
        if ( s == Node.ANY ) s = null ;
        if ( p == Node.ANY ) p = null ;
        if ( o == Node.ANY ) o = null ;

        for ( Quad q : this )
        {
            if ( matchOne(g,s,p,o,q) )
                matches.add(q) ;

        }
        return matches;
    }

    private static boolean matchOne(Node g, Node s, Node p, Node o, Quad q)
    {
        return
        ( p == null || p.equals(q.getPredicate()) )
        &&
        ( s == null || s.equals(q.getSubject()) )
        &&
        ( o == null || o.equals(q.getObject()) ) 
        &&
        ( g == null || g.equals(q.getGraph()) ) ;
    }
    
    @Override
    public String toString()
    { return PrintUtils.toString(this) ; }

    public void output(final IndentedWriter out, SerializationContext sCxt)
    { 
        final String sep = "\n" ;
        
        final Action<Quad> strAction = new Action<Quad>() {
            boolean first = true ; 
            public void apply(Quad quad)
            {
                if ( ! first )
                    out.print(sep) ;
                first = false ;
                out.print(quad.toString()) ;
            } } ;

        apply(this, strAction) ;
    }

    public String toString(PrefixMapping prefixMapping)
    { return PrintUtils.toString(this, prefixMapping) ; }

    public void output(IndentedWriter out)
    { PrintUtils.output(this, out) ; }

}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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