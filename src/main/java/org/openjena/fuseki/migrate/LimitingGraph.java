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

package org.openjena.fuseki.migrate;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.GraphWithPerform;

import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.shared.AddDeniedException;

public class LimitingGraph extends WrappedGraph implements GraphWithPerform
{
    int limit = 10000 ;
    int count = 0 ;
    
    LimitingBulkUpdateHandler bulk ;
    
    public LimitingGraph(Graph graph, int triplesLimit)
    {
        super(graph) ;
        this.limit = triplesLimit ;
        bulk = new LimitingBulkUpdateHandler(this, graph.getBulkUpdateHandler()) ;
    }

    @Override
    public void add( Triple t ) throws AddDeniedException
    {
        count++ ;
        checkSize() ;
        super.add(t) ;
    }

    /** returns this Graph's bulk-update handler */
    @Override
    public BulkUpdateHandler getBulkUpdateHandler()
    {
        return bulk ;
    }
    
    void checkSize()
    {
        if ( count > limit )
            throw new AddDeniedException("Attempt to exceed graph limit ("+limit+")") ;
    }
}
