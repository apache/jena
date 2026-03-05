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
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.query.text.TextIndexException;

/**
 * Parses OGC CQL2-JSON strings into {@link CqlExpression} AST nodes.
 * <p>
 * Expected JSON format follows CQL2-JSON:
 * <pre>
 * {"op": "=", "args": [{"property": "state"}, "WA"]}
 * {"op": "and", "args": [{...}, {...}]}
 * {"op": "in", "args": [{"property": "state"}, ["WA","OR"]]}
 * {"op": "between", "args": [{"property": "year"}, [2020, 2025]]}
 * {"op": "like", "args": [{"property": "name"}, "Gold%"]}
 * </pre>
 */
public class CqlParser {

    private static final Set<String> COMPARISON_OPS = Set.of("=", "<>", "<", ">", "<=", ">=");
    private static final Set<String> SPATIAL_OPS = Set.of(
        "s_intersects", "s_within", "s_contains", "s_disjoint",
        "s_equals", "s_crosses", "s_overlaps", "s_touches"
    );

    public static CqlExpression parse(String json) {
        JsonObject obj = JSON.parse(json);
        return parseNode(obj);
    }

    static CqlExpression parseNode(JsonObject obj) {
        if (!obj.hasKey("op")) {
            throw new TextIndexException("CQL2 JSON must have an 'op' field: " + obj);
        }

        String op = obj.get("op").getAsString().value();

        if (!obj.hasKey("args")) {
            throw new TextIndexException("CQL2 JSON must have an 'args' field: " + obj);
        }
        JsonArray args = obj.get("args").getAsArray();

        return switch (op) {
            case "and" -> parseAnd(args);
            case "or" -> parseOr(args);
            case "not" -> parseNot(args);
            case "in" -> parseIn(args);
            case "between" -> parseBetween(args);
            case "like" -> parseLike(args);
            default -> {
                if (COMPARISON_OPS.contains(op)) {
                    yield parseComparison(op, args);
                } else if (SPATIAL_OPS.contains(op)) {
                    yield parseSpatial(op, args);
                }
                throw new TextIndexException("Unknown CQL2 operator: " + op);
            }
        };
    }

    private static CqlExpression.CqlAnd parseAnd(JsonArray args) {
        List<CqlExpression> children = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            children.add(parseNode(args.get(i).getAsObject()));
        }
        return new CqlExpression.CqlAnd(children);
    }

    private static CqlExpression.CqlOr parseOr(JsonArray args) {
        List<CqlExpression> children = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            children.add(parseNode(args.get(i).getAsObject()));
        }
        return new CqlExpression.CqlOr(children);
    }

    private static CqlExpression.CqlNot parseNot(JsonArray args) {
        if (args.size() != 1) {
            throw new TextIndexException("CQL2 'not' requires exactly one argument, got " + args.size());
        }
        return new CqlExpression.CqlNot(parseNode(args.get(0).getAsObject()));
    }

    private static CqlExpression.CqlComparison parseComparison(String op, JsonArray args) {
        if (args.size() != 2) {
            throw new TextIndexException("CQL2 comparison requires exactly 2 arguments, got " + args.size());
        }
        String property = extractProperty(args.get(0));
        Object value = extractValue(args.get(1));
        return new CqlExpression.CqlComparison(op, property, value);
    }

    private static CqlExpression.CqlIn parseIn(JsonArray args) {
        if (args.size() != 2) {
            throw new TextIndexException("CQL2 'in' requires exactly 2 arguments, got " + args.size());
        }
        String property = extractProperty(args.get(0));
        JsonArray valArray = args.get(1).getAsArray();
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < valArray.size(); i++) {
            values.add(extractValue(valArray.get(i)));
        }
        return new CqlExpression.CqlIn(property, values);
    }

    private static CqlExpression.CqlBetween parseBetween(JsonArray args) {
        if (args.size() != 2) {
            throw new TextIndexException("CQL2 'between' requires exactly 2 arguments, got " + args.size());
        }
        String property = extractProperty(args.get(0));
        JsonArray bounds = args.get(1).getAsArray();
        if (bounds.size() != 2) {
            throw new TextIndexException("CQL2 'between' bounds array must have exactly 2 elements");
        }
        Object lower = extractValue(bounds.get(0));
        Object upper = extractValue(bounds.get(1));
        return new CqlExpression.CqlBetween(property, lower, upper);
    }

    private static CqlExpression.CqlLike parseLike(JsonArray args) {
        if (args.size() != 2) {
            throw new TextIndexException("CQL2 'like' requires exactly 2 arguments, got " + args.size());
        }
        String property = extractProperty(args.get(0));
        String pattern = args.get(1).getAsString().value();
        return new CqlExpression.CqlLike(property, pattern);
    }

    private static CqlExpression.CqlSpatial parseSpatial(String op, JsonArray args) {
        if (args.size() != 2) {
            throw new TextIndexException("CQL2 spatial op requires exactly 2 arguments, got " + args.size());
        }
        String property = extractProperty(args.get(0));
        // geometry is stored as-is for future processing
        Object geometry = args.get(1).toString();
        return new CqlExpression.CqlSpatial(op, property, geometry);
    }

    static String extractProperty(JsonValue val) {
        if (val.isObject()) {
            JsonObject obj = val.getAsObject();
            if (obj.hasKey("property")) {
                return obj.get("property").getAsString().value();
            }
        }
        throw new TextIndexException("Expected property reference {\"property\": \"name\"}, got: " + val);
    }

    static Object extractValue(JsonValue val) {
        if (val.isString()) {
            return val.getAsString().value();
        } else if (val.isNumber()) {
            Number num = val.getAsNumber().value();
            // Check if the number has a fractional part
            double d = num.doubleValue();
            if (d != Math.floor(d) || Double.isInfinite(d) || Double.isNaN(d)) {
                return d;
            }
            // Whole number: prefer int if it fits, otherwise long
            long l = num.longValue();
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return (int) l;
            }
            return l;
        } else if (val.isBoolean()) {
            return val.getAsBoolean().value();
        }
        throw new TextIndexException("Cannot extract CQL value from: " + val);
    }
}
