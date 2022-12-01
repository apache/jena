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

import java.util.*;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.eval.ShapeEval;
import org.apache.jena.shex.sys.ValidationContext;

// Shape
public class ShapeExprTripleExpr extends ShapeExpression {
    // [shex] This is the inlineShapeDefinition
    // Can we combine with a top-level ShexShape?

    /*
    Shape {
        id:shapeExprLabel?
        closed:BOOL?
        extra:[IRIREF]?
        expression:tripleExpr?
        semActs:[SemAct+]?
        annotations:[Annotation+]? }
    */

    private Node label;
    private Set<Node> extras;
    private boolean closed;
    //extra:[IRIREF]?
    private TripleExpression tripleExpr;
    //semActs:[SemAct+]?
    //annotations:[Annotation+]?
    public static Builder newBuilder() { return new Builder(); }

    private ShapeExprTripleExpr(Node label, Set<Node> extras, boolean closed, TripleExpression tripleExpr, List<SemAct> semActs) {
        super(semActs);
        this.label = label;
        if ( extras == null || extras.isEmpty() )
            this.extras = null;
        else
            this.extras = extras;
        this.closed = closed;
        this.tripleExpr = tripleExpr;
    }

    public TripleExpression getTripleExpr() { return tripleExpr; }

    public Node getLabel() {
        return label;
    }

    public Set<Node> getExtras() {
        return extras;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean satisfies(ValidationContext vCxt, Node node) {
        // Pass extras
        return ShapeEval.matchesTripleExpr(vCxt, tripleExpr, node, extras, closed);
    }

    @Override
    public void print(IndentedWriter iOut, NodeFormatter nFmt) {
        iOut.print("Shape");
        if ( label != null ) {
            iOut.print(" ");
            nFmt.format(iOut,  label);
        }
        iOut.println();
        iOut.incIndent();
        if ( closed )
            iOut.println("CLOSED");
        iOut.println("TripleExpression");
        iOut.incIndent();
        if ( tripleExpr != null )
            tripleExpr.print(iOut, nFmt);
        else
            iOut.println("<none>");
        iOut.decIndent();
        iOut.decIndent();
    }

    @Override
    public void visit(ShapeExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Shape: "+((label==null)?"":label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(closed, label, tripleExpr);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ShapeExprTripleExpr other = (ShapeExprTripleExpr)obj;
        return closed == other.closed && Objects.equals(label, other.label) && Objects.equals(tripleExpr, other.tripleExpr);
    }

    public static class Builder {
        private Node label;
        private Set<Node> extras = null;
        private List<SemAct> semActs;
        private Optional<Boolean> closed = null;
        //extra:[IRIREF]?
        private TripleExpression tripleExpr = null;
        //semActs:[SemAct+]?
        //annotations:[Annotation+]?

        Builder() {}

        public Builder label(Node label) { this.label = label ; return this; }

        public Builder extras(List<Node> extrasList) {
            if ( extras == null )
                extras = new HashSet<>();
            this.extras.addAll(extrasList);
            return this;
        }

        public Builder semActs(List<SemAct> semActsList) {
            if ( semActs == null )
                semActs = new ArrayList<>();
            if (semActsList != null)
                this.semActs.addAll(semActsList);
            return this;
        }

        public Builder closed(boolean value) { this.closed = Optional.of(value); return this; }

        public Builder shapeExpr(TripleExpression tripleExpr) { this.tripleExpr = tripleExpr; return this; }

        public ShapeExprTripleExpr build() {
            boolean isClosed = (closed == null) ? false : closed.get();
            return new ShapeExprTripleExpr(label, extras, isClosed, tripleExpr, semActs);
        }
    }
}
