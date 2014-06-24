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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.iri.IRI;

// UNUSED
/**
 * Implementation of a {@link PrefixMap} which extends another prefix map
 * without ever altering the parent.
 * <p>
 * This allows code to modify a prefix map based off of another map without
 * modifying that original map. This is somewhat different than simply making a
 * copy of an existing map since this implementation is partly a view over
 * another map so will reflect changes that happen to the other map.
 * </p>
 */
public class PrefixMapExtended extends PrefixMapBase {
    PrefixMap parent;
    PrefixMap local;

    /**
     * Creates an extended prefix map
     * 
     * @param ext
     *            Prefix Map to extend
     */
    public PrefixMapExtended(PrefixMap ext) {
        if (ext == null)
            throw new IllegalArgumentException("Prefix Map to extend cannot be null");
        this.parent = ext;
        this.local = PrefixMapFactory.create();
    }

    @Override
    public void add(String prefix, IRI iri) {
        prefix = canonicalPrefix(prefix);
        // Add to local always.
        local.add(prefix, iri);
    }

    @Override
    public void delete(String prefix) {
        prefix = canonicalPrefix(prefix);
        local.delete(prefix);
        if (parent.contains(prefix))
            Log.warn(this, "Attempt to delete a prefix in the parent");
    }

    @Override
    public String expand(String prefix, String localName) {
        prefix = canonicalPrefix(prefix);
        String x = local.expand(prefix, localName);
        if (x != null)
            return x;
        return parent.expand(prefix, localName);
    }

    @Override
    public Map<String, IRI> getMapping() {
        Map<String, IRI> mapping = new HashMap<>();
        mapping.putAll(parent.getMapping());
        mapping.putAll(local.getMapping());
        return Collections.unmodifiableMap(mapping);
    }

    @Override
    public boolean contains(String prefix) {
        return local.contains(prefix) || parent.contains(prefix);
    }

    @Override
    public String abbreviate(String uriStr) {
        String x = local.abbreviate(uriStr);
        if (x != null)
            return x;
        return parent.abbreviate(uriStr);
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        Pair<String, String> p = local.abbrev(uriStr);
        if (p != null)
            return p;
        return parent.abbrev(uriStr);
    }
    
    @Override
    public boolean isEmpty()
    {
        return parent.isEmpty() && local.isEmpty() ;
    }

    @Override
    public int size()
    {
        return parent.size() + local.size() ;
    }
}
