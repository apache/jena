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

package org.apache.jena.sparql.expr;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.algebra.optimize.ExprTransformConstantFold ;
import org.apache.jena.sparql.algebra.walker.Walker ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.function.FunctionEnv ;

public class ExprLib
{
    /** Evaluate or return null.
     * <p>
     * This is better (faster) than the simple implementation
     * which captures {@link ExprEvalException} and returns null.
     */

    public static NodeValue evalOrNull(Expr expr, Binding binding, FunctionEnv functionEnv) {
        return evalOrElse(expr, binding, functionEnv, null) ;
    }

    /** evaluate or throw an exception */
    // This post dates a lot of code that uses expr.eval directly.
    // Placeholder for now.
    private static NodeValue evalOrException(Expr expr, Binding binding, FunctionEnv functionEnv) {
        return expr.eval(binding, functionEnv) ;
    }

    private static NodeValue evalOrElse(Expr expr, Binding binding, FunctionEnv functionEnv, NodeValue exceptionValue) {
        // Exceptions in java are expensive if the stack information is
        // collected which is the default behaviour.  The expensive step is
        // Throwable.fillInStackTrace.
        //
        // Otherwise, they are reasonable cheap. It needs special exceptions
        // which overrides fillInStackTrace to be cheap but they loose the
        // general information for development.
        //
        // Instead, pick out specal cases, the expression being a single variable
        // being the important one.
        //
        // BOUND(?x) is a important case where the expression is often an exception
        // in general evaluation.  See E_Bound - different exception handling
        // (it handles VariableNotBoundException not a general ExprEvalException).

        if ( expr.isConstant() )
            // Easy case.
            return expr.getConstant() ;
        if ( expr.isVariable() ) {
            // The case of the expr being a single variable.
            Var v = expr.asVar() ;
            Node n = binding.get(v) ;
            if ( n == null )
                return exceptionValue ;
            NodeValue nv = NodeValue.makeNode(n) ;
            return nv ;
        }

        try {
            return expr.eval(binding, functionEnv) ;
        } catch (ExprEvalException ex) {
            return exceptionValue ;
        }
    }

    /** Attempt to fold any sub-expressions of the Expr.
     * Return an expression that is equivalent to the argument but maybe simpler.
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

//    /** transform expressions that may involve aggregates into one that just uses the variable for the aggregate */
//    public static ExprList replaceAggregateByVariable(ExprList exprs)
//    {
//        return ExprTransformer.transform(replaceAgg, exprs) ;
//    }

    private static ExprTransform replaceAgg = new ExprTransformCopy()
    {
        @Override
        public Expr transform(ExprAggregator eAgg)
        { return eAgg.getAggVar()  ; }
    } ;

    /** Decide whether an expression is safe for using a graph substitution.
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
            Walker.walk(expr, exprVisitorCheckForNonFunctions) ;
            return true ;
        } catch ( ExprUnstable ex ) {
            return false ;
        }
    }

    private static ExprVisitor exprVisitorCheckForNonFunctions = new ExprVisitorBase() {
        @Override public void visit(ExprFunction0 func) { check(func); }
        @Override public void visit(ExprFunction1 func) { check(func); }
        @Override public void visit(ExprFunction2 func) { check(func); }
        @Override public void visit(ExprFunction3 func) { check(func); }
        @Override public void visit(ExprFunctionN func) { check(func); }

        private void check(ExprFunction exprFn) {
            if ( exprFn instanceof Unstable )
                throw new ExprUnstable();
        }
    } ;

    private static class ExprUnstable extends ExprException {
        // Filling in the stack trace is the expensive part of
        // an exception but we don't need it.
        @Override
        public Throwable fillInStackTrace() { return this ; }
    }

    /** Go from a node to an expression. */
    public static Expr nodeToExpr(Node n) {
        if ( n.isVariable() )
            return new ExprVar(n) ;
        if ( n.isNodeTriple() ) {
            Node_Triple tripleTerm = (Node_Triple)n;
            return new ExprTripleTerm(tripleTerm);
        }
        return NodeValue.makeNode(n) ;
    }

    public static Expr rewriteTriple(Triple t) {
        Expr e1 = nodeToExpr(t.getSubject());
        Expr e2 = nodeToExpr(t.getPredicate());
        Expr e3 = nodeToExpr(t.getObject());
        return new E_TripleFn(e1, e2, e3);
    }
}
