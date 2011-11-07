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

package com.hp.hpl.jena.sparql.lang;

import java.util.* ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.syntax.* ;

/** Calculate in-scope variables from the AST */ 
public class SyntaxVarScope
{
    /* SPARQL 1.1 "in scope" rules
    Syntax Form                                     In-scope variables
    
    Basic Graph Pattern (BGP)                       v occurs in the BGP
    Path                                            v occurs in the path
    Group { P1 P2 ... }                             v is in-scope if in-scope in one or more of P1, P2, ...
    GRAPH term { P }                                v is term or v is in-scope in P
    { P1 } UNION { P2 }                             v is in-scope in P1 or in-scope in P2
    OPTIONAL {P}                                    v is in-scope in P
    SERVICE term {P}                                v is term or v is in-scope in P
    (expr AS v) for BIND, SELECT and GROUP BY       v is in-scope
    SELECT ..v .. { P }                             v is in-scope if v is mentioned as a project variable
    SELECT * { P }                                  v is in-scope in P
    BINDINGS varlist (values)                       v is in-scope if v is in varlist
     */
    
    /*
     * Check for non-group-keys vars
     * Check for unused vars (warning?)
     * Check for out of scope
     *   two cases: BIND and SubQuery
     *    BIND done during variable accumulation.
     *    SubQuery done as a separate pass.
     * Combine finalization with findAndAddNamedVars/setResultVars
     */

    public static void check(Query query)
    {
        if ( query.getQueryPattern() == null )
            // DESCRIBE may not have a pattern
            return ;
        
        checkSubQuery(query.getQueryPattern()) ;
        checkBind(query) ;
        // Check this level.
        checkQueryScope(query) ;
    
        // Other checks.
        Collection<Var> vars = varsOfQuery(query) ;
        check(query, vars) ;
    }

    // Check BIND by accumulating variables and making sure BIND does not attempt to reuse one  
    private static void checkBind(Query query)
    {
        LinkedHashSet<Var> queryVars = new LinkedHashSet<Var>() ;
        BindScopeChecker visitor = new BindScopeChecker(queryVars) ;
        //PatternVars.vars(query.getQueryPattern(), visitor) ;
        
        ElementWalker.Walker walker = new ScopeWalker(visitor) ;
        ElementWalker.walk(query.getQueryPattern(), walker) ;
    }
    
    // Check subquery by finding subquries and recurisively checking.
    // Includes appling all checks to nested subqueries.
    private static void checkSubQuery(Element el)
    {
        ElementVisitor v = new SubQueryScopeChecker() ;
        ElementWalker.walk(el, v) ;
    }
    
    // Check one level of query - SELECT expressions
    private static void checkQueryScope(Query query)
    {
        Collection<Var> vars = varsOfQuery(query) ;
        checkExprListAssignment(vars, query.getProject()) ;
    }
    
    // get all vars of a query
    private static Collection<Var> varsOfQuery(Query query)
    {
        Collection<Var> vars = PatternVars.vars(query.getQueryPattern()) ;
        if ( query.hasBindings() )
            vars.addAll(query.getBindingVariables()) ;
        return vars ;
    }
    
    // Other check (not scoping at this level) of a query
    private static void check(Query query, Collection<Var> vars)
    {
        // Check any expressions are assigned to fresh variables.
        checkExprListAssignment(vars, query.getProject()) ;
        
        // Check for SELECT * GROUP BY
        // Legal in ARQ, not in SPARQL 1.1
        if ( ! Syntax.syntaxARQ.equals(query.getSyntax()) )
        {
            if ( query.isQueryResultStar() && query.hasGroupBy() )
                throw new QueryParseException("SELECT * not legal with GROUP BY", -1 , -1) ;
        }
        
        // Check any variable in an expression is in scope (if GROUP BY) 
        checkExprVarUse(query) ;
        
        // Check GROUP BY AS 
        // ENABLE
        if ( false && query.hasGroupBy() )
        {
            VarExprList exprList2 = query.getGroupBy() ;
            checkExprListAssignment(vars, exprList2) ;
        // CHECK 
        }
        
    }
    
    private static void checkExprListAssignment(Collection<Var> vars, VarExprList exprList)
    {
        Set<Var> vars2 = new LinkedHashSet<Var>(vars) ;
        
        for ( Iterator<Var> iter = exprList.getVars().iterator() ; iter.hasNext() ; )
        {
            // In scope?
            Var v = iter.next();
            Expr e = exprList.getExpr(v) ;
            checkAssignment(vars2, e, v) ;
            vars2.add(v) ;
        }
    }
    
