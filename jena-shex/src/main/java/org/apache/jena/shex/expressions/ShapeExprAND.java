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

import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.sys.ValidationContext;

public class ShapeExprAND extends ShapeExpression {

    // Could pull out ShapeExpressionN
    // [ ] print
    // [ ] Most of equals.

    public static ShapeExpression create(List<ShapeExpression> acc) {
        if ( acc.size() == 0 )
            throw new InternalErrorException("Empty list");
        if ( acc.size() == 1 )
            return acc.get(0);
        return new ShapeExprAND(acc);
    }

    List<ShapeExpression> shapeExpressions;

    private ShapeExprAND(List<ShapeExpression> expressions) {
        this.shapeExpressions = expressions;
    }

    public List<ShapeExpression> expressions() {
        return shapeExpressions;
    }

    @Override
    public boolean satisfies(ValidationContext vCxt, Node data) {
        // Record all reports?
        for ( ShapeExpression shExpr : shapeExpressions ) {
            boolean innerSatisfies = shExpr.satisfies(vCxt, data);
            if ( !innerSatisfies )
                return false;
        }
        return true;
    }

    @Override
    public void visit(ShapeExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nFmt) {
        //out.printf("AND(%d)\n", shapeExpressions.size());
        out.println("AND");
        int idx = 0;
        for ( ShapeExpression shExpr : shapeExpressions ) {
            idx++;
            out.printf("%d -", idx);
            out.incIndent(4);
            shExpr.print(out, nFmt);
            out.decIndent(4);
        }
        out.println("/AND");
    }

    @Override
    public String toString() {
        return "ShapeExprAnd "+expressions();
    }

    @Override
    public int hashCode() {
        return Objects.hash(1, shapeExpressions);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ShapeExprAND other = (ShapeExprAND)obj;
        return Objects.equals(shapeExpressions, other.shapeExpressions);
    }
}
