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

package com.hp.hpl.jena.tdb.graph;

import com.hp.hpl.jena.graph.BulkUpdateHandler ;
import com.hp.hpl.jena.graph.GraphEvents ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

public class BulkUpdateHandlerTDB extends SimpleBulkUpdateHandler implements BulkUpdateHandler
{
    private final GraphTDB graphTDB ; 
    public BulkUpdateHandlerTDB(GraphTDB graph)
    {
        super(graph) ;
        this.graphTDB = graph ;
    }

    @Deprecated
    @Override
    public void remove(Node s, Node p, Node o)
    {
        s = fix(s) ;
        p = fix(p) ;
        o = fix(o) ;
        graphTDB.remove(s, p, o) ;
        manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) );
    }

    private static Node fix(Node n) { return (n!=null)? n : Node.ANY ; }
    
    @Deprecated
    @Override
    public void removeAll()
    {
        graphTDB.remove(null, null, null) ;
        notifyRemoveAll(); 
    }
}
