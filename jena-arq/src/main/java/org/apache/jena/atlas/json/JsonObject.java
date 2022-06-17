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

package org.apache.jena.atlas.json;

import java.util.*;
import java.util.Map.Entry ;
import java.util.function.BiConsumer ;
import java.util.stream.Stream;

public class JsonObject extends JsonValue
{
    private final Map<String, JsonValue> map = new LinkedHashMap<>() ;

    public JsonObject() {}

    @Override
    public boolean isObject()       { return true ; }
    @Override
    public JsonObject getAsObject() { return this ; }

    @Override
    public void visit(JsonVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public int hashCode() {
        return map.hashCode() ;
    }

    @Override
    public boolean equals(Object other) {
        if ( !(other instanceof JsonObject) )
            return false ;
        return map.equals(((JsonObject)other).map) ;
    }

    public void clear()
    { map.clear() ; }

    public boolean hasKey(Object key) {
        return map.containsKey(key) ;
    }

    public Set<String> keys() {
        return map.keySet() ;
    }

    public Set<Entry<String, JsonValue>> entrySet() {
        return map.entrySet() ;
    }

    public JsonValue get(String key) {
        return map.get(key) ;
    }

    /** For walking structures */
    public JsonObject getObj(String key) {
        return get(key).getAsObject() ;
    }

    /** For walking structures */
    public Number getNumber(String key) {
        return get(key).getAsNumber().value();
    }

    /** For walking structures */
    public String getString(String key) {
        return get(key).getAsString().value();
    }

    /** For walking structures */
    public boolean getBoolean(String key) {
        return get(key).getAsBoolean().value();
    }

    /** For walking structures */
    public Stream<JsonValue> getArray(String key) {
        return get(key).getAsArray().stream();
    }

    /** For walking structures */
    public Iterator<JsonValue> getIterator(String key) {
        return get(key).getAsArray().iterator();
    }

    public boolean isEmpty() {
        return map.isEmpty() ;
    }

    public Set<String> keySet() {
        return map.keySet() ;
    }

    public void forEach(BiConsumer<String, JsonValue> action) {
        map.forEach(action) ;
    }

    public JsonValue put(String key, JsonValue value) {
        return map.put(key, value) ;
    }

    public JsonValue put(String key, String value) {
        return map.put(key, new JsonString(value)) ;
    }

    public JsonValue put(String key, long value) {
        return map.put(key, JsonNumber.value(value)) ;
    }

    public JsonValue put(String key, boolean b) {
        return map.put(key, new JsonBoolean(b)) ;
    }

    public void putAll(Map<? extends String, ? extends JsonValue> m) {
        map.putAll(m) ;
    }

    public JsonValue remove(Object key) {
        return map.remove(key) ;
    }

    public int size() {
        return map.size() ;
    }

    public Collection<JsonValue> values() {
        return map.values() ;
    }
}
