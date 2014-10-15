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


import java.util.Iterator ;

import org.apache.jena.atlas.iterator.NullIterator ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.tdb.lib.TupleLib ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;


/** TripleTable - a collection of TupleIndexes for 3-tuples
 *  together with a node table.
 *   Normally, based on 3 indexes (SPO, POS, OSP) but other
 *   indexing structures can be configured.
 *   The node table form can map to and from NodeIds (longs)
 */

public class TripleTable extends TableBase
{
    public TripleTable(TupleIndex[] indexes, NodeTable nodeTable, DatasetControl policy)
    {
        super(3, indexes, nodeTable, policy) ;
        //table = new NodeTupleTableConcrete(3, indexes, nodeTable, policy) ;
    }
    
    public boolean add( Triple triple ) 
    { 
        return add(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    public boolean add(Node s, Node p, Node o) 
    { 
        return table.addRow(s, p, o) ;
    }
    
    /** Delete a triple  - return true if it was deleted, false if it didn't exist */
    public boolean delete( Triple triple ) 
    { 
        return delete(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    /** Delete a triple  - return true if it was deleted, false if it didn't exist */
    public boolean delete(Node s, Node p, Node o) 
    { 
        return table.deleteRow(s, p, o) ;
    }

    /** Find matching triples */
    public Iterator<Triple> find(Node s, Node p, Node o)
    {
        Iterator<Tuple<NodeId>> iter = table.findAsNodeIds(s, p, o) ;
        if ( iter == null )
            return new NullIterator<>() ;
        Iterator<Triple> iter2 = TupleLib.convertToTriples(table.getNodeTable(), iter) ;
        return iter2 ;
    }
    
    private static Transform<Tuple<Node>, Triple> action = new Transform<Tuple<Node>, Triple>(){
        @Override
        public Triple convert(Tuple<Node> item)
        {
            return new Triple(item.get(0), item.get(1), item.get(2)) ;
        }} ; 
   
    /** Clear - does not clear the associated node tuple table */
    public void clearTriples()
    { table.clear() ; }
}
