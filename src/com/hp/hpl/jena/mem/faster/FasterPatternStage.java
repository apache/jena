/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: FasterPatternStage.java,v 1.18 2005-07-22 22:08:23 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

public class FasterPatternStage extends PatternStageBase
    {
    protected GraphMemFaster graph;
    protected ProcessedTriple [] processed;
    
    protected abstract static class Finder
        {   
        public abstract Iterator find( Domain d );
        }
    
    public FasterPatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple [] triples )
        {
        this.graph = (GraphMemFaster) graph;
        this.processed = ProcessedTriple.allocateBindings( map, triples );
        setGuards( map, constraints, triples );
        }
    
    protected StageElement makeStageElementChain( Pipe sink, int index )
        {
        if (index == processed.length)
            return new StageElement.PutBindings( sink );
        else
            {
            Matcher m = processed[index].makeMatcher();
            Finder f = processed[index].finder( graph );
            ValuatorSet s = guards[index];
            StageElement next = makeStageElementChain( sink, index + 1 );
            return new FindTriples( m, f, s.isNonTrivial() ? new StageElement.RunValuatorSet( s, next ) : next );
            }
        }
    
    protected final class FindTriples extends StageElement
        {
        protected final Matcher matcher;
        protected final Finder finder;
        protected final StageElement next;
        
        public FindTriples( Matcher matcher, Finder finder, StageElement next )
            { this.matcher = matcher; this.finder = finder; this.next = next; }
        
        public final void run( Domain current )
            {
            Iterator it = finder.find( current );
            while (stillOpen && it.hasNext())
                if (matcher.match( current, (Triple) it.next() )) 
                    next.run( current );
            }
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