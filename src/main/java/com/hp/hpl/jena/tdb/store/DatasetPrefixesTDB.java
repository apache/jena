/*
 * (c) Copyright 2008, 2009 Hewlett-P;ackard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Iterator ;
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
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicy ;
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicyMRSW ;
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
    public static DatasetPrefixesTDB create(Location location, ConcurrencyPolicy policy) { return create(IndexBuilder.get(), location, policy) ; }
    
    @Deprecated
    public static DatasetPrefixesTDB create(IndexBuilder indexBuilder, Location location, ConcurrencyPolicy policy)
    { return new DatasetPrefixesTDB(indexBuilder, location, policy) ; }

    @Deprecated
    private DatasetPrefixesTDB(IndexBuilder indexBuilder, Location location, ConcurrencyPolicy policy)
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
        
        NodeTable nodes = NodeTableFactory.create(indexBuilder, filesetNodeTable, filesetNodeTableIdx, -1, -1) ;
        nodeTupleTable = new NodeTupleTableConcrete(3, indexes, nodes, policy) ;
    }

    //---- DI version
    
    public DatasetPrefixesTDB(TupleIndex[] indexes, NodeTable nodes, ConcurrencyPolicy policy)
    {
        this.nodeTupleTable = new NodeTupleTableConcrete(3, indexes, nodes, policy) ;
    }
    
    private DatasetPrefixesTDB()
    {
        this(IndexBuilder.mem(), Location.mem(), new ConcurrencyPolicyMRSW()) ;
    }
    
    /** Testing - dataset prefixes in-memory */
    public static DatasetPrefixesTDB testing() { return new DatasetPrefixesTDB() ; }

    //@Override
    public synchronized void insertPrefix(String graphName, String prefix, String uri)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
        Node u = Node.createURI(uri) ;

        nodeTupleTable.addRow(g,p,u) ;
    }

    //@Override
    public Set<String> graphNames()
    {
        Iterator<Tuple<Node>> iter = nodeTupleTable.find((Node)null, null, null) ;
        Set <String> x = new HashSet<String>() ;
        for ( ; iter.hasNext() ; )
            x.add(iter.next().get(0).getURI()) ;
        Iter.close(iter) ;
        return x ;
    }
    
    //@Override
    public synchronized String readPrefix(String graphName, String prefix)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
        
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, p, null) ;
        if ( ! iter.hasNext() )
            return null ;
        Tuple<Node> t = iter.next() ;
        Node uri = t.get(2) ;
        Iter.close(iter) ;
        return uri.getURI() ; 
    }

    //@Override
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

    //@Override
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
    
    //@Override
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
    
    //@Override
    public synchronized void removeFromPrefixMap(String graphName, String prefix, String uri)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, p, null) ;
        while ( iter.hasNext() )
            nodeTupleTable.deleteRow(g, p, iter.next().get(2)) ;
        Iter.close(iter) ;
    }

    public NodeTupleTable getNodeTupleTable()  { return nodeTupleTable ; }
    
    /** Return a PrefixMapping for the unamed graph */
    //@Override
    public PrefixMapping getPrefixMapping()
    { return getPrefixMapping(unamedGraphURI) ; }

    /** Return a PrefixMapping for a named graph */
    //@Override
    public PrefixMapping getPrefixMapping(String graphName)
    { return new GraphPrefixesProjection(graphName, this) ; }

    //@Override
    public void close()
    {
        nodeTupleTable.close() ;
    }

    //@Override
    public void sync() { sync(true) ; }

    //@Override
    public void sync(boolean force)
    { 
        nodeTupleTable.sync(force) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */