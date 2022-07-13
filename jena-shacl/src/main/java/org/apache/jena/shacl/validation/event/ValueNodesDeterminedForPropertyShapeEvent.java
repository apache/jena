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
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.path.Path;

import java.util.Set;

/**
 * Event emitted when the value nodes of a property shape have been determined, but before any constraints are validated.
 */
public class ValueNodesDeterminedForPropertyShapeEvent extends AbstractFocusNodeValidationEvent
                implements ValidationLifecycleEvent {
    protected final Path path;
    protected final ImmutableLazySetCopy<Node> pathNodes;

    public ValueNodesDeterminedForPropertyShapeEvent(ValidationContext vCxt,
                    Shape shape, Node focusNode, Path path,
                    Set<Node> pathNodes) {
        super(vCxt, shape, focusNode);
        this.path = path;
        this.pathNodes = new ImmutableLazySetCopy<>(pathNodes);
    }

    public Path getPath() {
        return path;
    }

    public Set<Node> getPathNodes() {
        return pathNodes.get();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ValueNodesDeterminedForPropertyShapeEvent that = (ValueNodesDeterminedForPropertyShapeEvent) o;
        if (!getPath().equals(that.getPath()))
            return false;
        return getPathNodes().equals(that.getPathNodes());
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getPath().hashCode();
        result = 31 * result + getPathNodes().hashCode();
        return result;
    }

    @Override public String toString() {
        return "ValueNodesDeterminedForPropertyShapeEvent{" +
                        "focusNode=" + focusNode +
                        ", shape=" + shape +
                        ", pathNodes=" + pathNodes +
                        '}';
    }
}
