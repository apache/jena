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

/**
 * Constraint-related event that pertains to a path.
 */
public abstract class AbstractConstraintEvaluationForPathEvent extends AbstractConstraintEvaluationEvent implements
                ConstraintEvaluationForPathEvent {
    protected final Path path;

    public AbstractConstraintEvaluationForPathEvent(ValidationContext vCxt,
                    Shape shape, Node focusNode,
                    Constraint constraint, Path path) {
        super(vCxt, shape, focusNode, constraint);
        this.path = path;
    }

    @Override public Path getPath() {
        return path;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        AbstractConstraintEvaluationForPathEvent that = (AbstractConstraintEvaluationForPathEvent) o;
        return getPath().equals(that.getPath());
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getPath().hashCode();
        return result;
    }

}
