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

package org.apache.jena.dboe.storage.prefixes;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapBase;

/**
 * (Temporary) adapter class of {@link org.apache.jena.riot.system.PrefixMap} over it's
 * replacement {@link PrefixMapI}.
 */
public class PrefixMapOverPrefixMapI extends PrefixMapBase implements PrefixMap {
    
    private final PrefixMapI pmapi;

    public static PrefixMap create(PrefixMapI pmap) {
        return new PrefixMapOverPrefixMapI(pmap);
    }

    private PrefixMapOverPrefixMapI(PrefixMapI pmapi) {
        this.pmapi = pmapi;
    }
    
    private static IRI iri(String iriStr) {
        return IRIResolver.iriFactory().create(iriStr);
    }

    private static String str(IRI iri) {
        return iri.toString();
    }

    @Override
    public Map<String, IRI> getMapping() {
        Map<String, IRI> map = pmapi.stream()
            .collect(Collectors.toMap((e) -> e.getPrefix(), (e) -> iri(e.getUri()) ));
        return map;
    }

    @Override
    public void add(String prefix, IRI iri) {
        pmapi.add(prefix, str(iri));
    }

    @Override
    public void delete(String prefix) {
        pmapi.delete(prefix);
    }

    @Override
    public void clear() {
        pmapi.clear();
    }

    @Override
    public boolean contains(String prefix) {
        return pmapi.containPrefix(prefix);
    }

    @Override
    public String abbreviate(String uriStr) {
        return pmapi.abbreviate(uriStr);
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        return pmapi.abbrev(uriStr);
    }

    @Override
    public String expand(String prefix, String localName) {
        return pmapi.expand(prefix, localName);
    }

    @Override
    public boolean isEmpty() {
        return pmapi.isEmpty();
    }

    @Override
    public int size() {
        return pmapi.size();
    }
}
