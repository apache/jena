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

package org.apache.jena.fuseki.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Management of intern'ed names {@code T} that can be used as keys. {@link #alloc}
 * creates an intern'ed {@code T}; if the object with the same name has already been
 * created, return the original. There is only ever one object for a given name.
 * <p>
 * {@code T ==} can be used to test of name match, though providing
 * {@code .hashCode} and {@code .equals} is preferred.
 */
public class NameMgr<T> {
    private final Map<String, T> registered = new ConcurrentHashMap<>();
    
    public NameMgr() { }
    
    /** register, creating an object if necessary */
    public T alloc(String name, Function<String, T> maker) {
        return registered.computeIfAbsent(name, maker);
    }

}
