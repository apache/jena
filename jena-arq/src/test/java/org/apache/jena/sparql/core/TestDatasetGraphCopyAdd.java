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

package org.apache.jena.sparql.core;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.sse.SSE ;
import static org.junit.Assert.*  ;
import org.junit.Test ;

public class TestDatasetGraphCopyAdd extends AbstractDatasetGraphTests 
{
    @Override
    protected DatasetGraph emptyDataset() { return DatasetGraphFactory.create() ; }
    
    @Test public void copyAdd_01() {
        Graph graph = SSE.parseGraph("(graph (:s :p :o))") ;
        Node g = SSE.parseNode(":g") ;
        DatasetGraph dsg = emptyDataset() ;
        dsg.addGraph(g, graph);
        graph.clear(); 
        assertTrue(graph.isEmpty()) ;
        assertFalse(dsg.getGraph(g).isEmpty()) ;
    }
}
