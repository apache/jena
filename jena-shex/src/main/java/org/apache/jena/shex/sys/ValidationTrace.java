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

package org.apache.jena.shex.sys;

import java.util.List;
import java.util.Objects;

import org.apache.jena.shex.expressions.ShexAnnotated;
import org.apache.jena.shex.expressions.ShexAnnotation;

/**
 * Records the expression path active during a validation failure.
 * ValidationTrace stores annotation-bearing expressions only.
 *
 * Expressions are ordered from outermost to innermost.
 * The expression path is later used for finding annotations of
 * failed expressions.
 */
public class ValidationTrace {

    private final List<ShexAnnotated> expressionPath;

    public ValidationTrace(List<ShexAnnotated> expressionPath) {
        this.expressionPath = List.copyOf(
            Objects.requireNonNull(expressionPath)
        );
    }

    /**
     * Get the expressions active during validation, ordered from
     * outermost to innermost.
     */
    public List<ShexAnnotated> getExpressionPath() {
        return expressionPath;
    }

    /**
     * Get the innermost expression, or null if the trace is empty.
     */
    public ShexAnnotated getInnermostExpression() {
        return expressionPath.isEmpty()
            ? null
            : expressionPath.get(expressionPath.size() - 1);
    }

    /**
     * Find the innermost expression carrying annotations.
     *
     * @return the expression with annotations, or null if none exists.
     */
    public ShexAnnotated getAnnotationSource() {
        for (int i = expressionPath.size() - 1; i >= 0; i--) {
            ShexAnnotated expression = expressionPath.get(i);
            if (!expression.getAnnotations().isEmpty())
                return expression;
        }
        return null;
    }

    /**
     * Get annotations from the innermost expression carrying annotations.
     *
     * Annotations from enclosing expressions are not merged.
     * @return an immutable list of annotations, empty if none exist.
     */
    public List<ShexAnnotation> getAnnotations() {
        ShexAnnotated source = getAnnotationSource();
        return source == null ? List.of() : source.getAnnotations();
    }
}
