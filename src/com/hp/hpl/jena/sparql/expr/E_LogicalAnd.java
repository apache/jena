/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/**
 * @author Andy Seaborne
 */ 

public class E_LogicalAnd extends ExprFunction2
{
    // See logical OR
    private static final String printName = "and" ;
    private static final String symbol = "&&" ;
    
    public E_LogicalAnd(Expr left, Expr right)
    {
        super(left, right, printName, symbol) ;
    }
    
    // Special : does not evaluate RHS if LHS means it is unnecessary. 
    @Override
    public NodeValue evalSpecial(Binding binding, FunctionEnv env)
    {
        ExprEvalException error = null ;
        try {
            NodeValue x = getArg1().eval(binding, env) ;
    
            if ( ! XSDFuncOp.booleanEffectiveValue(x) )
                return NodeValue.FALSE ; 
        } catch (ExprEvalException eee)
        {
            // RHS Must be true else error.
            error = eee ;
        }
        
        // LHS was false or error.
        
        try {
            NodeValue y = getArg2().eval(binding, env) ;
    
            if ( ! XSDFuncOp.booleanEffectiveValue(y) )
                return NodeValue.FALSE ;
            
            // RHS is true but was there an error earlier?
            if ( error != null ) 
                throw error ;
            
            return NodeValue.TRUE ;
        } catch (ExprEvalException eee)
        { 
            // LHS an error, RHS was not false => error
            // Throw the first
            if ( error != null )
                throw error ;
            // RHS was true - throw this error.
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
        return NodeValue.makeBoolean( boolX && boolY ) ;
    }
    
    @Override
    public Expr copy(Expr e1, Expr e2) {  return new E_LogicalAnd(e1 , e2 ) ; }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
