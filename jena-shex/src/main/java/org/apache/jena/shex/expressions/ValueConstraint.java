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
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.sys.ReportItem;
import org.apache.jena.shex.sys.ShexLib;
import org.apache.jena.shex.sys.ValidationContext;

public class ValueConstraint extends NodeConstraint {

    private final List<ValueSetRange> valueSetRanges;

    public ValueConstraint(List<ValueSetRange> valueSetRanges) {
        this.valueSetRanges = valueSetRanges;
    }

    @Override
    public ReportItem nodeSatisfies(ValidationContext vCxt, Node data) {
        boolean b = valueSetRanges.stream().anyMatch(valueSetRange->validateRange(vCxt, valueSetRange, data));
        if ( !b )
            return new ReportItem("Value "+ShexLib.displayStr(data)+" not in range: "+asString(), null);
        return null;
    }

    private boolean validateRange(ValidationContext vCxt, ValueSetRange valueSetRange, Node data) {
        boolean b1 = valueSetRange.included(data);
        if ( ! b1 )
            return false;
        boolean b2 = valueSetRange.excluded(data);
        if ( b2 )
            return false;
        // OK
        return true;
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nFmt) {
        if ( valueSetRanges.isEmpty() ) {
            out.println("[ ]");
            return;
        }
        out.print("[");
        valueSetRanges.forEach(valueSetRange->{
            out.print(" ");
            valueSetRange.item.print(out, nFmt);
            if ( ! valueSetRange.exclusions.isEmpty() ) {
                out.print(" -");
                valueSetRange.exclusions.forEach(ex->{
                    out.print(" ");
                    ex.print(out, nFmt);
                });
            }
        });
        out.println(" ]");
    }

    @Override
    public void visit(ShapeExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ValueConstraint"+asString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueSetRanges);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ValueConstraint other = (ValueConstraint)obj;
        return Objects.equals(valueSetRanges, other.valueSetRanges);
    }
}
