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

package com.hp.hpl.jena.sdb.exprmatch;

import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.util.ExprUtils;

/** Matches an expression template to a query expression.  
 */

public class ExprMatcher
{
    /* ActionMap  : what to do when a variable is encountered in the pattern
     * CalloutMap : what to do when a (fixed) function is discovered in the pattern 
     * ResultMap  : Association of var name to expression needed for a match.
     *              Actually, just a string -> expression map  
     */
    
    /** Match an expression against a pattern.  If the pattern has variables
     * in it, these are checked, by name, in the MapAction and the registered action
     * invoked to determine whether the variable matches; if the pattern has a named
     * funcion, the MapCallout is used to find a registered operation.
     * Return a map of results, recording the bindings of variables   
     */
    
    public static MapResult match(Expr expression,
                                  Expr pattern,
                                  MapAction  mapAction,
                                  MapCallout mapCallout,
                                  MapResult  mapResult)
    {
        MatchVisitor m = new MatchVisitor(expression, mapAction, mapCallout, mapResult) ;
        try {
            pattern.visit(m) ;
        } catch (NoExprMatch ex)
        { 
            return null ;
        } 
        return mapResult ;
    }

    public static MapResult match(Expr expression,
                                  Expr pattern,
                                  MapAction mapAction,
                                  MapCallout mapCallout)
    {
        return match(expression, pattern, mapAction, mapCallout, new MapResult()) ;
    }

    public static MapResult match(Expr expression, Expr pattern, MapAction mapAction)
    { return match(expression, pattern, mapAction, null, new MapResult()) ; }
    
    public static MapResult match(String expression, String pattern, MapAction mapAction)
    {
        return match(ExprUtils.parse(expression), 
                     ExprUtils.parse(pattern),
                     mapAction) ;
    }

    // Visit/walk the pattern
    static class MatchVisitor extends ExprVisitorFunction implements ExprVisitor
    {
        private Expr       target ;
        private MapAction  aMap ;
        private MapResult  rMap ;
        private MapCallout cMap ;

        // Default action is to accept anything - i.e. pattern variables are any expression
        static ActionMatch defaultAction = new ActionMatchBind() ;

        MatchVisitor(Expr target, MapAction aMap, MapCallout cMap, MapResult rMap)
        { 
            this.aMap = aMap ;
            this.rMap = rMap ;
            this.cMap = cMap ;
            this.target = target ;
        }

        @Override
        public void startVisit()
        {}

        /* ExprFunction in the pattern:
         * 1/ If in the call out map, do that : return 
         * 2/ Check that the thing to be matched is a function as well
         *    and has the same function label (internal name or IRI).
         * 3/ Check same number of arguments
         * 4/ For each argument, match that as well.
         */

        @Override
        public void visitExprFunction(ExprFunction patExpr)
        {
            String uri = patExpr.getFunctionIRI() ;

            if ( uri != null && cMap != null && cMap.containsKey(uri) )
            {
                List<Expr> args = patExpr.getArgs() ;
                if( ! cMap.get(uri).match(uri, args, rMap) )
                    throw new NoExprMatch("Function callout rejected match") ;
                return ;
            }

            if ( ! target.isFunction() )  
                throw new NoExprMatch("Not an ExprFunction: "+target) ;

            ExprFunction funcTarget = target.getFunction() ;

            if ( ! patExpr.getFunctionSymbol().equals(funcTarget.getFunctionSymbol()) )
                throw new NoExprMatch("Different function symbols: "+patExpr.getFunctionSymbol().getSymbol()+" // "+funcTarget.getFunctionSymbol().getSymbol()) ;

            if ( patExpr.numArgs() != funcTarget.numArgs() )
                throw new NoExprMatch("Different arity: "+patExpr.numArgs()+"/"+funcTarget.numArgs()) ;

            // Either both null (some built-in) or both the same IRI 
            if ( ! ( patExpr.getFunctionIRI() == null && funcTarget.getFunctionIRI() == null ) )
                if ( ! patExpr.getFunctionIRI().equals(funcTarget.getFunctionIRI()) )
                    throw new NoExprMatch("Different functions: "+patExpr.getFunctionIRI()+" "+funcTarget.getFunctionIRI()) ;

            for ( int i = 1 ; i <= funcTarget.numArgs() ; i++ )
            {
                // Recurse, breaking up the target. 
                Expr p = patExpr.getArg(i) ;
                Expr e = funcTarget.getArg(i) ;

                MatchVisitor m = new MatchVisitor(e, aMap, cMap, rMap) ;
                p.visit(m) ;
            }
        }

        /* NodeValue in the pattern
         * The target must have the same.  
         */

        @Override
        public void visit(NodeValue nv)
        {
            if ( ! target.isConstant() )
                throw new NoExprMatch("Not a NodeValue") ;
            if ( ! nv.equals(target.getConstant()) )
                throw new NoExprMatch("Different value: "+nv+" & "+target.getConstant()) ;
        }

        /*
         * Variable is the pattern
         * 1/ If in the action map, do the action
         * 2/ Invoke default action. 
         */

        @Override
        public void visit(ExprVar patternVar)
        {
            Var vn = patternVar.asVar() ;
            ActionMatch a = aMap.get(vn) ; 
            if ( a == null )
                a = defaultAction ;

            if ( a.match(vn, target, rMap) )
                return ;
            throw new NoExprMatch("Action for "+patternVar+ "+failed") ;
        }

        @Override
        public void visit(ExprFunctionOp funcOp)
        { throw new NoExprMatch("ExprFunctionOp") ; }

        @Override
        public void visit(ExprAggregator eAgg)
        { throw new NoExprMatch("ExprAggregate") ; }

        @Override
        public void finishVisit()
        {}
    }
}
