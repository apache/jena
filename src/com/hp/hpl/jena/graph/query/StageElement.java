/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: StageElement.java,v 1.4 2005-07-26 14:26:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.query.PatternStageBase.Applyer;

/**
    Class used internally by PatternStage to express the notion of "the
    runnable next component in this stage".
    @author kers
*/
public abstract class StageElement
    {
    public abstract void run( Domain current );

    /**
        A PutBindings is created with a domain sink and, whenever it is run,
        puts a copy of the current domain down the sink.
    */
    public static final class PutBindings extends StageElement
        {
        protected final Pipe sink;
        
        public PutBindings( Pipe sink )
            { this.sink = sink; }
        
        public final void run( Domain current )
            { sink.put( current.copy() ); }
        }
    
    
    /**
        A FindTriples runs match-and-next over all the triples returned
        by its finder.   
    */
    public static final class FindTriples extends StageElement
        {
        protected final Matcher matcher;
        protected final Applyer finder;
        protected final StageElement next;
        protected final Stage stage;
        
        public FindTriples( Stage stage, Matcher matcher, Applyer finder, StageElement next )
            { this.stage = stage;  this.matcher = matcher; this.finder = finder; this.next = next; }
    
        public final void run( Domain current )
            { if (stage.stillOpen) finder.applyToTriples( current, matcher, next ); }
        }

    /**
        A RunValuatorSet is created with a ValuatorSet and a next StageElement;
        whenever it is run, it evaluates the ValuatorSet and only if that 
        answers true does it run the next StageElement.
    */
    public static final class RunValuatorSet extends StageElement
        {
        protected final ValuatorSet s;
        protected final StageElement next;
        
        public RunValuatorSet( ValuatorSet s, StageElement next )
            { this.s = s; this.next = next; }
        
        public final void run( Domain current )
            { if (s.evalBool( current )) next.run( current ); }
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