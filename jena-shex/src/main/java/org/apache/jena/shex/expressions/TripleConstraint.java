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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// [shex] Not a ShapeExpression per se - part of a TripleExpression.
public class TripleConstraint extends TripleExpression {
    private static Logger LOG = LoggerFactory.getLogger(TripleConstraint.class);

    /*
    TripleConstraint {
        id:tripleExprLabel?
        inverse:BOOL?
        predicate:IRIREF
        valueExpr:shapeExpr?
        min:INTEGER?
        max:INTEGER?
        semActs:[SemAct+]?
        annotations:[Annotation+]?
    }
     */

    private final Node label;
    private final Node predicate;
    // [shex] Move to a block of constraints object
    private final ShapeExpression shapeExpression;
    private final boolean reverse;
    private final Cardinality cardinality;
    private final int min;
    private final int max;

    public TripleConstraint(Node label, Node predicate, boolean reverse, ShapeExpression valueExpr, Cardinality cardinality, List<SemAct> semActs) {
        super(semActs);
        this.label = label;
        this.predicate = predicate;
        this.reverse = reverse;
        this.shapeExpression = valueExpr;
        this.cardinality = cardinality;
        this.min = (cardinality==null) ? 1 : cardinality.min;
        this.max = (cardinality==null) ? 1 : cardinality.max;
    }

    public String cardinalityString() {
        if ( cardinality == null )
            return "";
        return cardinality.image;
    }

    public Node label() {
        return label;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public Node getPredicate() {
        return predicate;
    }

    public ShapeExpression getShapeExpression() {
        return shapeExpression;
    }

    public boolean reverse() {
        return reverse;
    }

    private static Node value(Triple triple, boolean reverse) {
        return reverse ? triple.getSubject() : triple.getObject();
    }

    @Override
    public void visit(TripleExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void print(IndentedWriter iOut, NodeFormatter nFmt) {
        iOut.print("TripleConstraint");
        if ( label != null ) {
            iOut.print(" $");
            nFmt.format(iOut, label);
        }
        iOut.println(" {");
        iOut.incIndent();
        iOut.printf("predicate = ");
        if ( reverse )
            iOut.print("^");
        nFmt.format(iOut, predicate);
        iOut.println();
        shapeExpression.print(iOut, nFmt);
        if ( cardinality != null ) {
            iOut.print(cardinality.toString());
            iOut.println();
        }
        iOut.decIndent();
        iOut.println("}");
    }

    @Override
    public int hashCode() {
        return Objects.hash(max, min, predicate, reverse, shapeExpression);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        TripleConstraint other = (TripleConstraint)obj;
        return max == other.max && min == other.min && Objects.equals(predicate, other.predicate) && reverse == other.reverse
               && Objects.equals(shapeExpression, other.shapeExpression);
    }

    @Override
    public String toString() {
        String cardStr = "";
        if ( ! cardinalityString().isEmpty() )
            cardStr = "cardinality="+cardinalityString()+", ";
        String s = "TripleConstraint";
        if ( label != null )
            s = s+"($"+label+")";
        return s+ " [predicate=" + predicate + ", "+cardStr+"shapeExpr=" + shapeExpression + "]";
    }

}
