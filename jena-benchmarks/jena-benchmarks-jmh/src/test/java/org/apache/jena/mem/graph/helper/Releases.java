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
package org.apache.jena.mem.graph.helper;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * Proviced the release specific access to the helper classes for working with graphs, triples and nodes.
 */
public class Releases {

    public static GraphTripleNodeHelper<Graph, Triple, Node> current = new GraphTripleNodeHelperCurrent();
    public static GraphTripleNodeHelper<org.apache.shadedJena480.graph.Graph, org.apache.shadedJena480.graph.Triple, org.apache.shadedJena480.graph.Node> v480 = new GraphTripleNodeHelper480();

}
