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

package com.hp.hpl.jena.sparql.engine.ref;

import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;

public interface Evaluator
{
    public ExecutionContext getExecContext() ;
    
    public Table basicPattern(BasicPattern pattern) ;
    
    public Table pathPattern(TriplePath triplePath) ;

    // Two forms that provide custom code evaluation
    public Table procedure(Table table, Node procId, ExprList args) ;
    public Table propertyFunction(Table table, Node procId, PropFuncArg subjArgs, PropFuncArg objArgs) ;

    public Table assign(Table table, VarExprList exprs) ;
    public Table extend(Table table, VarExprList exprs) ;
    
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
    public Table groupBy(Table table, VarExprList groupVars, List<ExprAggregator> aggregators) ;
    public Table project(Table table, List<Var> projectVars) ; 
    public Table distinct(Table table) ;
    public Table reduced(Table table) ;
    public Table slice(Table table, long start, long length) ;
}
