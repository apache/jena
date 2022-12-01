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

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpFilter extends Op1
{
    protected ExprList expressions;

    /** Add expression - mutates an existing filter */
    public static Op filter(Expr expr, Op op) {
        OpFilter f = ensureFilter(op);
        f.getExprs().add(expr);
        return f;
    }

    /**
     * Ensure that the algebra op is a filter. If the input is a filter, just return that,
     * else create a filter with no expressions and "this" as the subOp.
     * @apiNote
     * This operation assumes the caller is going to add expressions.
     * Filters without any expressions are discouraged.
     * Consider collecting the expressions together first and using {@link #filterBy}.
     */
    public static OpFilter ensureFilter(Op op) {
        if ( op instanceof OpFilter )
            return (OpFilter)op;
        else
            return new OpFilter(op);
    }

    /** Combine an ExprList with an Op so that the expressions filter the Op.
     * If the exprs are empty, return the Op.
     * If the op is already a OpFilter, merge the expressions into the filters existing expressions.
     * Else create a new OpFilter with the expressions and subOp.
     */
    public static Op filterBy(ExprList exprs, Op op) {
        if ( exprs == null || exprs.isEmpty() )
            return op;
        OpFilter f = ensureFilter(op);
        f.getExprs().addAll(exprs);
        return f;
    }

    /** Create a OpFilter with the expressions and subOp.
     * If subOp is a filter, combine expressions (de-layer).
     */
    public static OpFilter filterAlways(ExprList exprs, Op subOp) {
        OpFilter f = ensureFilter(subOp);
        f.getExprs().addAll(exprs);
        return f;
    }

    /** Make a OpFilter - guaranteed to return an fresh OpFilter */
    public static OpFilter filterDirect(ExprList exprs, Op op) {
        return new OpFilter(exprs, op);
    }

    /** Make a OpFilter - guaranteed to return an fresh OpFilter */
    public static OpFilter filterDirect(Expr expr, Op op) {
        OpFilter f = new OpFilter(op);
        f.getExprs().add(expr);
        return f;
    }

    private OpFilter(Op sub) {
        super(sub);
        expressions = new ExprList();
    }

    private OpFilter(ExprList exprs, Op sub) {
        super(sub);
        expressions = exprs;
    }

    /** Compress multiple filters: (filter (filter (filter op)))) into one (filter op) */
    public static OpFilter tidy(OpFilter base) {
        ExprList exprs = new ExprList();

        Op op = base;
        while (op instanceof OpFilter) {
            OpFilter f = (OpFilter)op;
            exprs.addAll(f.getExprs());
            op = f.getSubOp();
        }
        return new OpFilter(exprs, op);
    }

    public ExprList getExprs() { return expressions; }

    @Override
    public String getName() { return Tags.tagFilter; }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp); }

    @Override
    public void visit(OpVisitor opVisitor) { opVisitor.visit(this); }

    @Override
    public Op1 copy(Op subOp) { return new OpFilter(expressions, subOp); }

    @Override
    public int hashCode() {
        return expressions.hashCode();
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( ! (other instanceof OpFilter) ) return false;
        OpFilter opFilter = (OpFilter)other;
        if ( ! expressions.equals(opFilter.expressions) )
            return false;
        return getSubOp().equalTo(opFilter.getSubOp(), labelMap);
    }
}
