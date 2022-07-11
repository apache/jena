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

import java.util.Collection;

/**
 * Event emitted when the focus node of a shape has been determined but before any constraints are validated.
 */
public class FocusNodesDeterminedEvent extends AbstractShapeValidationEvent implements ValidationLifecycleEvent {
    protected final ImmutableLazyCollectionCopy<Node> focusNodes;

    public FocusNodesDeterminedEvent(ValidationContext vCxt, Shape shape,
                    Collection<Node> focusNodes) {
        super(vCxt, shape);
        this.focusNodes = new ImmutableLazyCollectionCopy<>(focusNodes);
    }

    public Collection<Node> getFocusNodes() {
        return focusNodes.get();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        FocusNodesDeterminedEvent that = (FocusNodesDeterminedEvent) o;
        return getFocusNodes().equals(that.getFocusNodes());
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getFocusNodes().hashCode();
        return result;
    }

    @Override public String toString() {
        return "FocusNodesDeterminedEvent{" +
                        "shape=" + shape +
                        ", focusNodes=" + focusNodes +
                        '}';
    }
}
