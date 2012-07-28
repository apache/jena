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
    A ConstraintStage implements the constraint evaluation part of a
    query. Any constraints not handled by previous PatternStages are prepared
    against the mapping and valuated against each binding that comes down
    the pipe; only bindings that evaluate to <code>true</code> are passed onward.

*/

public class ConstraintStage extends Stage
    {
    /**
        The set of prepared Valuators representing the constraint.
    */
    protected ValuatorSet prepared;

    /**
        Initialise this ConstraintStage with the mapping [from names to indexes] and
        ExpressionSet [the constraint expressions] that will be evaluated when the
        constraint stage runs.
    */
    public ConstraintStage( Mapping map, ExpressionSet constraint )
        { this.prepared = constraint.prepare( map ); }

    /**
        Evaluate the prepared constraints with the values given by the domain.
        Answer true if the constraint evaluates to true, and false if it evaluates to
        false or throws an exception.
    */
   private boolean evalConstraint( Domain d, ValuatorSet e )
        { try 
            { return e.evalBool( d ); } 
        catch (Exception ex) 
            { ex.printStackTrace( System.err ); return false; } }
        
    /**
        the delivery component: read the domain elements out of the
        input pipe, and only pass on those that satisfy the predicate.
    */
    @Override
    public Pipe deliver( final Pipe L )
        {
        final Pipe mine = previous.deliver( new BufferPipe() );
        new Thread( "a ConstraintStage" )
        	{
        	@Override
            public void run()
        		{
		        while (mine.hasNext())
		            { Domain d = mine.get();
		            if (evalConstraint( d, prepared )) L.put( d ); }
		        L.close();
        		}
        	} .start();
        return L;
        }
    }
