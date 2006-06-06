/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.core.Constraint;
import com.hp.hpl.jena.query.expr.*;

// TODO Condition recognition to be replaced by ExprMatcher.

public class ConditionRecognizer
{
    // 1 - insert here
    
    public static boolean recognize(Block block, Constraint c)
    {
        if ( ! c.isExpr() )
            return false ;
        return recognize(block, c.getExpr()) ;
    }
    
    public static boolean recognize(Block block, Expr expr)
    {
        RecognizeExpr v = new RecognizeExpr(block) ;
        expr.visit(v) ;
        return v.recognized ;
    }
    
    static class RecognizeExpr extends ExprVisitorBase
    {
        boolean recognized = false ;
        Block block ; 
        
        RecognizeExpr(Block block) { this.block = block ; }
        
        @Override
        public void visit(ExprFunction ex)
        {
            if ( ex instanceof E_Regex )
                visitRegex((E_Regex)ex) ;
        }

        public void visitRegex(E_Regex ex)
        {
            Expr arg1 = ex.getRegexExpr() ;
            Expr pattern = ex.getPattern() ;
            Expr flags = ex.getFlags() ;
            
            String varName = null ;
            boolean acceptableOperand = false ;

            if ( arg1.isVariable() )
                varName = arg1.getVarName() ;
            else if ( arg1.isExpr() && arg1.getExpr() instanceof E_Str )
            {
                Expr e = ((E_Str)arg1.getExpr()).getArg(1) ;
                if ( e.isVariable() )
                    varName = e.getVarName() ;
            }
            
            if ( varName == null )
                return ;
            
            Node var = Node.createVariable(varName) ;
            
            if ( ! block.getPatternVars().contains(var) )
            {
                System.err.println("Regex: reject/vars: "+ex) ;
                return ;
            }
            
            if ( ! pattern.isConstant() || ! pattern.getConstant().isString() )
                return ;
            if ( flags != null && ( ! flags.isConstant() || ! flags.getConstant().isString() ) )
                return ;
            // (var, string, string)
            recognized = true ;
            
        }
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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