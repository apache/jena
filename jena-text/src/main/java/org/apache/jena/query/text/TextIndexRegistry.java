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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Named registry of {@link TextIndexLucene} instances for multi-index support.
 * <p>
 * Each index is identified by a string ID. The first registered index becomes
 * the default. Transaction lifecycle methods delegate to all registered indexes.
 */
public class TextIndexRegistry {

    public static final String DEFAULT_ID = "default";

    private final Map<String, TextIndexLucene> indexes = new LinkedHashMap<>();
    private String defaultId;

    public TextIndexRegistry() {}

    /**
     * Create a single-index registry.
     */
    public static TextIndexRegistry single(TextIndexLucene index) {
        TextIndexRegistry reg = new TextIndexRegistry();
        reg.register(DEFAULT_ID, index);
        return reg;
    }

    public void register(String id, TextIndexLucene index) {
        indexes.put(id, index);
        if (defaultId == null) {
            defaultId = id;
        }
    }

    public TextIndexLucene get(String id) {
        TextIndexLucene idx = indexes.get(id);
        if (idx == null) {
            throw new TextIndexException("No text index registered with id: " + id);
        }
        return idx;
    }

    public TextIndexLucene getDefault() {
        if (defaultId == null) {
            throw new TextIndexException("No text indexes registered");
        }
        return indexes.get(defaultId);
    }

    public String getDefaultId() {
        return defaultId;
    }

    public Collection<TextIndexLucene> all() {
        return Collections.unmodifiableCollection(indexes.values());
    }

    public Map<String, TextIndexLucene> allWithIds() {
        return Collections.unmodifiableMap(indexes);
    }

    public int size() {
        return indexes.size();
    }

    public void prepareCommit() {
        for (TextIndexLucene idx : indexes.values()) {
            idx.prepareCommit();
        }
    }

    public void commit() {
        for (TextIndexLucene idx : indexes.values()) {
            idx.commit();
        }
    }

    public void rollback() {
        for (TextIndexLucene idx : indexes.values()) {
            idx.rollback();
        }
    }

    public void close() {
        for (TextIndexLucene idx : indexes.values()) {
            idx.close();
        }
    }
}
