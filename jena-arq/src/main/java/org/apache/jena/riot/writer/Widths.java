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

package org.apache.jena.riot.writer;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.PrefixMap;
import static org.apache.jena.riot.writer.WriterConst.RDF_type;

/**
 * Calculate widths to print in.
 * <p>
 * Flag {@code hasPrefixForRDF} is to avoid a reverse lookup in a prefix map.
 */
/*package*/ class Widths {

    /*package*/ static int calcWidth(PrefixMap prefixMap, String baseURI, Node p, boolean printTypeKeyword) {
        String x = prefixMap.abbreviate(p.getURI());
        if ( x == null )
            return p.getURI().length() + 2;
        if ( printTypeKeyword && RDF_type.equals(p) ) {
            // Use "a".
            return 1;
        }
        return x.length();
    }

    /*package*/ static int calcWidth(PrefixMap prefixMap, String baseURI, Collection<Node> nodes, int minWidth, int maxWidth, boolean printTypeKeyword) {
        Node prev = null;
        int nodeMaxWidth = minWidth;

        for ( Node n : nodes ) {
            if ( prev != null && prev.equals(n) )
                continue;
            int len = calcWidth(prefixMap, baseURI, n, printTypeKeyword);
            if ( len > maxWidth )
                continue;
            if ( nodeMaxWidth < len )
                nodeMaxWidth = len;
            prev = n;
        }
        return nodeMaxWidth;
    }
 
    /*package*/ static int calcWidthTriples(PrefixMap prefixMap, String baseURI, Collection<Triple> triples, int minWidth, int maxWidth, boolean printTypeKeyword) {
        Node prev = null;
        int nodeMaxWidth = minWidth;

        for ( Triple triple : triples ) {
            Node n = triple.getPredicate();
            if ( prev != null && prev.equals(n) )
                continue;
            int len = calcWidth(prefixMap, baseURI, n, printTypeKeyword);
            if ( len > maxWidth )
                continue;
            if ( nodeMaxWidth < len )
                nodeMaxWidth = len;
            prev = n;
        }
        return nodeMaxWidth;
    }
}
