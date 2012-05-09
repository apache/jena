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

package com.hp.hpl.jena.sparql.util.graph;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;


public class GNode
{
    public final Findable findable ;
    public final Node node ;
    
    public GNode(Graph graph, Node node)
    { this.findable = new FindableGraph(graph) ; this.node = node ; }
    
    public GNode(BasicPattern triples, Node node)
    { this.findable = new FindableBasicPattern(triples) ; this.node = node ; }
    
    public GNode(GNode other, Node node)
    { this.findable = other.findable ; this.node = node ; }

    @Override
    public String toString() { return "gnode:"+node ; }
}
