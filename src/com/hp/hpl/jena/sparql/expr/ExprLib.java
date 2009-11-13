/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;

public class ExprLib
{
    /** Decide whether an expression is safe for using a a graph substitution.
     * Need to be careful about value-like tests when the graph is not 
     * matched in a value fashion.
     */

    public static boolean isAssignmentSafeEquality(Expr expr)
    { 
        return isAssignmentSafeEquality(expr, false, false) ;
    }
    
    /**
     * @param graphHasStringEquality    True if the graph triple matching equates xsd:string and plain literal
     * @param graphHasNumercialValueEquality    True if the graph triple matching equates numeric values
     */
    
    public static boolean isAssignmentSafeEquality(Expr expr, boolean graphHasStringEquality, boolean graphHasNumercialValueEquality) 
    {
        if ( !(expr instanceof E_Equals) && !(expr instanceof E_SameTerm) )
            return false ;

        // Corner case: sameTerm is false for string/plain literal, 
        // but true in the graph. 
        
        ExprFunction2 eq = (ExprFunction2)expr ;
        Expr left = eq.getArg1() ;
        Expr right = eq.getArg2() ;
        Var var = null ;
        NodeValue constant = null ;

        if ( left.isVariable() && right.isConstant() )
        {
            var = left.asVar() ;
            constant = right.getConstant() ;
        }
        else if ( right.isVariable() && left.isConstant() )
        {
            var = right.asVar() ;
            constant = left.getConstant() ;
        }

        // Not between a variable and a constant
        if ( var == null || constant == null )
            return false ;

        if ( ! constant.isLiteral() )
            // URIs, bNodes.  Any bNode will have come from a substitution - not legal syntax in filters
            return true ;
        
        if (expr instanceof E_SameTerm)
        {
            if ( graphHasStringEquality && constant.isString() ) 
                // Graph is not same term
                return false ;
            if ( graphHasNumercialValueEquality && constant.isNumber() )
                return false ;
            return true ;
        }
        
        // Final check for "=" where a FILTER = can do value matching when the graph does not.
        if ( expr instanceof E_Equals )
        {
            if ( ! graphHasStringEquality && constant.isString() )
                return false ;
            if ( ! graphHasNumercialValueEquality && constant.isNumber() )
                return false ;
            return true ;
        }
        // Unreachable.
        throw new ARQInternalErrorException() ;
    }
    
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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