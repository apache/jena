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

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.sys.ValidationContext;

/** ShapeAtom - element of the ShEx abstract syntax. */
public class ShapeExprAtom extends ShapeExpression {

    private final ShapeExpression other;

    public ShapeExprAtom(ShapeExpression other) {
        this.other = other;
    }

    public ShapeExpression getShape() { return other; }

    @Override
    public void print(IndentedWriter out, NodeFormatter nFmt) {
        out.println(toString());
    }

    @Override
    public boolean satisfies(ValidationContext vCxt, Node data) {
        return true;
    }

    @Override
    public void visit(ShapeExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(other);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ShapeExprAtom other = (ShapeExprAtom)obj;
        return Objects.equals(this.other, other.other);
    }

    @Override
    public String toString() { return "ShapeExprAtom[ "+other+" ]"; }
}
