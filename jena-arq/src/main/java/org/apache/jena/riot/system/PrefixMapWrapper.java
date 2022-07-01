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
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.shared.PrefixMapping ;

public class PrefixMapWrapper implements PrefixMap
{
    private final PrefixMap other ;
    private PrefixMap get() { return other; }

    public PrefixMapWrapper(PrefixMap other) { this.other = other ; }

    protected PrefixMap getR() { return get(); }

    protected PrefixMap getW() { return get(); }

    @Override
    public Map<String, String> getMapping()
    { return getR().getMapping() ; }

    @Override
    public Map<String, String> getMappingCopy()
    { return getR().getMappingCopy() ; }

    @Override
    public void forEach(BiConsumer<String, String> action)
    { getR().forEach(action); }

    @Override
    public Stream<PrefixEntry> stream() {
        return getR().stream();
    }

    @Override
    public String get(String prefix)
    { return getR().get(prefix); }

    @Override
    public void add(String prefix, String iri)
    { getW().add(prefix, iri) ; }

    @Override
    public void putAll(PrefixMap pmap)
    { getW().putAll(pmap) ; }

    @Override
    public void putAll(PrefixMapping pmap)
    { getW().putAll(pmap) ; }

    @Override
    public void putAll(Map<String, String> mapping)
    { getW().putAll(mapping) ; }

    @Override
    public void delete(String prefix)
    { getW().delete(prefix) ; }

    @Override
    public void clear()
    { getW().clear(); }

    @Override
    public boolean containsPrefix(String prefix)
    { return getR().containsPrefix(prefix) ; }

    @Override
    public String abbreviate(String uriStr)
    { return getR().abbreviate(uriStr) ; }

    @Override
    public Pair<String, String> abbrev(String uriStr)
    { return getR().abbrev(uriStr) ; }

    @Override
    public String expand(String prefixedName)
    { return getR().expand(prefixedName) ; }

    @Override
    public String expand(String prefix, String localName)
    { return getR().expand(prefix, localName) ; }

    @Override
    public boolean isEmpty()
    { return getR().isEmpty() ; }

    @Override
    public int size()
    { return getR().size() ; }
}
