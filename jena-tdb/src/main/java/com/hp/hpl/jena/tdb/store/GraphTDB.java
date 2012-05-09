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

package com.hp.hpl.jena.tdb.store;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.Reorderable ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.sys.Session ;

public interface GraphTDB extends Graph, Closeable, Sync, Reorderable, Session
{
    public NodeTupleTable getNodeTupleTable() ;
    public Tuple<Node> asTuple(Triple triple) ;
    
    /** Get a lock that is shared for all graphs from the same dataset (it is the dataset lock) */
    public Lock getLock() ;
    
    /**
     * Return the graph node for this graph if it's in a quad table, else return
     * null for a triple table based (e.g. the default graph of a dataset)
     */ 
    public Node getGraphNode() ;
    
    /** Return the TDB-backed daatset for this graph.
     *  Maybe null - indicating it's a simple graph backed by TDB
     *  (and also the concrete default graph) 
     */
    public DatasetGraphTDB getDataset() ;
    
    public Location getLocation() ; 
}
