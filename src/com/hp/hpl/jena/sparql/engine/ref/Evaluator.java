/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd. 
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.ref;

import java.util.List;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.expr.E_Aggregator;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;

import com.hp.hpl.jena.query.SortCondition;

public interface Evaluator
{
    public ExecutionContext getExecContext() ;
    
    public Table basicPattern(BasicPattern pattern) ;
    
    public Table pathPattern(TriplePath triplePath) ;

    // Two forms that provide custom code evaluation
    public Table procedure(Table table, Node procId, ExprList args) ;
    public Table propertyFunction(Table table, Node procId, PropFuncArg subjArgs, PropFuncArg objArgs) ;
    
    public Table join(Table tableLeft, Table tableRight) ;
    public Table leftJoin(Table tableLeft, Table tableRight, ExprList expr) ;
    public Table diff(Table tableLeft, Table tableRight) ;
    public Table minus(Table left, Table right) ;
    public Table union(Table tableLeft, Table tableRight) ;
    public Table condition(Table left, Table right) ;

    public Table filter(ExprList expressions, Table tableLeft) ;

    public Table unit() ; 
    public Table list(Table table) ;
    
    public Table order(Table table, List<SortCondition> conditions) ;
    public Table groupBy(Table table, VarExprList groupVars, List<E_Aggregator> aggregators) ;
    public Table project(Table table, List<Var> projectVars) ; 
    public Table distinct(Table table) ;
    public Table reduced(Table table) ;
    public Table slice(Table table, long start, long length) ;
    public Table assign(Table table, VarExprList exprs) ;
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd. 
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