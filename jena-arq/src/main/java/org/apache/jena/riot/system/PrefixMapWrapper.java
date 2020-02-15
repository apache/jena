/**
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

package org.apache.jena.riot.system;

import java.util.Map ;
import java.util.function.BiConsumer ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.shared.PrefixMapping ;

public class PrefixMapWrapper implements PrefixMap
{
    private final PrefixMap other ;
    protected PrefixMap get() { return other; }

    public PrefixMapWrapper(PrefixMap other) { this.other = other ; }

    @Override
    public Map<String, String> getMapping()
    { return get().getMapping() ; }

    @Override
    public Map<String, String> getMappingCopy()
    { return get().getMappingCopy() ; }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        get().forEach(action);
    }

    @Override
    public void add(String prefix, String iri)
    { get().add(prefix, iri) ; }

    @Override
    public void putAll(PrefixMap pmap)
    { get().putAll(pmap) ; }

    @Override
    public void putAll(PrefixMapping pmap)
    { get().putAll(pmap) ; }

    @Override
    public void putAll(Map<String, String> mapping)
    { get().putAll(mapping) ; }

    @Override
    public void delete(String prefix)
    { get().delete(prefix) ; }

    @Override
    public void clear()
    { get().clear(); }

    @Override
    public boolean containsPrefix(String prefix)
    { return get().containsPrefix(prefix) ; }

    @Override
    public String abbreviate(String uriStr)
    { return get().abbreviate(uriStr) ; }

    @Override
    public Pair<String, String> abbrev(String uriStr)
    { return get().abbrev(uriStr) ; }

    @Override
    public String expand(String prefixedName)
    { return get().expand(prefixedName) ; }

    @Override
    public String expand(String prefix, String localName)
    { return get().expand(prefix, localName) ; }

    @Override
    public boolean isEmpty()
    { return get().isEmpty() ; }

    @Override
    public int size()
    { return get().size() ; }
}
