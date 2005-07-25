/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ProcessedTriple.java,v 1.10 2005-07-25 23:07:31 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.query.PatternStageBase.Finder;

/**
    A ProcessedTriple is three QueryNodes; it knows how to deliver an
    optimised Matcher which will use only the necessary QueryNode.match
    methods.
    
    @author kers
*/
public class ProcessedTriple extends QueryTriple
    {    
    public ProcessedTriple( QueryNode S, QueryNode P, QueryNode O ) 
        { super( S, P, O ); }

    static final QueryNodeFactory factory = new QueryNodeFactoryBase()
        {
        public QueryTriple createTriple( QueryNode S, QueryNode P, QueryNode O )
            { return new ProcessedTriple( S, P, O ); }
        
        public QueryTriple [] createArray( int size )
            { return new ProcessedTriple[size]; }
        };
    
    public static ProcessedTriple [] classify( Mapping map, Triple[] triples )
        { 
        return (ProcessedTriple []) QueryTriple.classify( factory, map, triples );
        }
    
    public static abstract class PreindexedFind
        {
        public abstract Iterator find( Node X, Node Y );
        }

    public static abstract class HalfindexedFind
        {
        public abstract Iterator find( Node X, Node Y, Node Z );
        }

    public Finder finder( Graph g )
        {
        GraphMemFaster graph = (GraphMemFaster) g;
        if (S instanceof QueryNode.Fixed) return finderFixedS( graph, S, P, O );
        if (O instanceof QueryNode.Fixed) return finderFixedO( graph, S, P, O );
        if (S instanceof QueryNode.Bound) return finderBoundS( graph, S, P, O );
        if (O instanceof QueryNode.Bound) return finderBoundO( graph, S, P, O );
        // System.err.println( ">> unoptimised finder " + this );
        return finderGeneral( graph, S, P, O );
        }

    protected FasterPatternStage.Finder finderFixedS( final GraphMemFaster graph, final QueryNode S, final QueryNode P, final QueryNode O )
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

    protected FasterPatternStage.Finder finderFixedO( final GraphMemFaster graph, final QueryNode S, final QueryNode P, final QueryNode O )
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

    protected FasterPatternStage.Finder finderBoundS( final GraphMemFaster graph, final QueryNode S, final QueryNode P, final QueryNode O )
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

    protected FasterPatternStage.Finder finderBoundO( final GraphMemFaster graph, final QueryNode S, final QueryNode P, final QueryNode O )
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
        ( final GraphMemFaster graph, final QueryNode S, final QueryNode P, final QueryNode O )
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