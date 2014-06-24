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

import com.hp.hpl.jena.query.ARQ ;
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
       These define the variables from a pattern that are in-scope
       These are not the usage rules.
         
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
    VALUES var     (values)                         v is in-scope if v is in varlist
    VALUES varlist (values)                         v is in-scope if v is in varlist
     */
    
    // Weakness : EXISTS inside FILTERs?

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
        BindScopeChecker v = new BindScopeChecker() ;
        ElementWalker.walk(query.getQueryPattern(), v) ;
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
        if ( query.hasValues() )
            vars.addAll(query.getValuesVariables()) ;
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
        Set<Var> vars2 = new LinkedHashSet<>(vars) ;

        for ( Var v : exprList.getVars() )
        {
            // In scope?
            Expr e = exprList.getExpr( v );
            checkAssignment( vars2, e, v );
            vars2.add( v );
        }
    }
    
    private static void checkExprVarUse(Query query)
    {
        if ( query.hasGroupBy() )
        {
            VarExprList groupKey = query.getGroupBy() ;
            
            // Copy - we need to add variables
            // SELECT (count(*) AS ?C)  (?C+1 as ?D) 
            List<Var> inScopeVars = new ArrayList<>(groupKey.getVars()) ;
            VarExprList exprList = query.getProject() ;

            for ( Var v : exprList.getVars() )
            {
                // In scope?
                Expr e = exprList.getExpr( v );
                if ( e == null )
                {
                    if ( !inScopeVars.contains( v ) )
                    {
                        throw new QueryParseException( "Non-group key variable in SELECT: " + v, -1, -1 );
                    }
                }
                else
                {
                    Set<Var> eVars = e.getVarsMentioned();
                    for ( Var v2 : eVars )
                    {
                        if ( !inScopeVars.contains( v2 ) )
                        {
                            throw new QueryParseException(
                                "Non-group key variable in SELECT: " + v2 + " in expression " + e, -1, -1 );
                        }
                    }
                }
                inScopeVars.add( v );
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
        for ( Var v : exprList.getVars() )
        {
            Expr e = exprList.getExpr( v );
            if ( !first )
            {
                sb.append( " " );
            }
            first = false;
            sb.append( "(" ).append( e ).append( " AS " ).append( v ).append( ")" );
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

    // Applies scope rules at each point it matters.
    // Does some recalculation in nested structures.
    
    public static class BindScopeChecker extends ElementVisitorBase
    {
        public BindScopeChecker() {}
        
        @Override
        public void visit(ElementGroup el)
        {
            // BIND scope rules
            // (and service warning)
            
            for ( int i = 0 ; i < el.getElements().size() ; i++ )
            {
                Element e = el.getElements().get(i) ;
                // Tests.
                if ( e instanceof ElementBind )
                {
                    Collection<Var> accScope = calcScopeAll(el.getElements(), i) ;
                    check(accScope, (ElementBind)e) ;
                }
                
                if ( e instanceof ElementService )
                {
                    Collection<Var> accScope = calcScopeAll(el.getElements(), i) ;
                    check(accScope, (ElementService)e) ;
                }
            }
        }
        
        private static Collection<Var> calcScopeAll(List<Element> elements, int idx)
        {
            return calcScope(elements, 0, idx) ;
        }

        /** Calculate scope, working forwards */
        private static Collection<Var> calcScope(List<Element> elements, int start, int finish)
        {
            Collection<Var> accScope = new HashSet<>() ;
            for ( int i = start ; i < finish ; i++ )
            {
                Element e = elements.get(i) ;
                PatternVars.vars(accScope, e) ;
            }
            return accScope ;
        }

        // Inside filters.
        
        private static void check(Collection<Var> scope, ElementBind el)
        {
            Var var = el.getVar() ;
            if ( scope.contains(var) ) 
                throw new QueryParseException("BIND: Variable used when already in-scope: "+var+" in "+el, -1 , -1) ;
            checkAssignment(scope, el.getExpr(), var) ;
        }
        
        private static void check(Collection<Var> scope, ElementService el)
        {
            if ( ARQ.isStrictMode() && el.getServiceNode().isVariable() )
            {
                Var var = Var.alloc(el.getServiceNode()) ;
                if ( ! scope.contains(var) ) 
                    throw new QueryParseException("SERVICE: Variable not already in-scope: "+var+" in "+el, -1 , -1) ;
            }
        }
    }
}
