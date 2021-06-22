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

public class TripleExprWalker implements TripleExprVisitor {

    private final TripleExprVisitor beforeVisitor;
    private final TripleExprVisitor afterVisitor;
    private final ShapeExprVisitor shapeVisitor;

    public TripleExprWalker(TripleExprVisitor beforeVisitor, TripleExprVisitor afterVisitor,
                                  ShapeExprVisitor shapeVisitor) {
        this.beforeVisitor = beforeVisitor;
        this.afterVisitor = afterVisitor;
        this.shapeVisitor = shapeVisitor;
    }

    private void before(TripleExpression tripleExpr) {
        if ( beforeVisitor != null )
            tripleExpr.visit(beforeVisitor);
    }

    private void after(TripleExpression tripleExpr) {
        if ( afterVisitor != null )
            tripleExpr.visit(afterVisitor);
    }

    @Override
    public void visit(TripleExprEachOf object) {
        before(object);
        object.expressions().forEach(tripleExpr->tripleExpr.visit(this));
        after(object);
    }

    @Override
    public void visit(TripleExprOneOf object) {
        before(object);
        object.expressions().forEach(tripleExpr->tripleExpr.visit(this));
        after(object);
    }

    @Override
    public void visit(TripleExprCardinality object) {
        before(object);
        object.target().visit(this);
        after(object);

    }

    @Override public void visit(TripleExprNone object) {
        before(object);
        after(object);
    }

    @Override public void visit(TripleExprRef object) {
        before(object);
        after(object);
    }

    @Override public void visit(TripleConstraint object) {
        before(object);
        if ( shapeVisitor != null )
            object.getShapeExpression().visit(shapeVisitor);
        after(object);
    }
}
