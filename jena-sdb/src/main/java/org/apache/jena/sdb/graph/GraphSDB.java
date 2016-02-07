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

package org.apache.jena.sdb.graph;

import java.util.Iterator ;

import org.apache.jena.graph.* ;
import org.apache.jena.graph.impl.AllCapabilities ;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.sdb.SDB ;
import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.sql.SDBConnection ;
import org.apache.jena.sdb.store.DatasetGraphSDB ;
import org.apache.jena.sdb.store.LibSDB ;
import org.apache.jena.sdb.store.StoreLoader ;
import org.apache.jena.sdb.store.StoreLoaderPlus ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class GraphSDB extends GraphBase implements Graph
{
    private static Logger log = LoggerFactory.getLogger(GraphSDB.class) ;

    protected Store store = null ;
    
    // ARP buffers, which results in nested updates from our prespective
    protected int inBulkUpdate = 0 ;
    
    protected Node graphNode = Quad.defaultGraphNodeGenerated ;
    protected DatasetGraphSDB datasetStore = null ;
    
    public GraphSDB(Store store, String uri) {
        this(store, NodeFactory.createURI(uri));
    }

    public GraphSDB(Store store) {
        this(store, (Node)null);
    }

    public GraphSDB(Store store, Node graphNode) {
        if ( graphNode == null )
            graphNode = Quad.defaultGraphNodeGenerated;

        this.store = store;
        this.graphNode = graphNode;

        // Avoid looping here : DatasetStoreGraph can make GraphSDB's
        datasetStore = new DatasetGraphSDB(store, this, SDB.getContext().copy());

        // readPrefixMapping() ;
    }
    
    /* We don't support value tests, hence handlesLiteralTyping is false */
    @Override
    public Capabilities getCapabilities() { 
    	if (capabilities == null)
            capabilities = new AllCapabilities()
        	  { @Override public boolean handlesLiteralTyping() { return false; } };
        return capabilities;
    }
    
    public Store getStore() { return store ; } 

    public SDBConnection getConnection() { return store.getConnection() ; }
    
    @Override
    public PrefixMapping createPrefixMapping() {
        try {
            String graphURI = null;
            if ( Quad.isDefaultGraphGenerated(graphNode) )
                graphURI = "";
            else if ( graphNode.isURI() )
                graphURI = graphNode.getURI();
            else {
                log.warn("Not a URI for graph name");
                graphURI = graphNode.toString();
            }

            return new PrefixMappingSDB(graphURI, store.getConnection());
        }
        catch (Exception ex) {
            log.warn("Failed to get prefixes: " + ex.getMessage());
            return null;
        }
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
        Iterator<Triple> iter = LibSDB.findTriples(datasetStore, graphNode, m.getSubject(), m.getPredicate(), m.getObject());
        return WrappedIterator.create(iter);
    }

    public StoreLoader getBulkLoader() { return store.getLoader() ; }
    
    @Override
    public GraphEventManager getEventManager() {
        if ( gem == null )
            gem = new EventManagerSDB();
        return gem;
    }

    @Override
    public void performAdd(Triple triple) {
        if ( inBulkUpdate == 0 )
            store.getLoader().startBulkUpdate();

        if ( Quad.isDefaultGraphGenerated(graphNode) )
            store.getLoader().addTriple(triple);
        else {
            // XXX
            StoreLoaderPlus x = (StoreLoaderPlus)store.getLoader();
            x.addQuad(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject());
        }
        if ( inBulkUpdate == 0 )
            store.getLoader().finishBulkUpdate();
    }
    
    @Override
    public void performDelete(Triple triple) {
        if ( inBulkUpdate == 0 )
            store.getLoader().startBulkUpdate();
        if ( Quad.isDefaultGraphGenerated(graphNode) )
            store.getLoader().deleteTriple(triple);
        else {
            // XXX
            StoreLoaderPlus x = (StoreLoaderPlus)store.getLoader();
            x.deleteQuad(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject());
        }
        if ( inBulkUpdate == 0 )
            store.getLoader().finishBulkUpdate();
    }
    
    public void startBulkUpdate()  { inBulkUpdate += 1 ; if (inBulkUpdate == 1) store.getLoader().startBulkUpdate();}
    public void finishBulkUpdate() { inBulkUpdate -= 1 ; if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();}
    
    @Override
    public TransactionHandler getTransactionHandler() { return store.getConnection().getTransactionHandler() ; }
    
    @Override
    public int graphBaseSize() { return (int) (Quad.isDefaultGraphGenerated(graphNode) ? store.getSize() : store.getSize(graphNode)); }
    
	public void deleteAll() {
		if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
		if ( Quad.isDefaultGraphGenerated(graphNode) )
			store.getLoader().deleteAll();
		else
			((StoreLoaderPlus) store.getLoader()).deleteAll(graphNode);
		if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
	}
}
