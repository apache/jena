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

package com.hp.hpl.jena.sdb.compiler;

import static org.apache.jena.atlas.iterator.Iter.apply ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.PrintUtils ;
import org.apache.jena.atlas.iterator.Action ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.PrintSerializable ;
import com.hp.hpl.jena.sparql.util.QueryOutputUtils ;

public class QuadBlock extends ArrayList<Quad> implements Iterable<Quad>, PrintSerializable
{
    // Pre-dates QuadPattern
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
        this.addAll(quadPattern.getPattern().getList()) ;
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

    @Override
    public void output(final IndentedWriter out, SerializationContext sCxt)
    { 
        final String sep = "\n" ;

        final Action<Quad> strAction = new Action<Quad>() {
            boolean first = true ; 
            @Override
            public void apply(Quad quad)
            {
                if ( ! first )
                    out.print(sep) ;
                first = false ;
                out.print(String.valueOf(quad)) ;
            } } ;

            apply(this, strAction) ;
    }

    @Override
    public String toString(PrefixMapping prefixMapping)
    { return QueryOutputUtils.toString(this, prefixMapping) ; }

    @Override
    public void output(IndentedWriter out)
    { 
        String x = QueryOutputUtils.toString(this) ;
        out.print(x) ;
    }

}
