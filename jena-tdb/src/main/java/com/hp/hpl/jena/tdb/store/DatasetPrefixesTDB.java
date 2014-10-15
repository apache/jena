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

import java.util.* ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.graph.GraphPrefixesProjection ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;

/**
 * Dataset prefixes; a table of nodes with prefix-centric operations. The table
 * is G-P-U where G is a graph name ("" is used for the default graph), P is a
 * string (the prefix) and U is the IRI.
 */
public class DatasetPrefixesTDB implements DatasetPrefixStorage
{
    /* 
     * Almost everythig is cached in the prefix map asociated with the
     * graph or dataset so this table is the persistent form and
     * does nto need a cache of it's own.   
     * 
     */
    
    static final RecordFactory factory = new RecordFactory(3*NodeId.SIZE, 0) ;
    static final String unamedGraphURI = "" ;
    
    private final NodeTupleTable nodeTupleTable ;
    
    public DatasetPrefixesTDB(NodeTupleTable nodeTupleTable) {
        this.nodeTupleTable = nodeTupleTable ;
    }
    
    @Override
    public synchronized void insertPrefix(String graphName, String prefix, String uri) {
        Node g = NodeFactory.createURI(graphName) ; 
        Node p = NodeFactory.createLiteral(prefix) ; 
        Node u = NodeFactory.createURI(uri) ;

        nodeTupleTable.addRow(g,p,u) ;
    }

    @Override
    public Set<String> graphNames() {
        Iterator<Tuple<Node>> iter = nodeTupleTable.find((Node)null, null, null) ;
        Set <String> x = new HashSet<>() ;
        for ( ; iter.hasNext() ; )
            x.add(iter.next().get(0).getURI()) ;
        Iter.close(iter) ;
        return x ;
    }
    
    @Override
    public synchronized String readPrefix(String graphName, String prefix) {
        Node g = NodeFactory.createURI(graphName) ; 
        Node p = NodeFactory.createLiteral(prefix) ; 
        
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, p, null) ;
        try {
            if ( ! iter.hasNext() )
                return null ;
            Tuple<Node> t = iter.next() ;
            Node uri = t.get(2) ;
            return uri.getURI() ;
        } finally { Iter.close(iter) ; } 
    }

    @Override
    public synchronized String readByURI(String graphName, String uriStr) {
        Node g = NodeFactory.createURI(graphName) ; 
        Node u = NodeFactory.createURI(uriStr) ; 
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, null, u) ;
        if ( ! iter.hasNext() )
            return null ;
        Node prefix = iter.next().get(1) ;
        Iter.close(iter) ;
        return prefix.getLiteralLexicalForm()  ;
    }

    @Override
    public synchronized Map<String, String> readPrefixMap(String graphName) {
        Map<String, String> map = new HashMap<>() ;
        // One class of problem from mangled databases
        // (non-transactional, not shutdown cleanly)
        // ends up with NPE access the node table from
        // prefix index. As prefixes are "nice extras", we
        // keep calm and carry on in th eface of exceptions.
        
        Node g = NodeFactory.createURI(graphName) ;
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, null, null) ;
        for ( ; iter.hasNext() ; )
        {
            try {
                Tuple<Node> t = iter.next();
                String prefix = t.get(1).getLiteralLexicalForm() ;
                String uri = t.get(2).getURI() ;
                map.put(prefix, uri) ;
            } catch (Exception ex) { 
                Log.warn(this, "Mangled prefix map: graph name="+graphName, ex) ;
            }
        }
        Iter.close(iter) ;
        return map ;
    }
    
    @Override
    public synchronized void loadPrefixMapping(String graphName, PrefixMapping pmap) {
        Node g = NodeFactory.createURI(graphName) ;
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, null, null) ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<Node> t = iter.next();
            String prefix = t.get(1).getLiteralLexicalForm() ;
            String uri = t.get(2).getURI() ;
            pmap.setNsPrefix(prefix, uri) ;
        }
        Iter.close(iter) ;
    }
    
    @Override
    public synchronized void removeFromPrefixMap(String graphName, String prefix) {
        Node g = NodeFactory.createURI(graphName) ; 
        Node p = NodeFactory.createLiteral(prefix) ; 
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, p, null) ;
        List<Tuple<Node>> list = Iter.toList(iter) ;    // Materialize.
        Iter.close(iter) ;
        for ( Tuple<Node> tuple : list )
            nodeTupleTable.deleteRow(g, p, tuple.get(2)) ;
    }

    public NodeTupleTable getNodeTupleTable()  { return nodeTupleTable ; }
    
    /** Return a PrefixMapping for the unamed graph */
    @Override
    public PrefixMapping getPrefixMapping()
    { return getPrefixMapping(unamedGraphURI) ; }

    /** Return a PrefixMapping for a named graph */
    @Override
    public PrefixMapping getPrefixMapping(String graphName) { 
        PrefixMapping pm = new GraphPrefixesProjection(graphName, this) ;
        // Force into cache.
        // See JENA-81
        pm.getNsPrefixMap() ;
        return pm ;
    }
    
    @Override
    public void close() {
        nodeTupleTable.close() ;
    }

    @Override
    public void sync() {
        nodeTupleTable.sync() ;
    }
}
