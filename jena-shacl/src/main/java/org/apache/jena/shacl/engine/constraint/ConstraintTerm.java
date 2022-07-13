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

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.validation.event.ConstraintEvaluatedOnFocusNodeEvent;
import org.apache.jena.shacl.validation.event.ConstraintEvaluatedOnSinglePathNodeEvent;
import org.apache.jena.sparql.path.Path;

/* Constraint that does not need access to the data other than the nodes supplied. e.g. sh:datatype. */
public abstract class ConstraintTerm implements Constraint {

    public ConstraintTerm() {}

    @Override
    final
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> valueNodes) {
        valueNodes.forEach(x->applyConstraintTerm(vCxt, shape, focusNode, path, x));
    }

    @Override
    final
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        applyConstraintTerm(vCxt, shape, focusNode, null, focusNode);
    }

    private void applyConstraintTerm(ValidationContext vCxt, Shape shape, Node focusNode, Path path, Node term) {
        ReportItem item = validate(vCxt, term);
        boolean passed = item == null;
        if (path == null) {
            vCxt.notifyValidationListener(() -> new ConstraintEvaluatedOnFocusNodeEvent(vCxt, shape, focusNode, this, passed));
        } else {
            vCxt.notifyValidationListener(() -> new ConstraintEvaluatedOnSinglePathNodeEvent(vCxt, shape, focusNode, this, path,
                                            term, passed));
        }
        if ( passed ) {
            return;
        }
        vCxt.reportEntry(item, shape, focusNode, path, this);
    }

    public abstract ReportItem validate(ValidationContext vCxt, Node n) ;
}
