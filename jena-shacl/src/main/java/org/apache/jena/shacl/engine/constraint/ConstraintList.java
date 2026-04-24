/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
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

public abstract class ConstraintList implements Constraint {

    protected ConstraintList() {}

    @Override
    final
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> valueNodes) {
        valueNodes.forEach(x->applyConstraintList(vCxt, shape, focusNode, path, data, x));
    }

    @Override
    final
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        applyConstraintList(vCxt, shape, focusNode, null, data, focusNode);
    }

    private void applyConstraintList(ValidationContext vCxt, Shape shape, Node focusNode, Path path, Graph data, Node listHead) {
        ReportItem item = validateList(vCxt, data, listHead);
        boolean passed = (item == null);
        if (path == null) {
            vCxt.notifyValidationListener(() -> new ConstraintEvaluatedOnFocusNodeEvent(vCxt, shape, focusNode, this, passed));
        } else {
            vCxt.notifyValidationListener(() -> new ConstraintEvaluatedOnSinglePathNodeEvent(vCxt, shape, focusNode, this, path,
                                                                                             listHead, passed));
        }
        if ( passed )
            return;
        vCxt.reportEntry(item, shape, focusNode, path, this);
    }

    protected abstract ReportItem validateList(ValidationContext vCxt, Graph data, Node n) ;
}
