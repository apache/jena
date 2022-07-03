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

package org.apache.jena.sparql.algebra.op;

import java.util.Objects;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpUnfold extends Op1
{
    protected final Expr expr ;
    protected final Var var1 ;
    protected final Var var2 ;

    public OpUnfold(Op subOp, Expr expr, Var var1, Var var2) {
        super(subOp) ;
        this.expr = expr ;
        this.var1 = var1 ;
        this.var2 = var2 ;
    }

    public Expr getExpr()
    {
        return expr ;
    }

    public Var getVar1()
    {
        return var1 ;
    }

    public Var getVar2()
    {
        return var2 ;
    }

    @Override
    public String getName() {
        return Tags.tagUnfold ;
    }

    @Override
    public void visit(OpVisitor opVisitor) {
        opVisitor.visit(this) ;
    }

    @Override
    public Op1 copy(Op subOp) {
        OpUnfold op = new OpUnfold(subOp, expr, var1, var2) ;
        return op ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( !(other instanceof OpUnfold) )
            return false ;
        OpUnfold unfold = (OpUnfold)other ;

        if (    !Objects.equals(var1, unfold.var1) )
            return false ;
        if (    !Objects.equals(var2, unfold.var2) )
            return false ;
        if (    !Objects.equals(expr, unfold.expr) )
            return false ;
        return getSubOp().equalTo(unfold.getSubOp(), labelMap) ;
    }

    @Override
    public Op apply(Transform transform, Op subOp) {
        return transform.transform(this, subOp) ;
    }

    @Override
    public int hashCode() {
        if ( var2 == null )
            return getName().hashCode() ^ expr.hashCode() ^ var1.hashCode() ^ getSubOp().hashCode() ;
        else
            return getName().hashCode() ^ expr.hashCode() ^ var1.hashCode() ^ var2.hashCode() ^ getSubOp().hashCode() ;
    }
}
