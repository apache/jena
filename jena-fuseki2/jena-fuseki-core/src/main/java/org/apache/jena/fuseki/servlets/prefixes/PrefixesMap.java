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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.Transactional;

/**
 * {@link PrefixesAccess} implementation using a {@link PrefixMap}.
 * This is the implementation used by {@link ActionPrefixesBase}
 * which forms the way the prefixes operation is added to a Fuseki
 * service.
 */
public class PrefixesMap implements PrefixesAccess {
    private final PrefixMap prefixMap;
    private final Transactional transactional;

    public PrefixesMap(PrefixMap prefixMap, Transactional transactional) {
        this.transactional = transactional;
        this.prefixMap = prefixMap;
    }

    @Override
    public Transactional transactional() {
        return transactional;
    }

    @Override
    public Optional<String> fetchURI(String prefix) {
        return Optional.ofNullable(prefixMap.get(prefix));
    }

    @Override
    public void updatePrefix(String prefix, String uri) {
        prefixMap.delete(prefix);
        prefixMap.add(prefix, uri);
    }

    @Override
    public void removePrefix(String prefixToRemove) {
        prefixMap.delete(prefixToRemove);
    }

    @Override
    public Map<String, String> getAll() {
        return prefixMap.getMapping();
    }

    @Override
    public List<String> fetchPrefix(String uri) {
        List<String> prefixList = new ArrayList<>();
        for (String prefix : prefixMap.getMapping().keySet()) {
            if (prefixMap.getMapping().get(prefix).equals(uri)) {
                prefixList.add(prefix);
            }
        }
        return prefixList;
    }
}
