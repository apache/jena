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

/** Convert all visit calls on the expressions in a call to a generic visit operation for expression functions */
public abstract class ExprVisitorFunction implements ExprVisitor 
{
    @Override
    public void visit(ExprFunction0 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction1 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction2 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction3 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunctionN func) { visitExprFunction(func) ; }

    protected abstract void visitExprFunction(ExprFunction func) ;
}
