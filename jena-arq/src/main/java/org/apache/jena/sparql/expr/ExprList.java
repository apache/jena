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

import java.util.* ;

import org.apache.jena.sparql.algebra.walker.Walker ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.util.Context ;

public class ExprList implements Iterable<Expr>
{
    private final List<Expr> expressions ;
    /** Create a copy which does not share the list of expressions with the original */ 
    public static ExprList copy(ExprList other) { return new ExprList(other) ; }
    
    /** Create an ExprList that contains the expressions */
    public static ExprList create(Collection<Expr> exprs) {
        ExprList exprList = new ExprList() ;
        exprs.forEach(exprList::add) ;
        return exprList ; 
    } 
    
    /** Empty, immutable ExprList */
    public static final ExprList emptyList = new ExprList(Collections.emptyList()) ;
    
    public ExprList() { expressions = new ArrayList<>() ; }
    
    private ExprList(ExprList other) {
        this() ;
        expressions.addAll(other.expressions) ;
    }

    public ExprList(Expr expr) {
        this() ;
        expressions.add(expr) ;
    }

    public ExprList(List<Expr> x)   { expressions = x ; }

    public boolean isSatisfied(Binding binding, ExecutionContext execCxt) {
        for (Expr expr : expressions) {
            if ( !expr.isSatisfied(binding, execCxt) )
                return false ;
        }
        return true ;
    }
    
    public Expr get(int idx)                            { return expressions.get(idx) ; }
    public int size()                                   { return expressions.size() ; }
    public boolean isEmpty()                            { return expressions.isEmpty() ; }
    public ExprList subList(int fromIdx, int toIdx)     { return new ExprList(expressions.subList(fromIdx, toIdx)) ; }
    public ExprList tail(int fromIdx)                   { return subList(fromIdx, expressions.size()) ; }
    
    public Set<Var> getVarsMentioned() {
        return ExprVars.getVarsMentioned(this);
    }

    /**
     * Rewrite, applying a node{@literal ->}node transformation
     */
    public ExprList applyNodeTransform(NodeTransform transform) {
        ExprList x = new ExprList() ;
        for ( Expr e : expressions)
            x.add(e.applyNodeTransform(transform));
        return x ; 
    }

    public ExprList copySubstitute(Binding binding) {
        ExprList x = new ExprList() ;
        for (Expr expr : expressions ) {
            expr = expr.copySubstitute(binding) ;
            x.add(expr) ;
        }
        return x ;
    }

    public void addAll(ExprList exprs)      { expressions.addAll(exprs.getList()) ; }
    public void add(Expr expr)              { expressions.add(expr) ; }
    public List<Expr> getList()             { return Collections.unmodifiableList(expressions) ; }
    /** Use only while building ExprList */
    public List<Expr> getListRaw()          { return expressions ; }
    @Override
    public Iterator<Expr> iterator()        { return expressions.iterator() ; }
    
    public void prepareExprs(Context context) {
        ExprBuild build = new ExprBuild(context) ;
        // Give each expression the chance to set up (bind functions)
        for (Expr expr : expressions)
            Walker.walk(expr, build) ;
    }
    
    @Override
    public String toString()
    { return expressions.toString() ; }
    
    @Override
    public int hashCode() { return expressions.hashCode() ; }

    public boolean equals(ExprList other, boolean bySyntax) {
        if ( this == other ) return true ;
        if (expressions.size() != other.expressions.size()) return false;
        
        for ( int i = 0 ; i < expressions.size() ; i++ ) {
            Expr e1 = expressions.get(i) ;
            Expr e2 = other.expressions.get(i) ;
            if ( ! e1.equals(e2, bySyntax) ) 
                return false ;
        }
        return true ;
    }
    
    @Override
    public boolean equals(Object other) {
        if ( this == other ) return true ;
        if ( ! ( other instanceof ExprList ) ) return false ;
        ExprList exprs = (ExprList)other ;
        //return expressions.equals(exprs.expressions) ;
        return equals((ExprList)other, false) ;
    }

    public static ExprList splitConjunction(ExprList exprList1) {
        ExprList exprList2 = new ExprList() ;
        for (Expr expr : exprList1)
            split(exprList2, expr) ;
        return exprList2 ;
    }

    private static ExprList splitConjunction(Expr expr) {
        ExprList exprList = new ExprList() ;
        split(exprList, expr) ;
        return exprList ;
    }

    private static void split(ExprList exprList, Expr expr) {
        // Explode &&-chain to exprlist.
        while (expr instanceof E_LogicalAnd) {
            E_LogicalAnd x = (E_LogicalAnd)expr ;
            Expr left = x.getArg1() ;
            Expr right = x.getArg2() ;
            split(exprList, left) ;
            expr = right ;
        }
        // Drop through and add remaining
        exprList.add(expr) ;
    }
}
