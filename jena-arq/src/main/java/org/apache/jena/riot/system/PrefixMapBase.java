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

package org.apache.jena.riot.system;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.ActionKeyValue;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Abstract base implementation of a {@link PrefixMap} which provides
 * some useful helper methods
 * 
 */
public abstract class PrefixMapBase implements PrefixMap {

    protected boolean strSafeFor(String str, char ch) {
        return str.indexOf(ch) == -1;
    }

    protected String canonicalPrefix(String prefix) {
        if (prefix.endsWith(":"))
            return prefix.substring(0, prefix.length() - 1);
        return prefix;
    }
    
    @Override
    public Map<String, IRI> getMappingCopy() {
        return new HashMap<>(this.getMapping());
    }
    
    @Override
    public Map<String, String> getMappingCopyStr() {
        final Map<String, String> smap = new HashMap<>();
        ActionKeyValue<String, IRI> action = new ActionKeyValue<String, IRI>() {
            @Override
            public void apply(String key, IRI value) {
                String str = value.toString();
                smap.put(key, str);
            }
        };
        Iter.apply(getMapping(), action);
        return smap;
    }
    
    @Override
    public void add(String prefix, String iriString) {
        this.add(prefix, IRIFactory.iriImplementation().create(iriString));
    }
    
    @Override
    public void putAll(PrefixMap pmap) {
        for (Map.Entry<String, IRI> e : pmap.getMapping().entrySet()) {
            this.add(e.getKey(), e.getValue());
        }
    }

    @Override
    public void putAll(PrefixMapping pmap) {
        putAll(pmap.getNsPrefixMap()) ;
    }

    @Override
    public void putAll(Map<String, String> mapping) {
        for (Map.Entry<String, String> e : mapping.entrySet()) {
            this.add(e.getKey(), e.getValue());
        }
    }

    /**
     * Abbreviate an IRI or return a pair of prefix and local parts.
     * 
     * @param uriStr
     *            URI string to abbreviate
     * @param turtleSafe
     *            Only return legal Turtle local names.
     */
    protected Pair<String, String> abbrev(Map<String, IRI> prefixes, String uriStr, boolean checkLocalPart) {
        for (Entry<String, IRI> e : prefixes.entrySet()) {
            String uriForPrefix = e.getValue().toString();

            if (uriStr.startsWith(uriForPrefix)) {
                String ln = uriStr.substring(uriForPrefix.length());
                if (!checkLocalPart || this.isSafeLocalPart(ln))
                    return Pair.create(e.getKey(), ln);
            }
        }
        return null;
    }
    
    @Override
    public String expand(String prefixedName) {
        int i = prefixedName.indexOf(':');
        if (i < 0)
            return null;
        return expand(prefixedName.substring(0, i), prefixedName.substring(i + 1));
    }
    
    /**
     * Is a local name safe? Default is a fast check for Turtle-like local names.
     * @param ln Local name
     * @return True if safe, false otherwise
     */
    protected boolean isSafeLocalPart(String ln) {
        // This test isn't complete but covers the common issues that arise. 
        // Does not consider possible escaping.
        return (strSafeFor(ln, '/') && strSafeFor(ln, '#') && strSafeFor(ln, ':'));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        boolean first = true;

        for (Entry<String, IRI> e : this.getMapping().entrySet()) {
            String prefix = e.getKey();
            IRI iri = e.getValue();
            if (first)
                first = false;
            else
                sb.append(" ,");
            sb.append(prefix);
            sb.append(":=");
            sb.append(iri.toString());
        }
        sb.append(" }");
        return sb.toString();
    }
}