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

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.Tags;

/* 
    Logical OR and AND is special with respect to handling errors truth table.
    
    A       B   |   NOT A   A && B  A || B
    -------------------------------------
    E       E   |   E       E       E
    E       T   |   E       E       T
    E       F   |   E       F       E
    T       E   |   F       E       T
    T       T   |   F       T       T
    T       F   |   F       F       T
    F       E   |   T       F       E
    F       T   |   T       F       T
    F       F   |   T       F       F
*/

public class E_LogicalOr extends ExprFunction2
{
    
    private static final String functionName = Tags.tagOr ;
    private static final String symbol = Tags.symOr ;
    
    public E_LogicalOr(Expr left, Expr right)
    {
        super(left, right, functionName, symbol) ;
    }
    
    @Override
    public NodeValue evalSpecial(Binding binding, FunctionEnv env)
    {
        ExprEvalException error = null ;
        try {
            NodeValue x = getArg1().eval(binding, env) ;
    
            if ( XSDFuncOp.booleanEffectiveValue(x) )
    			return NodeValue.TRUE ; 
        } catch (ExprEvalException eee)
        {
            // RHS Must be true else error.
            error = eee ;
        }
        
        // LHS was false or error.
        
        try {
            NodeValue y = getArg2().eval(binding, env) ;
    
    		if ( XSDFuncOp.booleanEffectiveValue(y) )
    			return NodeValue.TRUE ;
            
            // RHS is false but was there an error earlier?
            if ( error != null ) 
                throw error ;
    		
    		return NodeValue.FALSE ;
        } catch (ExprEvalException eee)
        { 
            // LHS an error, RHS was not true => error
            // Throw the first
            if ( error != null )
                throw error ;
            // RHS was false - throw this error.
            throw eee ;
        }
    }
    
    @Override
    public NodeValue eval(NodeValue x, NodeValue y)
    {
        // Evaluation only happens as part of copySubstitute.
        // Proper evaluation is a special form as above.
        
        if ( ! x.isBoolean() )
            throw new ExprEvalException("Not a boolean: "+x) ;    
        if ( ! y.isBoolean() )
            throw new ExprEvalException("Not a boolean: "+y) ;    
        
        boolean boolX = x.getBoolean() ;
        boolean boolY = y.getBoolean() ;
        return NodeValue.makeBoolean( boolX || boolY ) ;
    }
    
    @Override
    public Expr copy(Expr e1, Expr e2) {  return new E_LogicalOr(e1 , e2 ) ; }
}
