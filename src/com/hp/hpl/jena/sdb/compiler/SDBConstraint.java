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

package com.hp.hpl.jena.sdb.compiler;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;

public abstract class SDBConstraint
{
    private boolean completeConstraint ;
    private Expr expr ;
    
    public SDBConstraint(Expr expr, boolean completeConstraint)
    { 
        this.expr = expr ; 
        this.completeConstraint = completeConstraint ;
    }
    
    abstract public SDBConstraint substitute(Binding binding) ;
    
    public boolean isComplete() { return completeConstraint ; }
    
    @Override
    public String toString() { return "[SDBConstraint "+expr+"]" ; }

    public Expr getExpr()
    {
        return expr ;
    }

    public abstract SqlExpr compile(Scope scope) ;
    
}
