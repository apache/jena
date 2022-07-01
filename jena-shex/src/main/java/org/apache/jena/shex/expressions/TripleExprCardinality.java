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
import org.apache.jena.riot.out.NodeFormatter;

/**
 * Class to add cardinality to a bracketed {@link TripleExpression}.
 * <p>
 * {@link TripleConstraint TripleConstraints} have their own cardinality handling.
 */
public class TripleExprCardinality extends TripleExpression {

    private final TripleExpression other;
    private final Cardinality cardinality;
    private final int min;
    private final int max;

    public TripleExprCardinality(TripleExpression tripleExpr, Cardinality cardinality) {
        super();
        this.other = tripleExpr;
        this.cardinality = cardinality;
        this.min = (cardinality==null) ? 1 : cardinality.min;
        this.max = (cardinality==null) ? 1 : cardinality.max;
    }

    public TripleExpression target() { return other; }

    public String cardinalityString() {
        if ( cardinality == null )
            return "";
        return cardinality.image;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }


    @Override
    public void visit(TripleExprVisitor visitor) {
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
        TripleExprCardinality other = (TripleExprCardinality)obj;
        return Objects.equals(this.other, other.other);
    }

    @Override
    public void print(IndentedWriter iOut, NodeFormatter nFmt) {
        String s = cardinalityString();
        iOut.println("Cardinality");
        if ( ! s.isEmpty() )
            iOut.println("Cardinality = "+s);
        iOut.incIndent();
        other.print(iOut, nFmt);
        iOut.decIndent();
        iOut.println("/Cardinality");
    }

    @Override
    public String toString() {
        String s = cardinalityString();
        if ( s.isEmpty() )
            return "Cardinality [{-} other="+other+"]";
        return "Cardinality ["+s+" other="+other+"]";
    }
}
