/**
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

/** Override {@link DatasetGraph#addGraph} so that it always copies
 * content from the added graph data.
 */

/*package*/ class DatasetGraphCopyAdd extends DatasetGraphWrapper 
{
    public DatasetGraphCopyAdd(DatasetGraph dsg) {
        super(dsg);
    }
    
    @Override
    public void addGraph(Node graphName, Graph graph) {
        graph.find(null,null,null).forEachRemaining(t-> {
            Quad q = Quad.create(graphName, t) ;
            super.add(q) ;
        }) ;
    }
}