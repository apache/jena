/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.LinkedHashSet ;
import java.util.List ;
import java.util.Set ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.syntax.ElementBind ;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery ;
import com.hp.hpl.jena.sparql.syntax.PatternVars ;
import com.hp.hpl.jena.sparql.syntax.PatternVarsVisitor ;

/** Calculate in-scope variables from the AST */ 
public class SyntaxVarScope
{
    /*
     * Check for non-group-keys vars
     * Check for unused vars (warning?)
     * 1/ Combine finalization with findAndAddNamedVars/setResultVars
     */

    /** Accumulate pattern variables but include some checking as well */  
    static class PatternVarsVisitorChecker extends PatternVarsVisitor
    {
        public PatternVarsVisitorChecker(Set<Var> s)
        {
            super(s) ;
        }
        
        @Override
        public void visit(ElementBind el)
        {
            Var var = el.getVar() ;
            
            if ( acc.contains(var) ) 
                throw new QueryParseException("Variable used when already in-scope: "+var+" in "+el, -1 , -1) ;
            checkAssignment(acc, el.getExpr(), var) ;
        }
        
        @Override
        public void visit(ElementSubQuery el)
        {
            check(el.getQuery(), acc) ;
            super.visit(el) ;
        }
    }
    
    public static void check(Query query)
    {
        if ( query.getQueryPattern() == null )
            // DESCRIBE may not have a pattern
            return ;
        // And now check down the element for subqueries.
        LinkedHashSet<Var> queryVars = new LinkedHashSet<Var>() ;
        
        PatternVarsVisitorChecker visitor = new PatternVarsVisitorChecker(queryVars) ;
        PatternVars.vars(query.getQueryPattern(), visitor) ;
        queryVars.addAll(query.getGroupBy().getVars()) ;
        
        check(query, queryVars) ;
    }
    
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
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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