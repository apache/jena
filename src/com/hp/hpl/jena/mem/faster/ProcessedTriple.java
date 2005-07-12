/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ProcessedTriple.java,v 1.5 2005-07-12 15:57:42 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.mem.faster.FasterPatternStage.*;
import com.hp.hpl.jena.mem.faster.ProcessedNode.Bound;
import com.hp.hpl.jena.mem.faster.ProcessedNode.Fixed;
import com.hp.hpl.jena.shared.BrokenException;

/**
    A ProcessedTriple is three ProcessedNodes; it knows how to deliver an
    optimised Matcher which will use only the necessary ProcessedNode.match
    methods.
    
    @author kers
*/
public class ProcessedTriple
    {
    public final ProcessedNode S;
    public final ProcessedNode P;
    public final ProcessedNode O;
    
    public ProcessedTriple( ProcessedNode S, ProcessedNode P, ProcessedNode O ) 
        { this.S = S; this.P = P; this.O = O; }
    
    public String toString()
        { return "<pt " + S.toString() + " " + P.toString() + " " + O.toString() + ">"; }

    public static ProcessedTriple [] allocateBindings( Mapping map, Triple[] triples )
        {
        ProcessedTriple [] result = new ProcessedTriple[triples.length];
        for (int i = 0; i < triples.length; i += 1)
            result[i] = allocateBindings( map, triples[i] );
        return result;
        }

    public static ProcessedTriple allocateBindings( Mapping map, Triple triple )
        {
        Set local = new HashSet();
        return new ProcessedTriple
            (
            ProcessedNode.allocateBindings( map, local, triple.getSubject() ),
            ProcessedNode.allocateBindings( map, local, triple.getPredicate() ),
            ProcessedNode.allocateBindings( map, local, triple.getObject() )
            );
        }
    
    protected Matcher makeMatcher()
        {
        final int SMATCH = 4, PMATCH = 2, OMATCH = 1, NOMATCH = 0;
        int bits = 
            (S.mustMatch() ? SMATCH : 0) 
            + (P.mustMatch() ? PMATCH : 0)
            + (O.mustMatch() ? OMATCH : 0)
            ;
        switch (bits)
            {
            case SMATCH + PMATCH + OMATCH:
                return new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        { return S.match( d, t.getSubject() )
                            && P.match( d, t.getPredicate() )
                            && O.match( d, t.getObject() ); }
                    };
                    
            case SMATCH + OMATCH:
                return new Matcher() 
                    {
                    public boolean match( Domain d, Triple t )
                        { 
                        return S.match( d, t.getSubject() ) 
                        && O.match( d, t.getObject() ); }
                    };
                    
            case SMATCH + PMATCH:  
                return new Matcher() 
                    {
                    public boolean match( Domain d, Triple t )
                        { 
                        return S.match( d, t.getSubject() ) 
                        && P.match( d, t.getPredicate() ); 
                        }
                    };
                    
            case PMATCH + OMATCH:
                return new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        {
                        return P.match( d, t.getPredicate() )
                        && O.match( d, t.getObject() );
                        }
                    };
    
            case SMATCH:                
                return new Matcher() 
                    {
                    public boolean match( Domain d, Triple t )
                        { return S.match( d, t.getSubject() ); }
                    };
    
            case PMATCH:
                return new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        { return P.match( d, t.getPredicate() ); }
                    };
                    
            case OMATCH:
                return new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        { return O.match( d, t.getObject() ); }
                    };
    
            case NOMATCH:
                return Matcher.always;
                    
            }
        throw new BrokenException( "uncatered-for case in optimisation" );
        }
    
    public static abstract class PreindexedFind
        {
        public abstract Iterator find( Node X, Node Y );
        }

    public static abstract class HalfindexedFind
        {
        public abstract Iterator find( Node X, Node Y, Node Z );
        }

    protected Finder finder( GraphMemFaster graph )
        {
        if (S instanceof Fixed) return finderFixedS( graph, S, P, O );
        if (O instanceof Fixed) return finderFixedO( graph, S, P, O );
        if (S instanceof Bound) return finderBoundS( graph, S, P, O );
        if (O instanceof Bound) return finderBoundO( graph, S, P, O );
        // System.err.println( ">> unoptimised finder " + this );
        return finderGeneral( graph, S, P, O );
        }

    protected FasterPatternStage.Finder finderFixedS( final GraphMemFaster graph, final ProcessedNode S, final ProcessedNode P, final ProcessedNode O )
        {
        final ProcessedTriple.PreindexedFind f = graph.findFasterFixedS( S.node );
        return new FasterPatternStage.Finder()
            {
            public Iterator find( Domain current )
                {
                return f.find( P.finder( current ), O.finder( current ) );
                }
            };
        }

    protected FasterPatternStage.Finder finderFixedO( final GraphMemFaster graph, final ProcessedNode S, final ProcessedNode P, final ProcessedNode O )
        {
        final ProcessedTriple.PreindexedFind f = graph.findFasterFixedO( O.node );
        return new FasterPatternStage.Finder()
            {
            public Iterator find( Domain current )
                {
                return f.find( S.finder( current ), P.finder( current ) );
                }
            };
        }

    protected FasterPatternStage.Finder finderBoundS( final GraphMemFaster graph, final ProcessedNode S, final ProcessedNode P, final ProcessedNode O )
        {            
        final ProcessedTriple.HalfindexedFind f = graph.findFasterBoundS();
        return new FasterPatternStage.Finder()
            {
            public Iterator find( Domain current )
                {
                return f.find( S.finder( current ), P.finder( current ), O.finder( current ) );
                }
            };
        }

    protected FasterPatternStage.Finder finderBoundO( final GraphMemFaster graph, final ProcessedNode S, final ProcessedNode P, final ProcessedNode O )
        {
        final ProcessedTriple.HalfindexedFind f = graph.findFasterBoundO();
        return new FasterPatternStage.Finder()
            {
            public Iterator find( Domain current )
                {
                return f.find( S.finder( current ), P.finder( current ), O.finder( current ) );
                }
            };
        }

    protected FasterPatternStage.Finder finderGeneral
        ( final GraphMemFaster graph, final ProcessedNode S, final ProcessedNode P, final ProcessedNode O )
        {
        return new FasterPatternStage.Finder()
            {
            public Iterator find( Domain current )
                {
                return graph.findFaster
                    ( S.finder( current ), P.finder( current ), O.finder( current )  );
                }
            };
        }

    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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