    private static void checkExprVarUse(Query query)
    {
        if ( query.hasGroupBy() )
        {
            VarExprList groupKey = query.getGroupBy() ;
            List<Var> groupVars = groupKey.getVars() ;
            VarExprList exprList = query.getProject() ;
            
            for ( Iterator<Var> iter = exprList.getVars().iterator() ; iter.hasNext() ; )
            {
                // In scope?
                Var v = iter.next();
                Expr e = exprList.getExpr(v) ;
                if ( e == null )
                {
                    if ( ! groupVars.contains(v) )
                        throw new QueryParseException("Non-group key variable in SELECT: "+v, -1 , -1) ;
                }
                else
                {
                    Set<Var> eVars = e.getVarsMentioned() ;
                    for ( Var v2 : eVars )
                    {
                        if ( ! groupVars.contains(v2) )
                            throw new QueryParseException("Non-group key variable in SELECT: "+v2+" in expression "+e , -1 , -1) ;
                    }
                }
            }
        }
    }
    
    private static void checkAssignment(Collection<Var> scope, Expr expr, Var var)
    {
        // Project SELECT ?x
        if ( expr == null )
            return ;
        
        // expr not null
        if ( scope.contains(var) ) 
            throw new QueryParseException("Variable used when already in-scope: "+var+" in "+fmtAssignment(expr, var), -1 , -1) ;

        // test for impossible variables - bound() is a bit odd.
        if ( false )
        {
            Set<Var> vars = expr.getVarsMentioned() ;
            for ( Var v : vars )
            {
                if ( !scope.contains(v) )
                    throw new QueryParseException("Variable used in expression is not in-scope: "+v+" in "+expr, -1 , -1) ;
            }
        }
    }
    
    private static String fmtExprList(VarExprList exprList)
    {
        StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for ( Iterator<Var> iter = exprList.getVars().iterator() ; iter.hasNext() ; )
        {
            Var v = iter.next();
            Expr e = exprList.getExpr(v) ;
            if ( ! first )
                sb.append(" ") ;
            first = false ;
            sb.append("(").append(e).append(" AS ").append(v).append(")") ;
        }
        return sb.toString() ;
    }
    
    private static String fmtAssignment(Expr expr, Var var)
    {
        return "("+expr+" AS "+var+")" ;
    }

    // Modifed walked for variables.
    
    /** Visitor for subqueries scope rules . */
    private static class SubQueryScopeChecker extends ElementVisitorBase
    {
        @Override
        public void visit(ElementSubQuery el)
        {
            Query query = el.getQuery() ;
            checkQueryScope(query) ;
            // Recursively check sub-queries in sub-queries.
            check(el.getQuery()) ;
        }
    }

    /** Accumulate pattern variables but include some checking (BIND) as well */
    private static class BindScopeChecker extends PatternVarsVisitor
    {
        public BindScopeChecker(Set<Var> s)
        {
            super(s) ;
        }
        
        @Override
        public void visit(ElementBind el)
        {
            Var var = el.getVar() ;
            
            if ( acc.contains(var) ) 
                throw new QueryParseException("BIND: Variable used when already in-scope: "+var+" in "+el, -1 , -1) ;
            checkAssignment(acc, el.getExpr(), var) ;
        }
    }

    // Special version of walker for scoping rules.
    
    public static class ScopeWalker extends ElementWalker.Walker
    {
        PatternVarsVisitor pvVisitor ;
        
        protected ScopeWalker(PatternVarsVisitor visitor)
        {
            super(visitor) ;
            pvVisitor = visitor ;
        }
        
        @Override
        public void visit(ElementMinus el)
        {
            // Don't go down the RHS of MINUS
            //if ( el.getMinusElement() != null )
            //    el.getMinusElement().visit(this) ;
            proc.visit(el) ;
        }
        
        // It is a top-down walk, so on enter an element of group or UNION,
        // then the entry set is 

        // Isolate elements of UNION
        @Override
        public void visit(ElementUnion el)
        {
            Set<Var> accState = new HashSet<Var>(pvVisitor.acc) ;
            doMultipleIndependent(accState, el.getElements()) ;
            pvVisitor.acc = accState ;
            proc.visit(el) ;
        }
        
        // There are different kinds of elements in a GROUP:
        // BGPs (ElementTriplesBlock ElementPathBlock)
        //   Rolling accumulation
        // GRAPH ?g { ?s ?p ?o }
        // BIND applies to BGP 
        // FILTER end of group
        // All other elements (SERVICE?) outcome is only to the overall results.
        
//        @Override
//        public void visit(ElementGroup el)
//        {
//            // But BIND needs to be does over end of group.
//            // Ditto FILTER tests.
//            Set<Var> accState = new HashSet<Var>(pvVisitor.acc) ;
//            doMultipleIndependent(accState, el.getElements()) ;
//            pvVisitor.acc = accState ;
//            proc.visit(el) ;
//        }
        

        private void doMultipleIndependent(Set<Var> agg, List<Element> elements)
        {
            // agg is empty?
            for ( Element e : elements )
            {
                pvVisitor.acc.clear() ;
                // Do subelement.
                e.visit(this) ;
                // Accumulate for final result.
                agg.addAll(pvVisitor.acc) ;
            }
        }
        
    }

    
//    public static void varsWalk(Element element, PatternVarsVisitor visitor)
//    {
//        ElementWalker.Walker walker = new ScopeWalker(visitor) ;
//        ElementWalker.walk(element, walker) ;
//    }
//    
}
