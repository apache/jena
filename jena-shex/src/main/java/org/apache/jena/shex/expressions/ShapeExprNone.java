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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.sys.ValidationContext;

/** Absence of a shape expression. For example, the outcome of "{}" */
public class ShapeExprNone extends ShapeExpression {

    private static ShapeExpression instance = new ShapeExprNone();
    public static ShapeExpression get() { return instance ; }

    private ShapeExprNone() {}

    @Override
    public boolean satisfies(ValidationContext vCxt, Node data) {
        return true;
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nFmt) {
        out.println(toString());
    }

    @Override
    public void visit(ShapeExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() { return "ShapeExprNone"; }

    @Override
    public int hashCode() {
        return ShexConst.hashShExprNone;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        return true;
    }
}
