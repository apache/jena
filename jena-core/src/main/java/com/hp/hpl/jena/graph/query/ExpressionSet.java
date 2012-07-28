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

import java.util.*;

import com.hp.hpl.jena.util.CollectionFactory;

/**
	ExpressionSet: represent a set of (boolean) expressions ANDed together.

*/
public class ExpressionSet 
    {
    private Set<Expression> expressions = CollectionFactory.createHashedSet();
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

    /**
         Answer a ValuatorSet which contains exactly the valuators for each
         Expression in this ExpressionSet, prepared against the VariableIndexes vi.
    */
    public ValuatorSet prepare( VariableIndexes vi )
        {
        ValuatorSet result = new ValuatorSet();
        Iterator<Expression> it = expressions.iterator();
        while (it.hasNext()) result.add( it.next().prepare( vi ) );
        return result;    
        }
    
    /**
         Answer an iterator over all the Expressions in this ExpressionSet.
    */
    public Iterator<Expression> iterator()
        { return expressions.iterator(); }
    
    /**
         Answer a string representing this ExpressionSet for human consumption.
    */
    @Override public String toString()
        { return expressions.toString(); }
    }
