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

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.optimize.ExprTransformConstantFold ;
import com.hp.hpl.jena.sparql.core.Var ;

public class ExprLib
{
    /** Attempt to fold any sub-expressions of the Expr.
     * Return an expression that is euqivalent to the argument but maybe simpler.    
     * @param expr
     * @return Expression
     */
    public static Expr foldConstants(Expr expr) {
        return ExprTransformer.transform(new ExprTransformConstantFold(), expr) ;
    }

    /** transform an expression that may involve aggregates into one that just uses the variable for the aggregate */  

    public static Expr replaceAggregateByVariable(Expr expr)
    {
        return ExprTransformer.transform(replaceAgg, expr) ;
    }

    /** transform expressions that may involve aggregates into one that just uses the variable for the aggregate */  
    public static ExprList replaceAggregateByVariable(ExprList exprs)
    {
        return ExprTransformer.transform(replaceAgg, exprs) ;
    }
    
    private static ExprTransform replaceAgg = new ExprTransformCopy()
    {
        @Override
        public Expr transform(ExprAggregator eAgg)       
        { return eAgg.getAggVar()  ; }
    } ;
    
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
    
    /** Some "functions" are non-deterministic (unstable) - 
     * calling them with the same arguments 
     * does not yields the same answer each time.
     * Therefore how and when they are called
     * matters.
     * 
     * Functions: RAND, UUID, StrUUID, BNode
     * 
     * NOW() is safe.
     */
    public static boolean isStable(Expr expr) {
        try {
            ExprWalker.walk(exprVisitorCheckForNonFunctions, expr) ;
            return true ;
        } catch ( ExprUnstable ex ) {
            return false ;
        }
    }

    private static ExprVisitor exprVisitorCheckForNonFunctions = new ExprVisitorBase() { 
        @Override
        public void visit(ExprFunction0 func) {
            if ( func instanceof E_Random ||
                func instanceof E_UUID ||
                func instanceof E_StrUUID)
                throw new ExprUnstable() ; 
        }
        @Override
        public void visit(ExprFunctionN func) {
            if (func instanceof E_BNode )
                throw new ExprUnstable() ;
        }
    } ;
    
    private static class ExprUnstable extends ExprException {}
}
