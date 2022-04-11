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

package org.apache.jena.shacl.validation.event;

import org.apache.jena.graph.Node;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.path.Path;

import java.util.Set;

/**
 * Event emitted when the validation of a shape has begun.
 */
public class ConstraintEvaluationForPropertyShapeStartedEvent extends AbstractConstraintEvaluationOnPathNodesEvent
                implements ValidationLifecycleEvent {
    public ConstraintEvaluationForPropertyShapeStartedEvent(ValidationContext vCxt, Shape shape, Node focusNode,
                    Constraint constraint, Path path,
                    Set<Node> pathNodes) {
        super(vCxt, shape, focusNode, constraint, path, pathNodes);
    }

    @Override public String toString() {
        return "ConstraintEvaluationForPropertyShapeStartedEvent{" +
                        "constraint=" + constraint +
                        ", path=" + path +
                        ", valueNodes=" + valueNodes +
                        ", focusNode=" + focusNode +
                        ", shape=" + shape +
                        '}';
    }
}
