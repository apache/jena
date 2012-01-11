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


package org.apache.jena.fuseki.migrate;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.WrappedBulkUpdateHandler;

public class LimitingBulkUpdateHandler extends WrappedBulkUpdateHandler
{
    LimitingGraph lGraph ;
    
    public LimitingBulkUpdateHandler(LimitingGraph graph, BulkUpdateHandler bulk)
    {
        super(graph, bulk) ;
        this.lGraph = graph ;
    }
    
    @Override
    public void add( Triple [] triples )
    {
        lGraph.count = lGraph.count+triples.length ;
        lGraph.checkSize() ;
        super.add(triples) ;
    }
    
    @Override
    public void add( List<Triple> triples )
    {
        lGraph.count = lGraph.count+triples.size() ;
        lGraph.checkSize() ;
        super.add(triples);
    }
    
    @Override
    public void add( Iterator<Triple> it )
    {
        for ( ; it.hasNext() ; )
        {
            Triple t = it.next() ;
            lGraph.count++ ;
            lGraph.checkSize() ;
            graph.add(t) ;
        }
    }
    
    @Override
    public void add( Graph g, boolean withReifications )
    {
        // Not perfect
        lGraph.count = lGraph.count+g.size() ;
        lGraph.checkSize() ;
        super.add(g, withReifications) ;
    }
    
    @Override
    public void add( Graph g )
    {
        lGraph.count = lGraph.count+g.size() ;
        lGraph.checkSize() ;
        super.add( g );
    }
}
