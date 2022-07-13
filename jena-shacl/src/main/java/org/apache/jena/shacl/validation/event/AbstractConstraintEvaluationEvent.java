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

/**
 * Constraint-related event.
 */
public abstract class AbstractConstraintEvaluationEvent extends AbstractFocusNodeValidationEvent implements
                ConstraintEvaluationEvent {
    protected final Constraint constraint;

    public AbstractConstraintEvaluationEvent(ValidationContext vCxt, Shape shape, Node focusNode,
                    Constraint constraint) {
        super(vCxt, shape, focusNode);
        this.constraint = constraint;
    }

    @Override public Constraint getConstraint() {
        return constraint;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        AbstractConstraintEvaluationEvent that = (AbstractConstraintEvaluationEvent) o;
        return getConstraint().equals(that.getConstraint());
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getConstraint().hashCode();
        return result;
    }

}
