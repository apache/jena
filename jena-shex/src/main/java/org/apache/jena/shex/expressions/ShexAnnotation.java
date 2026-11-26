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

package org.apache.jena.shex.expressions;

import java.util.Objects;
import org.apache.jena.graph.Node;

/**
 * A ShEx annotation consisting of a predicate and a value.
 */
public class ShexAnnotation {

    private final Node predicate;
    private final Node value;

    public ShexAnnotation(Node predicate, Node value) {
        this.predicate = Objects.requireNonNull(predicate);
        this.value = Objects.requireNonNull(value);
    }

    public Node getPredicate() {
        return predicate;
    }

    public Node getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicate, value);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;

        ShexAnnotation other = (ShexAnnotation)obj;
        return Objects.equals(predicate, other.predicate)
                && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return "ShexAnnotation [predicate=" + predicate
                + ", value=" + value + "]";
    }
}
