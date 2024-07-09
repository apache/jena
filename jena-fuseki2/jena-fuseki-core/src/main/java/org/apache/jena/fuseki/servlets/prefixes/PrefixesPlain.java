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

package org.apache.jena.fuseki.servlets.prefixes;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;

/**
 * {@link PrefixesAccess} implementation using an in-memory
 * {@code ConcurrentHashMap}.
 */
public class PrefixesPlain implements PrefixesAccess {
    private final TransactionalLock transactional;
    private Map<String, String> pairs = new ConcurrentHashMap<>();
    public Map<String, String> getPairs() {
        return pairs;
    }

    public PrefixesPlain() {
        this.transactional = TransactionalLock.createMRSW();
    }

    @Override
    public Transactional transactional() { return transactional; }

    @Override
    public Optional<String> fetchURI(String prefix) {
        Objects.requireNonNull(prefix);
        return Optional.ofNullable(this.pairs.get(prefix));
    }

    @Override
    public void updatePrefix(String prefix, String uri) {
        this.pairs.put(prefix, uri);
    }

    @Override
    public void removePrefix(String prefixToRemove) {
        this.pairs.remove(prefixToRemove);
    }

    @Override
    public Map<String, String> getAll() {
        return this.pairs;
    }

    @Override
    public List<String> fetchPrefix(String uri) {
        Objects.requireNonNull(uri);
        List<String> prefixList = new ArrayList<>();
        for (String prefix : pairs.keySet()) {
            if (pairs.get(prefix).equals(uri))
                prefixList.add(prefix);
        }
        return prefixList;
    }


}
