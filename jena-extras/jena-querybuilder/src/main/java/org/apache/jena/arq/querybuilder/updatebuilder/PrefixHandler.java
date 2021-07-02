/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.updatebuilder;

import java.util.Map;

import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.shared.PrefixMapping;

/**
 * The prefix handler for the updatebuilder class
 *
 */
public class PrefixHandler {

    // the prefix mapping we are handling
    private final PrefixMapping pMap;

    /**
     * Constructor. <b>May modify the contents of the provided prefix mapping</b>
     * 
     * @param pMap The prefix map to handle.
     */
    public PrefixHandler(PrefixMapping pMap) {
        this.pMap = pMap;
    }

    /**
     * Constructor. Creates and empty prefix mapping
     */
    public PrefixHandler() {
        this.pMap = PrefixMapping.Factory.create();
    }

    /**
     * get the canonical prefix name.
     * 
     * Removes ':' from the end of the name if present.
     * 
     * @param x The prefix name
     * @return The prefix name with the trailing ':' removed.
     */
    private static String canonicalPfx(String x) {
        if (x.endsWith(":"))
            return x.substring(0, x.length() - 1);
        return x;
    }

    /**
     * Add a prefix to the prefix mapping.
     * 
     * @param pfx The prefix to add.
     * @param uri The uri to resolve the prefix to.
     */
    public void addPrefix(String pfx, String uri) {
        pMap.setNsPrefix(canonicalPfx(pfx), uri);
    }

    /**
     * Clear the prefix mapping.
     */
    public void clearPrefixes() {
        pMap.clearNsPrefixMap();
    }

    /**
     * Add the map of prefixes to the query prefixes.
     * 
     * @param prefixes The map of prefixs to URIs.
     */
    public void addPrefixes(Map<String, String> prefixes) {
        for (Map.Entry<String, String> e : prefixes.entrySet()) {
            addPrefix(e.getKey(), e.getValue());
        }
    }

    /**
     * Get the prefix mapping
     * 
     * @return the prefix mapping object.
     */
    public PrefixMapping getPrefixes() {
        return pMap;
    }

    /**
     * Get the expression factory based on the prefix mapping.
     * 
     * @return an Expression Factory.
     */
    public ExprFactory getExprFactory() {
        return new ExprFactory(pMap);
    }

    /**
     * Add prefixes from a prefix mapping.
     * 
     * @param prefixes The prefix mapping to add from.
     */
    public void addPrefixes(PrefixMapping prefixes) {
        pMap.setNsPrefixes(prefixes);
    }

}
