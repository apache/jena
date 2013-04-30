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

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;

public interface GraphTDB extends Graph, Closeable, Sync
{
    /** The NodeTupleTable for this graph */ 
    public NodeTupleTable getNodeTupleTable() ;
    
    /**
     * Return the graph node for this graph if it's in a quad table, else return
     * null for a triple table based (e.g. the default graph of a dataset)
     */ 
    public Node getGraphNode() ;
    
    /** Return the TDB-backed dataset for this graph */
    public DatasetGraphTDB getDataset() ;
}
