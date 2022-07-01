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

package org.apache.jena.fuseki;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.sse.SSE;

class ServerTest {
    public static final String  graphName1    = "http://graph/1";
    public static final String  graphName2    = "http://graph/2";
    public static final String  graphName99   = "http://graph/99";

    public static final Node    gn1           = NodeFactory.createURI("http://graph/1");
    public static final Node    gn2           = NodeFactory.createURI("http://graph/2");
    public static final Node    gn99          = NodeFactory.createURI("http://graph/99");

    public static final Graph   graph1        = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))");
    public static final Graph   graph2        = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))");
}
