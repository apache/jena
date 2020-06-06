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

package org.apache.jena.shacl.lib;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.graph.GNode;

public class GN {
    
    // Node filter tests.
//public static boolean isURI(GNode n)         { return n != null && isURI(n.getNode()); }
//public static boolean isBlank(GNode n)       { return n != null && isBlank(n.getNode()); }
//public static boolean isLiteral(GNode n)     { return n != null && isLiteral(n.getNode()); }
//public static boolean isResource(GNode n)    { return n != null && isURI(n.getNode())||isBlank(n.getNode()); }



    public static GNode create(Graph graph, Node node) {
        return new GNode(graph, node);
    }

    public static GNode subject(Graph graph, Triple triple) {
        return triple == null ? null : create(graph, triple.getSubject());
    }

    public static GNode predicate(Graph graph, Triple triple) {
        return triple == null ? null : create(graph, triple.getPredicate());
    }

    public static GNode object(Graph graph, Triple triple) {
        return triple == null ? null : create(graph, triple.getObject());
    }
}
