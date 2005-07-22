/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PatternStage.java,v 1.28 2005-07-22 22:04:19 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.BrokenException;

import java.util.*;

/**
    A PatternStage is a Stage that handles some bunch of related patterns; those patterns
    are encoded as Triples.
    
    @author hedgehog
*/

public class PatternStage extends PatternStageBase
    {
    protected Graph graph;
    protected Pattern [] compiled;
    
    public PatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple [] triples )
        {
        this.graph = graph;
        this.compiled = compile( map, triples );
        setGuards( map, constraints, triples );
        }

    protected Pattern [] compile( Mapping map, Triple [] triples )
        { return compile( compiler, map, triples ); }
        
    protected Pattern [] compile( PatternCompiler pc, Mapping map, Triple [] source )
        { return PatternStageCompiler.compile( pc, map, source ); }
        
    private static final PatternCompiler compiler = new PatternStageCompiler();
        
    protected StageElement makeStageElementChain( Pipe sink, int index )
        {
        if (index == compiled.length)
            return new StageElement.PutBindings( sink );
        else
            {
            Pattern p = compiled[index];
            ValuatorSet s = guards[index];
            StageElement nextElement = makeStageElementChain( sink, index + 1 );
            StageElement next = s.isNonTrivial() 
                ? new StageElement.RunValuatorSet( s, nextElement ) 
                : nextElement
                ;
            return new FindTriples( p, next );
            }
        }    

    protected boolean mustMatch( Element e )
        { return e instanceof Bind || e instanceof Bound; }
    
    protected Matcher makeMatcher( Pattern p )
        {
        final Element S = p.S, P = p.P, O = p.O;
        final int SMATCH = 4, PMATCH = 2, OMATCH = 1, NOMATCH = 0;
        int bits = 
            (mustMatch( S ) ? SMATCH : 0) 
            + (mustMatch( P ) ? PMATCH : 0)
            + (mustMatch( O ) ? OMATCH : 0)
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
    
    protected final class FindTriples extends StageElement
        {
        protected final Pattern p;
        protected final Matcher m;
        protected final StageElement next;
        
        public FindTriples( Pattern p, StageElement next )
            { this.p = p; this.next = next; this.m = makeMatcher( p ); }
        
        public final void run( Domain current )
            {
            Triple findPattern = p.asTripleMatch( current ).asTriple();
            Iterator it = graph.find( findPattern );
            while (stillOpen && it.hasNext())
                if (m.match( current, (Triple) it.next() )) 
                    next.run( current );
            }
        }  

     
    }

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
