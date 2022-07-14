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

package org.apache.jena.fuseki.access;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/** A {@link SecurityContext} that provides no access itself but indicates that the set of visible graphs should be
 * constructed from a set embedded in the query. Use {@link SecurityContextDynamic#instantiateFromQuery} to create the
 * query-specific set of graphs to filter.
 * This context is not meant to be used directly by end users: The initial query has a list of visible graphs prepended
 * before execution based on use-case specific criteria, with the following format:
 *
 * #pragma acl.graphs <graph1>|<graph2>|..|<graphN>
 *
 */
public class SecurityContextDynamic extends SecurityContextAllowNone {

    public static final Node dynamicAccess = NodeFactory.createURI("urn:jena:accessGraphsDynamic");

    public static final String GRAPH_PRAGMA_DELIMITER = "|";
    private static final Pattern GRAPH_PRAGMA_DELIMITER_PATTERN = Pattern.compile("\\" + GRAPH_PRAGMA_DELIMITER);
    private static final String GRAPH_PRAGMA_PATTERN_GROUP = "graphs";
    private static final Pattern GRAPH_PRAGMA_PATTERN = Pattern.compile(
        "^" +
        // Leading whitespace
        "\\s*" +
        // For simplicity, expect pragma on first non-whitespace line
        "#pragma acl\\.graphs\\h+(?<" + GRAPH_PRAGMA_PATTERN_GROUP + ">\\S*)\\h*\\v" +
        // Anything else
        ".*",
        Pattern.DOTALL
    );

    public static SecurityContext forQuery(String queryString) {
        if ( queryString != null ) {
            Matcher m = GRAPH_PRAGMA_PATTERN.matcher(queryString);
            if ( m.matches() ) {
                String[] graphs = GRAPH_PRAGMA_DELIMITER_PATTERN
                    .splitAsStream(m.group(GRAPH_PRAGMA_PATTERN_GROUP))
                    .map(String::trim)
                    .toArray(String[]::new);
                if ( graphs.length > 0 ) {
                    return new SecurityContextView(graphs);
                }
            }
        }
        /* Note: SecurityContext.ALL is not supported since according to AssemblerSecurityRegistry support for
         *  urn:jena:accessAllGraphs (*) and urn:jena:accessAllNamedGraphs (**) is unfinished.
         */
        return SecurityContext.NONE;
    }
}
