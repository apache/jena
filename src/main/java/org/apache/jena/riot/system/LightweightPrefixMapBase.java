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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;

/**
 * Abstract base implementation of a {@link LightweightPrefixMap} which provides
 * some useful helper methods
 * 
 */
public abstract class LightweightPrefixMapBase implements LightweightPrefixMap {

    protected boolean strSafeFor(String str, char ch) {
        return str.indexOf(ch) == -1;
    }

    protected String canonicalPrefix(String prefix) {
        if (prefix.endsWith(":"))
            return prefix.substring(0, prefix.length() - 1);
        return prefix;
    }

    /**
     * Abbreviate an IRI or return a pair of prefix and local parts.
     * 
     * @param uriStr
     *            URI string to abbreviate
     * @param turtleSafe
     *            Only return legal Turtle local names.
     */
    protected Pair<String, String> abbrev(Map<String, IRI> prefixes, String uriStr, boolean turtleSafe) {
        for (Entry<String, IRI> e : prefixes.entrySet()) {
            String uriForPrefix = e.getValue().toString();

            if (uriStr.startsWith(uriForPrefix)) {
                String ln = uriStr.substring(uriForPrefix.length());
                if (!turtleSafe || this.isTurtleSafe(ln))
                    return Pair.create(e.getKey(), ln);
            }
        }
        return null;
    }
    
    /**
     * Is a local name safe for Turtle
     * @param ln Local name
     * @return True if safe, false otherwise
     */
    protected final boolean isTurtleSafe(String ln) {
        return (strSafeFor(ln, '/') && strSafeFor(ln, '#') && strSafeFor(ln, ':'));
    }
}