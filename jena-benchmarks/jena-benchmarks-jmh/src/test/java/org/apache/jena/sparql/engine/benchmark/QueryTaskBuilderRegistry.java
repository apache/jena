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
package org.apache.jena.sparql.engine.benchmark;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.sparql.engine.join.QueryTaskBuilder480;
import org.apache.jena.sparql.engine.join.QueryTaskBuilderCurrent;

public class QueryTaskBuilderRegistry {
    private static final QueryTaskBuilderRegistry INSTANCE = new QueryTaskBuilderRegistry();

    static {
        INSTANCE.put("current", () -> new QueryTaskBuilderCurrent());
        INSTANCE.put("4.8.0", () -> new QueryTaskBuilder480());
    }

    private final Map<String, Creator<QueryTaskBuilder>> registry = new ConcurrentHashMap<>();

    private QueryTaskBuilderRegistry() {
    }

    public static QueryTaskBuilderRegistry get() {
        return INSTANCE;
    }

    /** Returns a factory for creating fresh instances of QueryTaskBuilder for the given name. */
    public Creator<QueryTaskBuilder> get(String name) {
        return Optional.ofNullable(registry.get(name))
            .orElseThrow(() -> new NoSuchElementException("No task builder with name " + name));
    }

    public void put(String name, Creator<QueryTaskBuilder> builderFactory) {
        registry.put(name, builderFactory);
    }
}
