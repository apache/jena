/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.dboe.storage.prefixes;

import java.util.Objects;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * Algorithms over {@link PrefixMapI} to abbreviate and expand
 */
public class PrefixLib {

    /**
     * Remove ":" from a prefix if necessary to make it canonical.
     * @param prefix
     * @return prefix, without colon.
     */
    public static String canonicalPrefix(String prefix) {
        if ( prefix.endsWith(":") )
            return prefix.substring(0, prefix.length() - 1);
        return prefix;
    }

    /** Canonical name for graphs */
    public static Node canonicalGraphName(Node graphName) {
        if ( graphName == StoragePrefixes.nodeDefaultGraph) 
            return graphName;
        if ( graphName == null || Quad.isDefaultGraph(graphName) )
            return StoragePrefixes.nodeDefaultGraph;
        return graphName;
    }

    /**
     * Is this the canonical, internal marker for the default graph for storage
     * prefixes? ({@link StoragePrefixes#nodeDefaultGraph})
     * 
     * @param graphName
     */
    public static boolean isNodeDefaultGraph(Node graphName) {
        return Objects.equals(StoragePrefixes.nodeDefaultGraph, graphName);
    }

    /** abbreviate a uriStr, giving a string as a short form. If not possible return null.
     * This does not guarantee that the result is suitable for all RDF syntaxes.
     * Further checking for the rules of a particular syntax are necessary.
     */
    public static String abbreviate(PrefixMapI pmap, String uriStr) {
        for ( PrefixEntry e : pmap ) {
            String prefix = e.getPrefix();
            String prefixUri = e.getUri();
            if ( uriStr.startsWith(prefixUri) ) {
                String ln = uriStr.substring(prefixUri.length());
                if ( strSafeFor(ln, '/') && strSafeFor(ln, '#') && strSafeFor(ln, ':') )
                    return prefix + ":" + ln;
            }
        }
        return null;
    }

    private static boolean strSafeFor(String str, char ch) {
        return str.indexOf(ch) == -1;
    }

    /**
     * Abbreviate a uriStr, return the prefix and local parts.
     * This does not guarantee that the result is suitable for all RDF syntaxes.
     */
    public static Pair<String, String> abbrev(PrefixMapI prefixes, String uriStr) {
        for ( PrefixEntry e : prefixes ) {
            String uriForPrefix = e.getUri();
            if ( uriStr.startsWith(uriForPrefix) )
                return Pair.create(e.getPrefix(), uriStr.substring(uriForPrefix.length()));
        }
        return null;
    }

    /** Expand a prefixedName which must include a ':' */
    public static String expand(PrefixMapI prefixes, String prefixedName) {
        int i = prefixedName.indexOf(':');
        if ( i < 0 )
            return null;
        return expand(prefixes, prefixedName.substring(0, i), prefixedName.substring(i + 1));
    }

    /** Expand a prefix, local name pair. */
    public static String expand(PrefixMapI prefixes, String prefix, String localName) {
        prefix = canonicalPrefix(prefix);
        String x = prefixes.get(prefix);
        if ( x == null )
            return null;
        return x + localName;
    }

}
