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

package org.apache.jena.tdb2.store;

import java.util.* ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.DatasetPrefixStorage ;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;

/**
 * Dataset prefixes; a table of nodes with prefix-centric operations. The table
 * is G-P-U where G is a graph name ("" is used for the default graph), P is a
 * string (the prefix) and U is the IRI.
 */
public class DatasetPrefixesTDB implements DatasetPrefixStorage
{
    // Consider a cache - like PrefixMappingCache was - but needs to respect transactions.
    // See getPrefixMapping.
    
    static final RecordFactory factory = new RecordFactory(3*NodeId.SIZE, 0) ;
    static final String unamedGraphURI = "" ;
    
    private final NodeTupleTable nodeTupleTable ;
    
    public DatasetPrefixesTDB(NodeTupleTable nodeTupleTable) {
        this.nodeTupleTable = nodeTupleTable ;
    }
    
    @Override
    public void loadPrefixMapping(String graphName, PrefixMapping pmap) {
        Node g = NodeFactory.createURI(graphName) ; 
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, null, null) ;
        iter.forEachRemaining(tuple->{
            Node prefix = tuple.get(1)  ;
            Node uri = tuple.get(2) ;
            pmap.setNsPrefix(prefix.getLiteralLexicalForm(), uri.getURI()) ;
        }) ;
    }

    @Override
    public void insertPrefix(String graphName, String prefix, String uri) {
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
    public String readPrefix(String graphName, String prefix) {
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
    public String readByURI(String graphName, String uriStr) {
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
    public Map<String, String> readPrefixMap(String graphName) {
        Map<String, String> map = new HashMap<>() ;
        Node g = NodeFactory.createURI(graphName) ;
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, null, null) ;
        for ( ; iter.hasNext() ; ) {
            try {
                Tuple<Node> t = iter.next() ;
                String prefix = t.get(1).getLiteralLexicalForm() ;
                String uri = t.get(2).getURI() ;
                map.put(prefix, uri) ;
            }
            catch (Exception ex) {
                Log.warn(this, "Mangled prefix map: graph name=" + graphName, ex) ;
            }
        }
        Iter.close(iter) ;
        return map ;
    }
    
    @Override
    public void removeFromPrefixMap(String graphName, String prefix) {
        Node g = NodeFactory.createURI(graphName) ; 
        Node p = NodeFactory.createLiteral(prefix) ;
        removeAll(g, p, null) ;
    }

    @Override
    public void removeAllFromPrefixMap(String graphName) {
        Node g = NodeFactory.createURI(graphName) ; 
        removeAll(g, null, null) ;
    }

    /** Remove by pattern */
    private synchronized void removeAll(Node g, Node p, Node uri) {
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, p, uri) ;
        List<Tuple<Node>> list = Iter.toList(iter) ;    // Materialize.
        Iter.close(iter) ;
        for ( Tuple<Node> tuple : list )
            nodeTupleTable.deleteRow(tuple.get(0), tuple.get(1), tuple.get(2)) ; 
    }
    
    public NodeTupleTable getNodeTupleTable()  { return nodeTupleTable ; }
    
    /** Return a PrefixMapping for the unamed graph */
    @Override
    public PrefixMapping getPrefixMapping()
    { return getPrefixMapping(unamedGraphURI) ; }

    /** Return a PrefixMapping for a named graph */
    @Override
    public PrefixMapping getPrefixMapping(String graphName) {
        PrefixMapping pm = new GraphPrefixesProjectionTDB(graphName, this) ;
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
