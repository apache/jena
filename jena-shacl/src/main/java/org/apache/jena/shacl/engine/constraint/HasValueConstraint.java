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

import static org.apache.jena.shacl.compact.writer.CompactOut.compact;
import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.Objects;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;

/** sh:hasValue */
public class HasValueConstraint extends ConstraintEntity {

    private final Node value;

    public HasValueConstraint(Node value) {
        Objects.requireNonNull(value, "value");
        this.value = value;
    }

    @Override
    public Node getComponent() {
        return SHACL.HasValueConstraintComponent;
    }

    // NodeShape usage.
    @Override
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        if ( ! focusNode.equals(value) ) {
            String errMsg = toString()+" : No value "+displayStr(value);
            vCxt.reportEntry(errMsg, shape, focusNode, null, null, this);
        }
    }

    // PropertyShape usage.
    @Override
    public ReportItem validate(ValidationContext vCxt, Set<Node> pathNodes) {
        if ( pathNodes.contains(value) )
            return null;
        String errMsg = toString()+" : No value "+displayStr(value)+" in "+pathNodes;
        return new ReportItem(errMsg, null);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compact(out, nodeFmt, "hasValue", value);
    }

    @Override
    public String toString() {
        return "HasValueConstraint["+value+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof HasValueConstraint) )
            return false;
        HasValueConstraint other = (HasValueConstraint)obj;
        return Objects.equals(value, other.value);
    }
}
