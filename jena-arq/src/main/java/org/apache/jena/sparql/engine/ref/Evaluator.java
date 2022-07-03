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

package org.apache.jena.sparql.engine.ref;

import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;

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
    public Table unfold(Table table, Expr expr, Var var1, Var var2) ;
    
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
