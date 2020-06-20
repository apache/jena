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

import static org.apache.jena.shacl.engine.constraint.CompactOut.compactArrayNodes;
import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;

/** sh:in */
public class InConstraint extends ConstraintTerm {

    private final List<Node> values = new ArrayList<>();

    public InConstraint(List<Node> list) {
        values.addAll(list);
    }

    @Override
    public Node getComponent() {
        return SHACL.InConstraintComponent;
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Node n) {
        if ( values.contains(n) )
            return null;
        String errMsg = toString()+" : RDF term "+displayStr(n)+" not in expected values";
        return new ReportItem(errMsg, n);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compactArrayNodes(out, nodeFmt, "in", values);
    }

    @Override
    public String toString() {
        return "InConstraint"+values;
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof InConstraint) )
            return false;
        InConstraint other = (InConstraint)obj;
        return Objects.equals(values, other.values);
    }
}
