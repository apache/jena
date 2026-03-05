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

package org.apache.jena.query.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

/**
 * Parses JSON sort specifications into {@link SortSpec} lists.
 * <p>
 * Single: {@code {"field": "year", "order": "desc"}}
 * Multi:  {@code [{"field": "year", "order": "desc"}, {"field": "title"}]}
 * Default order is ascending.
 */
public class SortSpecParser {

    public static List<SortSpec> parse(String json) {
        JsonValue val = JSON.parseAny(json);
        if (val.isArray()) {
            return parseArray(val.getAsArray());
        } else if (val.isObject()) {
            return Collections.singletonList(parseOne(val.getAsObject()));
        }
        throw new TextIndexException("Sort spec must be a JSON object or array: " + json);
    }

    private static List<SortSpec> parseArray(JsonArray arr) {
        List<SortSpec> specs = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            specs.add(parseOne(arr.get(i).getAsObject()));
        }
        return specs;
    }

    private static SortSpec parseOne(JsonObject obj) {
        if (!obj.hasKey("field")) {
            throw new TextIndexException("Sort spec must have a 'field' key: " + obj);
        }
        String field = obj.get("field").getAsString().value();
        boolean descending = false;
        if (obj.hasKey("order")) {
            String order = obj.get("order").getAsString().value();
            descending = "desc".equalsIgnoreCase(order);
        }
        return new SortSpec(field, descending);
    }

    /**
     * Check if a JSON string looks like a sort spec (has a "field" key).
     */
    public static boolean isSortSpec(String json) {
        return json.contains("\"field\"");
    }
}
