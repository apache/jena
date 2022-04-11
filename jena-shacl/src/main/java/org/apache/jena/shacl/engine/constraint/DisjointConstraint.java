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
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.event.ConstraintEvaluatedOnPathNodesWithCompareNodesEvent;
import org.apache.jena.shacl.validation.event.ConstraintEvaluatedOnSinglePathNodeWithCompareNodesEvent;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.path.Path;

import java.util.Objects;
import java.util.Set;

import static org.apache.jena.shacl.compact.writer.CompactOut.compactArrayNodes;
import static org.apache.jena.shacl.lib.ShLib.displayStr;

/** sh:disjoint */
public class DisjointConstraint extends ConstraintPairwise {

    public DisjointConstraint(Node value) {
        super(value, SHACL.DisjointConstraintComponent);
    }

    @Override
    public void validate(ValidationContext vCxt, Shape shape, Node focusNode, Path path,
                         Set<Node> pathNodes, Set<Node> compareNodes) {
        boolean allPassed = true;
        for ( Node vn : pathNodes ) {
            boolean passed = true;
            if ( compareNodes.contains(vn) ) {
                String msg = toString()+": not disjoint: "+displayStr(vn)+" is in "+compareNodes;
                passed = false;
                allPassed = false;
                vCxt.reportEntry(msg, shape, focusNode, path, vn, this);
            }
            if (!passed) {
                vCxt.notifyValidationListener(() -> makeEventSinglePathNode(
                                                vCxt, shape, focusNode, path, vn,
                                                compareNodes, false));
            }
        }
        if (allPassed){
            vCxt.notifyValidationListener(() -> makeEvent(
                                            vCxt, shape, focusNode, path, pathNodes,
                                            compareNodes, true));
        }
    }

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compact(out, nodeFmt, "disjoint", value);
    }

    @Override
    public String toString() {
        return "Disjoint["+displayStr(value)+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.value, 1);
    }
}
