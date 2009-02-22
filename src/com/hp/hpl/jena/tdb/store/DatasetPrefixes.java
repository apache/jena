/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lib.ColumnMap;
import lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable;
import com.hp.hpl.jena.tdb.sys.Names;

public class DatasetPrefixes implements Closeable, Sync
{
    static final String unamedGraphURI = "" ; //Quad.defaultGraphNode.getURI() ;
    private final NodeTupleTable nodeTupleTable ;
    static final ColumnMap colMap = new ColumnMap("GPU", "GPU") ;
    
    static final RecordFactory factory = new RecordFactory(3*NodeId.SIZE, 0) ;
    public DatasetPrefixes(Location location)
    {
        this(IndexBuilder.get(), location) ;
    }
    
    public DatasetPrefixes(IndexBuilder indexBuilder, Location location)
    {
        // This is a table "G" "P" "U" (Graph, Prefix, URI), indexed on GPU only.
        
        TupleIndex index = new TupleIndexRecord(3, colMap, factory, indexBuilder.newRangeIndex(location, factory, Names.indexPrefix)) ;
        TupleIndex[] indexes = { index } ;
        // No node cache.  Prefixes are cached.
        NodeTable nodes = NodeTableFactory.create(indexBuilder, location, Names.prefixesData, Names.indexPrefix2Id, -1, -1) ;
        nodeTupleTable = new NodeTupleTable(3, indexes, nodes) ;
    }
    
    private DatasetPrefixes()
    {
        // This is a table "G" "P" "U" (Graph, Prefix, URI), indexed on GPU only.
        
        TupleIndex index = new TupleIndexRecord(3, colMap, factory, IndexBuilder.mem().newRangeIndex(null, factory, Names.indexPrefix)) ;
        TupleIndex[] indexes = { index } ;
        NodeTable nodes = NodeTableFactory.create(IndexBuilder.mem(), null, Names.prefixesData, Names.indexPrefix2Id, -1, -1) ;
        nodeTupleTable = new NodeTupleTable(3, indexes, nodes) ;
    }
    
    /** Testing - dataset prefixes in-memory */
    public static DatasetPrefixes testing() { return new DatasetPrefixes() ; }

    public synchronized void insertPrefix(String graphName, String prefix, String uri)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
        Node u = Node.createURI(uri) ;

        nodeTupleTable.addRow(g,p,u) ;
    }

    public Set<String> graphNames()
    {
        Iterator<Tuple<Node>> iter = nodeTupleTable.find((Node)null, null, null) ;
        Set <String> x = new HashSet<String>() ;
        for ( ; iter.hasNext() ; )
            x.add(iter.next().get(0).getURI()) ;
        return x ;
    }
    
    public synchronized String readPrefix(String graphName, String prefix)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, p, null) ;
        if ( ! iter.hasNext() )
            return null ;
        Node uri = iter.next().get(2) ;
        return uri.getURI() ; 
    }

    public synchronized String readByURI(String graphName, String uriStr)
    {
        Node g = Node.createURI(graphName) ; 
        Node u = Node.createURI(uriStr) ; 
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, null, u) ;
        if ( ! iter.hasNext() )
            return null ;
        Node prefix = iter.next().get(1) ;
        return prefix.getLiteralLexicalForm()  ;
    }

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
        return map ;
    }
    
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
    }
    
    public synchronized void removeFromPrefixMap(String graphName, String prefix, String uri)
    {
        Node g = Node.createURI(graphName) ; 
        Node p = Node.createLiteral(prefix) ; 
        Iterator<Tuple<Node>> iter = nodeTupleTable.find(g, p, null) ;
        while ( iter.hasNext() )
            nodeTupleTable.deleteRow(g, p, iter.next().get(2)) ;
    }

    @Override
    public void close()
    {
        nodeTupleTable.close() ;
    }

    @Override
    public void sync(boolean force)
    { 
        nodeTupleTable.sync(force) ;
    }

    /** Return a PrefixMapping for the unamed graph */ 
    public PrefixMapping getPrefixMapping()
    { return getPrefixMapping(unamedGraphURI) ; }
    
    /** Return a PrefixMapping for a named graph */ 
    public PrefixMapping getPrefixMapping(String graphName)
    { return new Projection(graphName) ; }
    
    // A view of the table.
    // Manages the PrefixMappingImpl cache as well.
    class Projection extends PrefixMappingImpl
    {
        // Own cache and complete replace  PrefixMappingImpl?
        
        private String graphName ; 
        Projection(String graphName) { this.graphName = graphName ; }

//        @Override
//        protected void regenerateReverseMapping() {}

        @Override
        public String getNsURIPrefix( String uri )
        {
            String x = super.getNsURIPrefix(uri) ;
            if ( x !=  null )
                return x ;
            // Do a reverse read.
            x = readByURI(graphName, uri) ;
            if ( x != null )
                super.set(x, uri) ;
            return x ;
        }
        
        
        @Override 
        public Map<String, String> getNsPrefixMap()
        {
            Map<String, String> m =  readPrefixMap(graphName) ;
            // Force into the cache
            for ( Entry<String, String> e : m.entrySet() ) 
                super.set(e.getKey(), e.getValue()) ;
            return m ;
        }

        
        @Override
        protected void set(String prefix, String uri)
        {
            super.set(prefix, uri) ;
            insertPrefix(graphName, prefix, uri) ;
        }

        @Override
        protected String get(String prefix)
        {
            String x = super.get(prefix) ;
            if ( x !=  null )
                return x ;
            // In case it has been updated.
            x = readPrefix(graphName, prefix) ;
            super.set(prefix, x) ;
            return x ;
        }

        @Override
        public PrefixMapping removeNsPrefix(String prefix)
        {
            String uri = super.getNsPrefixURI(prefix) ;
            if ( uri != null )
                removeFromPrefixMap(graphName, prefix, uri) ;
            super.removeNsPrefix(prefix) ;
            return this ; 
        }
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