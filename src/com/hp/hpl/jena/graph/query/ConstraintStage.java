/*
  (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ConstraintStage.java,v 1.17 2004-12-06 13:50:13 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

/**
    A ConstraintStage implements the constraint evaluation part of a
    query. Any constraints not handled by previous PatternStages are prepared
    against the mapping and valuated against each binding that comes down
    the pipe; only bindings that evaluate to <code>true</code> are passed onward.
    
    @author kers
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
    public Pipe deliver( final Pipe L )
        {
        final Pipe mine = previous.deliver( new BufferPipe() );
        new Thread( "a ConstraintStage" )
        	{
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

/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
