/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: ExpressionSet.java,v 1.13 2004-12-06 13:50:13 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import java.util.*;

import com.hp.hpl.jena.util.CollectionFactory;

/**
	ExpressionSet: represent a set of (boolean) expressions ANDed together.

	@author kers
*/
public class ExpressionSet 
    {
    private Set expressions = CollectionFactory.createHashedSet();
    /**
        Initialise an expression set with no members.
    */
	public ExpressionSet() 
        {}
    
    /**
        Answer this expressionset after e has been anded into it.
     	@param e the expression to and into the set
     	@return this ExpressionSet
    */
    public ExpressionSet add( Expression e )
        {
        expressions.add( e );
        return this;    
        }

    /**
         Answer true iff this ExpressionSet is non-trivial (ie non-empty).
    */
    public boolean isComplex()
        { return !expressions.isEmpty(); }
    
//    /**
//        Evaluate this expression set, delivering true iff no member of the set evaluates
//        to false.
//        
//     	@param vv the mapping from variables to values
//     	@return true iff no member evaluates to false
//     */    
//    public boolean evalBool( VariableValues vv )
//        { 
//        Iterator it = expressions.iterator();
//        while (it.hasNext()) 
//            if (((Expression) it.next()).evalBool( vv ) == false) return false;
//        return true;
//        }

    /**
         Answer a ValuatorSet which contains exactly the valuators for each
         Expression in this ExpressionSet, prepared against the VariableIndexes vi.
    */
    public ValuatorSet prepare( VariableIndexes vi )
        {
        ValuatorSet result = new ValuatorSet();
        Iterator it = expressions.iterator();
        while (it.hasNext()) result.add( ((Expression) it.next()).prepare( vi ) );
        return result;    
        }
    
    /**
         Answer an iterator over all the Expressions in this ExpressionSet.
    */
    public Iterator iterator()
        { return expressions.iterator(); }
    
    /**
         Answer a string representing this ExpressionSet for human consumption.
    */
    public String toString()
        { return expressions.toString(); }
    }

/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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