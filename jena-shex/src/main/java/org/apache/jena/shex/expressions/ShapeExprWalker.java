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

package org.apache.jena.shex.expressions;

public class ShapeExprWalker implements ShapeExprVisitor {

    private final ShapeExprVisitor beforeVisitor;
    private final ShapeExprVisitor afterVisitor;
    private final TripleExprVisitor tripleExprWalker;

//    public ShapeExpressionWalker(ShapeExpressionVisitor beforeVisitor, ShapeExpressionVisitor afterVisitor) {
//        this(beforeVisitor, afterVisitor, null, null);
//    }

    public ShapeExprWalker(ShapeExprVisitor beforeVisitor, ShapeExprVisitor afterVisitor,
                                 TripleExprVisitor beforeTripleExprVisitor, TripleExprVisitor afterTripleExprVisitor) {
        this.beforeVisitor = beforeVisitor;
        this.afterVisitor = afterVisitor;
        this.tripleExprWalker = new TripleExprWalker(beforeTripleExprVisitor, afterTripleExprVisitor, this);
    }


    private void before(ShapeExpression shape) {
        if ( beforeVisitor != null )
            shape.visit(beforeVisitor);
    }

    private void after(ShapeExpression shape) {
        if ( afterVisitor != null )
            shape.visit(afterVisitor);
    }

    @Override public void visit(ShapeExprAND shape) {
        before(shape);
        shape.expressions().forEach(sh->sh.visit(this));
        after(shape);
    }

    @Override public void visit(ShapeExprOR shape) {
        before(shape);
        shape.expressions().forEach(sh->sh.visit(this));
        after(shape);
    }

    @Override public void visit(ShapeExprNOT shape) {
        before(shape);
        shape.subShape().visit(this);
        after(shape);
    }

    @Override
    public void visit(ShapeExprRef shape) {
        before(shape);
        after(shape);
    }

    @Override
    public void visit(ShapeExprFalse shape) {
        before(shape);
        after(shape);
    }

    @Override
    public void visit(ShapeExprNone shape) {
        before(shape);
        after(shape);
    }

    @Override
    public void visit(ShapeExprTrue shape) {
        before(shape);
        after(shape);
    }

    @Override
    public void visit(ShapeExprExternal shape) {
        before(shape);
        after(shape);
    }

    @Override
    public void visit(ShapeExprTripleExpr shape) {
        before(shape);
        if ( shape.getTripleExpr() != null )
            shape.getTripleExpr().visit(tripleExprWalker);
        after(shape);
    }

    @Override
    public void visit(StrRegexConstraint constraint) {
        before(constraint);
        after(constraint);
    }

    @Override
    public void visit(StrLengthConstraint constraint) {
        before(constraint);
        after(constraint);
    }

    @Override
    public void visit(DatatypeConstraint constraint) {
        before(constraint);
        after(constraint);
    }

    @Override
    public void visit(NodeKindConstraint constraint) {
        before(constraint);
        after(constraint);
    }

    @Override
    public void visit(NumLengthConstraint constraint) {
        before(constraint);
        after(constraint);
    }

    @Override
    public void visit(NumRangeConstraint constraint) {
        before(constraint);
        after(constraint);
    }

    @Override
    public void visit(ValueConstraint constraint) {
        before(constraint);
        after(constraint);
    }
}
