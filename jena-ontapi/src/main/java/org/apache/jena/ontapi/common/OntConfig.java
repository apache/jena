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

package org.apache.jena.ontapi.common;

import org.apache.jena.ontapi.model.OntModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration to control {@link OntModel} and {@link OntPersonality} behavior.
 */
public class OntConfig {
    public static final OntConfig DEFAULT = new OntConfig();

    private final Map<String, Object> settings;

    public OntConfig() {
        this(Map.of());
    }

    protected OntConfig(Map<String, Object> settings) {
        this.settings = Map.copyOf(Objects.requireNonNull(settings));
    }

    public boolean getBoolean(Enum<?> key) {
        return getBoolean(key.name());
    }

    public boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IllegalArgumentException("Config contains value for key = " + key + ", but it is not a boolean");
    }

    public Object get(String key) {
        return settings.get(key);
    }

    public OntConfig setTrue(Enum<?> key) {
        return setBoolean(key, true);
    }

    public OntConfig setFalse(Enum<?> key) {
        return setBoolean(key, false);
    }

    public OntConfig setBoolean(Enum<?> key, boolean value) {
        return setBoolean(key.name(), value);
    }

    public OntConfig setBoolean(String key, boolean value) {
        return set(key, value);
    }

    public OntConfig set(String key, Object value) {
        Map<String, Object> settings = new HashMap<>(this.settings);
        settings.put(key, value);
        return new OntConfig(settings);
    }
}
