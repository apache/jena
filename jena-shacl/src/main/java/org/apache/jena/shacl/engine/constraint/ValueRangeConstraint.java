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

package org.apache.jena.shacl.engine.constraint;

import static org.apache.jena.shacl.engine.constraint.CompactOut.compact;

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.ValueSpaceClassification;

/** A constraint that tests the value of a node. */
public abstract class ValueRangeConstraint extends ConstraintTerm {

    protected final NodeValue nodeValue;
    private final Node constraintComponent;

    protected ValueRangeConstraint(Node value, Node constraintComponent) {
        this.nodeValue = NodeValue.makeNode(value);
        this.constraintComponent = constraintComponent;
    }

    @Override
    final public ReportItem validate(ValidationContext vCxt, Node n) {
        NodeValue nv = NodeValue.makeNode(n);
        ValueSpaceClassification vs = NodeValue.classifyValueOp(nodeValue, nv);
        try {
            int r = NodeValue.compare(nodeValue, nv);
            if ( r == Expr.CMP_INDETERMINATE )
                return new ReportItem(toString()+" indeterminant to "+n, n);
            boolean b = test(r);
            if ( b )
                return null;
            return new ReportItem(getErrorMessage(n), n);
        } catch (ExprNotComparableException ex) {
            return new ReportItem(toString()+" can't compare to "+n, n);
        }
    }

    protected abstract String getErrorMessage(Node n);

    protected abstract boolean test(int r);

    protected abstract String getName();

    @Override
    final
    public Node getComponent() {
        return constraintComponent  ;
    }


    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compact(out, nodeFmt, getName(), nodeValue.asNode());
    }

    @Override
    public String toString() {
        return getName()+"["+nodeValue+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraintComponent, nodeValue);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( ! this.getClass().equals(obj.getClass()) )
            return false;
        ValueRangeConstraint other = (ValueRangeConstraint)obj;
        return Objects.equals(constraintComponent, other.constraintComponent) && Objects.equals(nodeValue, other.nodeValue);
    }
}
