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

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.graph.GraphPrefixesProjection ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTableConcrete ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;
import com.hp.hpl.jena.tdb.sys.DatasetControlMRSW ;
import com.hp.hpl.jena.tdb.sys.Names ;

public class DatasetPrefixesTDB implements DatasetPrefixStorage
{
    // Index on GPU and a nodetable.
    // The nodetable is itself an index and a data file.
    
    static final String unamedGraphURI = "" ; //Quad.defaultGraphNode.getURI() ;
    
    // Use NodeTupleTableView?
    private final NodeTupleTable nodeTupleTable ;
    static final ColumnMap colMap = new ColumnMap("GPU", "GPU") ;
    
    public static final RecordFactory factory = new RecordFactory(3*NodeId.SIZE, 0) ;

    
    @Deprecated
    public static DatasetPrefixesTDB create(Location location, DatasetControl policy) { return create(IndexBuilder.get(), location, policy) ; }
    
    @Deprecated
    public static DatasetPrefixesTDB create(IndexBuilder indexBuilder, Location location, DatasetControl policy)
    { return new DatasetPrefixesTDB(indexBuilder, location, policy) ; }

    @Deprecated
    private DatasetPrefixesTDB(IndexBuilder indexBuilder, Location location, DatasetControl policy)
    {
        // TO BE REMOVED when DI sorted out.
        // This is a table "G" "P" "U" (Graph, Prefix, URI), indexed on GPU only.
        // GPU index
        FileSet filesetGPU = null ;
        if ( location != null )
            filesetGPU = new FileSet(location, Names.indexPrefix) ;
        
        TupleIndex index = new TupleIndexRecord(3, colMap, factory, indexBuilder.newRangeIndex(filesetGPU, factory)) ;
        TupleIndex[] indexes = { index } ;
        
        // Node table.
        FileSet filesetNodeTableIdx = null ;
        if ( location != null )
            filesetNodeTableIdx = new FileSet(location, Names.prefixNode2Id) ;
        
        FileSet filesetNodeTable = null ;
        if ( location != null )
            filesetNodeTable = new FileSet(location, Names.prefixId2Node) ;
        
        NodeTable nodes = NodeTableFactory.create(indexBuilder, filesetNodeTable, filesetNodeTableIdx, -1, -1, -1) ;
        nodeTupleTable = new NodeTupleTableConcrete(3, indexes, nodes, policy) ;
    }

    //---- DI version
    
    public DatasetPrefixesTDB(TupleIndex[] indexes, NodeTable nodes, DatasetControl policy)
    {
        this.nodeTupleTable = new NodeTupleTableConcrete(3, indexes, nodes, policy) ;
    }
    
    private DatasetPrefixesTDB()
    {
        this(IndexBuilder.mem(), Location.mem(), new DatasetControlMRSW()) ;
    }
    
    /** Testing - dataset prefixes in-memory */
    public static DatasetPrefixesTDB testing() { return new DatasetPrefixesTDB() ; }
    
    // Use DatasetControl
//    public boolean isReadOnly() { return nodeTupleTable.isReadOnly() ; }
//    public void setReadOnly(boolean mode) { nodeTupleTable.setReadOnly(mode) ; }

    @Override
    public synchronized void insertPrefix(String graphName, String prefix, String uri)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
        Node u = Node.createURI(uri) ;

        nodeTupleTable.addRow(g,p,u) ;
    }

    @Override
    public Set<String> graphNames()
    {
        Iterator<Tuple<Node>> iter = nodeTupleTable.find((Node)null, null, null) ;
        Set <String> x = new HashSet<String>() ;
        for ( ; iter.hasNext() ; )
            x.add(iter.next().get(0).getURI()) ;
        Iter.close(iter) ;
        return x ;
    }
    
    @Override
    public synchronized String readPrefix(String graphName, String prefix)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
        
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
    public synchronized String readByURI(String graphName, String uriStr)
    {
        Node g = Node.createURI(graphName) ; 
        Node u = Node.createURI(uriStr) ; 
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, null, u) ;
        if ( ! iter.hasNext() )
            return null ;
        Node prefix = iter.next().get(1) ;
        Iter.close(iter) ;
        return prefix.getLiteralLexicalForm()  ;
    }

    @Override
    public synchronized Map<String, String> readPrefixMap(String graphName)
    {
        Node g = Node.createURI(graphName) ;
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, null, null) ;
        Map<String, String> map = new HashMap<String, String>() ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<Node> t = iter.next();
            String prefix = t.get(1).getLiteralLexicalForm() ;
            String uri = t.get(2).getURI() ;
            map.put(prefix, uri) ;
        }
        Iter.close(iter) ;
        return map ;
    }
    
    @Override
    public synchronized void loadPrefixMapping(String graphName, PrefixMapping pmap)
    {
        Node g = Node.createURI(graphName) ;
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
    public synchronized void removeFromPrefixMap(String graphName, String prefix, String uri)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
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
    public PrefixMapping getPrefixMapping(String graphName)
    { 
        PrefixMapping pm = new GraphPrefixesProjection(graphName, this) ;
        // Force into cache.
        // See JENA-81
        pm.getNsPrefixMap() ;
        return pm ;
    }
    
    @Override
    public void close()
    {
        nodeTupleTable.close() ;
    }

    @Override
    public void sync()  { nodeTupleTable.sync() ; }
}
