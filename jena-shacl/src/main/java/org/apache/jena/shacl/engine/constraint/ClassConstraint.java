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

import java.util.Collection;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.system.G;

/** sh:class */
public class ClassConstraint extends ConstraintDataTerm {

    private final Node expectedClass;

    // Better to take a Collection?
    public ClassConstraint(Node expectedClass) {
        this.expectedClass = expectedClass;
    }

    public Node getExpectedClass() {
        return expectedClass;
    }

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compact(out, nodeFmt, "class", expectedClass);
        // Only allowed in a property shape without OR or NOT.
//        if ( expectedClass.isURI() && ! ShLib.isDatatype(expectedClass.getURI()) ) {
//            nodeFmt.format(out, expectedClass);
//        } else {
//            compact(out, nodeFmt, "class", expectedClass);
//        }
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Graph data, Node focusNode) {
        if ( focusNode.isLiteral() ) {
            String msg = toString()+": Expected class :"+displayStr(expectedClass)+" for "+displayStr(focusNode);
            return new ReportItem(msg, focusNode);
        }

        Collection<Node> types = G.allTypesOfNodeRDFS(data, focusNode);
        if ( types.contains(expectedClass) )
            return null;
        String msg = toString()+": Expected class :"+displayStr(expectedClass)+" for "+displayStr(focusNode);
        return new ReportItem(msg, focusNode);
    }

    @Override
    public Node getComponent() {
        return SHACL.ClassConstraintComponent;
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nodeFmt) {
        out.print("ClassConstraint[");
        nodeFmt.format(out, expectedClass);
        out.print("]");
    }

    @Override
    public String toString() {
        return "ClassConstraint["+displayStr(expectedClass)+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedClass);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof ClassConstraint other) )
            return false;
        return Objects.equals(expectedClass, other.expectedClass);
    }
}
