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

public interface ShapeExprVisitor //extends NodeConstraintVisitor
{
    public default void visit(ShapeExprAND shape) {}
    public default void visit(ShapeExprOR shape) {}
    public default void visit(ShapeExprNOT shape) {}
    public default void visit(ShapeExprDot shape) {}
    public default void visit(ShapeExprAtom shape) {}
    public default void visit(ShapeExprNone shape) {}
    public default void visit(ShapeExprRef shape) {}
    public default void visit(ShapeExprExternal shape) {}
    public default void visit(ShapeExprTripleExpr shape) {}
    public default void visit(ShapeNodeConstraint shape) {}

    public default void visit(ShapeExprTrue shape) {}
    public default void visit(ShapeExprFalse shape) {}
}
