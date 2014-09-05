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
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.lib.TupleLib ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;


/** Quad table - a collection of TupleIndexes for 4-tuples
 *  together with a node table.
 */

public class QuadTable extends TableBase
{
    public QuadTable(TupleIndex[] indexes, NodeTable nodeTable, DatasetControl policy)
    {
        super(4, indexes, nodeTable, policy);
    }

    /** Add a quad - return true if it was added, false if it already existed */
    public boolean add( Quad quad ) 
    { 
        return add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    /** Add a quad (as graph node and triple) - return true if it was added, false if it already existed */
    public boolean add(Node gn, Triple triple ) 
    { 
        return add(gn, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    /** Add a quad - return true if it was added, false if it already existed */
    public boolean add(Node g, Node s, Node p, Node o) 
    { 
        return table.addRow(g,s,p,o) ;
    }
    
    /** Delete a quad - return true if it was deleted, false if it didn't exist */
    public boolean delete( Quad quad ) 
    { 
        return delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    /** Delete a quad (as graph node and triple) - return true if it was deleted, false if it didn't exist */
    public boolean delete( Node gn, Triple triple ) 
    { 
        return delete(gn, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    /** Delete a quad - return true if it was deleted, false if it didn't exist */
    public boolean delete(Node g, Node s, Node p, Node o) 
    { 
        return table.deleteRow(g, s, p, o) ;
    }
    
    /** Find matching quads */
    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
    {
        Iterator<Tuple<NodeId>> iter = table.findAsNodeIds(g, s, p, o) ;
        if ( iter == null )
            return new NullIterator<>() ;
        Iterator<Quad> iter2 = TupleLib.convertToQuads(table.getNodeTable(), iter) ;
        return iter2 ;
    }
    
    private static Transform<Tuple<Node>, Quad> action = new Transform<Tuple<Node>, Quad>(){
        @Override
        public Quad convert(Tuple<Node> item)
        {
            return new Quad(item.get(0), item.get(1), item.get(2), item.get(3)) ;
        }} ; 

    /** Clear - does not clear the associated node tuple table */
    public void clearQuads()
    { table.clear() ; }
}
