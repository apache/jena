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

package com.hp.hpl.jena.graph.query;


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
        
        @Override
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
    
        @Override
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
        
        @Override
        public final void run( Domain current )
            { if (s.evalBool( current )) next.run( current ); }
        }
    }
