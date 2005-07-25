/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PatternStage.java,v 1.30 2005-07-25 11:14:47 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
    A PatternStage is a Stage that handles some bunch of related patterns; those patterns
    are encoded as Triples.
    
    @author hedgehog
*/

public class PatternStage extends PatternStageBase
    {
    protected Graph graph;
    protected QueryTriple [] compiled;
    
    public PatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple [] triples )
        {
        this.graph = graph;
        this.compiled = QueryTriple.classify( getFactory(), map, triples );
        setGuards( map, constraints, triples );
        }

    protected QueryNodeFactory getFactory()
        { return QueryNode.factory; }
        
    protected StageElement makeStageElementChain( Pipe sink, int index )
        {
        if (index == compiled.length)
            return new StageElement.PutBindings( sink );
        else
            {
            QueryTriple p = compiled[index];
            ValuatorSet s = guards[index];
            StageElement nextElement = makeStageElementChain( sink, index + 1 );
            StageElement next = s.isNonTrivial() 
                ? new StageElement.RunValuatorSet( s, nextElement ) 
                : nextElement
                ;
            return new FindTriples( p, next );
            }
        }    
    
    protected final class FindTriples extends StageElement
        {
        protected final QueryTriple p;
        protected final Matcher m;
        protected final StageElement next;
        
        public FindTriples( QueryTriple p, StageElement next )
            { this.p = p; this.next = next; this.m = p.createMatcher(); }
        
        public final void run( Domain current )
            {
            Triple toFind = Triple.create( p.S.finder( current ), p.P.finder( current ), p.O.finder( current )  );
            Iterator it = graph.find( toFind );
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
