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

package com.hp.hpl.jena.tdb.store.nodetupletable ;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleTable ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;

public interface NodeTupleTable extends Sync, Closeable
{
    public boolean addRow(Node... nodes) ;

    public boolean deleteRow(Node... nodes) ;

    /** Find by node. */
    public Iterator<Tuple<Node>> find(Node... nodes) ;

    /** Find by node - return an iterator of NodeIds. Can return "null" for not found as well as NullIterator */
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node... nodes) ;

    /** Find by NodeId. */
    public Iterator<Tuple<NodeId>> find(NodeId... ids) ;
    
    /** Find by NodeId. */
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> ids) ;

    /** Find all tuples */ 
    public Iterator<Tuple<NodeId>> findAll() ;

    /** Return the undelying tuple table - used with great care by tools
     * that directly manipulate internal structures. 
     */
    public TupleTable getTupleTable() ;

    /** Return the node table */
    public NodeTable getNodeTable() ;

    public boolean isEmpty() ;
    
    /** Clear the tuple table.  After this operation, find* will find  nothing.
     * This does not mean all data has been removed - for example, it does not mean
     * that any node table has been emptied.
     */
    public void clear() ;

    // No clear operation - need to manage the tuple table 
    // and node tables separately.
    
    public long size() ;

    /** Return the current policy, if any, for this NodeTupleTable */
    public DatasetControl getPolicy() ;
}
