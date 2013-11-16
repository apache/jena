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

package com.hp.hpl.jena.sparql.algebra.op ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;

public abstract class OpExtendAssign extends Op1 {
    protected final VarExprList assignments ;

    protected OpExtendAssign(Op subOp) {
        super(subOp) ;
        assignments = new VarExprList() ;
    }

    protected OpExtendAssign(Op subOp, VarExprList exprs) {
        super(subOp) ;
        assignments = exprs ;
    }

    final
    protected void add(Var var, Expr expr) {
        assignments.add(var, expr) ;
    }

    final
    public VarExprList getVarExprList() {
        return assignments ;
    }

    @Override
    final 
    public int hashCode()
    { return getName().hashCode() ^ assignments.hashCode() ^ getSubOp().hashCode() ; }

    public abstract OpExtendAssign copy(Op subOp, VarExprList varExprList) ;
}
