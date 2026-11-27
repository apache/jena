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

import org.apache.jena.graph.Node;
import org.apache.jena.shex.expressions.ShexAnnotated;
import org.apache.jena.shex.expressions.ShexAnnotation;

/**
 * Result of validation of a constraint.
 */
public class ReportItem {
    private final String message;
    private final Node value;
    private final ValidationTrace validationTrace;

    public ReportItem(String message, Node value) {
        this(message, value, null);
    }

    public ReportItem(String message, Node value, ValidationTrace validationTrace) {
        this.message = message;
        this.value = value;
        this.validationTrace = validationTrace;
    }

    public String getMessage() {
        return message;
    }

    public Node getValue() {
        return value;
    }

    /**
     * Get the validation trace associated with this report item.
     * May be null if no annotated expression was active.
     */
    public ValidationTrace getValidationTrace() {
        return validationTrace;
    }

    /**
     * Return a copy with the given validation trace, unless one
     * is already present.
     */
    ReportItem withValidationTrace(ValidationTrace trace) {
        if (validationTrace != null)
            return this;
        return new ReportItem(message, value, trace);
    }

    /**
     * Get annotations associated with this validation failure.
     *
     * Annotations are resolved from the innermost expression
     * carrying annotations in the validation trace.
     * @return annotations, or an empty list if none are available.
     */
    public List<ShexAnnotation> getAnnotations() {
        return validationTrace == null ?
            List.of() : validationTrace.getAnnotations();
    }

    /**
     * Get the expression from which the annotations originate.
     *
     * @return annotation source, or null if none exists.
     */
    public ShexAnnotated getAnnotationSource() {
        return validationTrace == null ?
            null : validationTrace.getAnnotationSource();
    }

    @Override
    public String toString() {
        return message+" ("+ShexLib.displayStr(value)+")";
    }
}
