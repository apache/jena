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

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.iri.IRI;

// UNUSED
/**
 * Implementation of a {@link LightweightPrefixMap} which extends another prefix
 * map without ever altering the parent
 */
public class PrefixMap2 extends PrefixMap {
    LightweightPrefixMap parent;
    LightweightPrefixMap local;

    public PrefixMap2(LightweightPrefixMap ext) {
        this.parent = ext;
        this.local = new PrefixMap();
    }

    /** Add a prefix, overwites any existing association */
    @Override
    public void add(String prefix, IRI iri) {
        prefix = canonicalPrefix(prefix);
        // Add to local always.
        local.add(prefix, iri);
    }

    /** Add a prefix, overwites any existing association */
    @Override
    public void delete(String prefix) {
        prefix = canonicalPrefix(prefix);
        local.delete(prefix);
        if (parent.contains(prefix))
            Log.warn(this, "Attempt to delete a prefix in the parent");
    }

    /** Expand a prefix, return null if it can't be expanded */
    @Override
    public String expand(String prefix, String localName) {
        prefix = canonicalPrefix(prefix);
        String x = local.expand(prefix, localName);
        if (x != null)
            return x;
        return parent.expand(prefix, localName);
    }
}
