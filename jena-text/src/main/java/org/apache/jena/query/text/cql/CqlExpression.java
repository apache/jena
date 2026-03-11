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

package org.apache.jena.query.text.cql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sealed interface hierarchy for OGC CQL2-JSON filter expressions.
 * Each node type is a Java record with a {@link #toCanonical()} method
 * that produces a deterministic string for cache key generation.
 */
public sealed interface CqlExpression
    permits CqlExpression.CqlAnd,
            CqlExpression.CqlOr,
            CqlExpression.CqlNot,
            CqlExpression.CqlComparison,
            CqlExpression.CqlIn,
            CqlExpression.CqlBetween,
            CqlExpression.CqlLike,
            CqlExpression.CqlSpatial {

    String toCanonical();

    record CqlAnd(List<CqlExpression> args) implements CqlExpression {
        public CqlAnd {
            args = List.copyOf(args);
        }

        @Override
        public String toCanonical() {
            List<String> sorted = args.stream()
                .map(CqlExpression::toCanonical)
                .sorted()
                .collect(Collectors.toList());
            return "and(" + String.join(",", sorted) + ")";
        }
    }

    record CqlOr(List<CqlExpression> args) implements CqlExpression {
        public CqlOr {
            args = List.copyOf(args);
        }

        @Override
        public String toCanonical() {
            List<String> sorted = args.stream()
                .map(CqlExpression::toCanonical)
                .sorted()
                .collect(Collectors.toList());
            return "or(" + String.join(",", sorted) + ")";
        }
    }

    record CqlNot(CqlExpression arg) implements CqlExpression {
        @Override
        public String toCanonical() {
            return "not(" + arg.toCanonical() + ")";
        }
    }

    record CqlComparison(String op, String property, Object value) implements CqlExpression {
        @Override
        public String toCanonical() {
            return op + "(" + property + "," + value + ")";
        }
    }

    record CqlIn(String property, List<Object> values) implements CqlExpression {
        public CqlIn {
            List<Object> sorted = new ArrayList<>(values);
            sorted.sort((a, b) -> String.valueOf(a).compareTo(String.valueOf(b)));
            values = Collections.unmodifiableList(sorted);
        }

        @Override
        public String toCanonical() {
            List<String> vals = values.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
            return "in(" + property + ",[" + String.join(",", vals) + "])";
        }
    }

    record CqlBetween(String property, Object lower, Object upper) implements CqlExpression {
        @Override
        public String toCanonical() {
            return "between(" + property + "," + lower + "," + upper + ")";
        }
    }

    record CqlLike(String property, String pattern) implements CqlExpression {
        @Override
        public String toCanonical() {
            return "like(" + property + "," + pattern + ")";
        }
    }

    record CqlSpatial(String op, String property, Object geometry) implements CqlExpression {
        @Override
        public String toCanonical() {
            return op + "(" + property + "," + geometry + ")";
        }
    }
}
