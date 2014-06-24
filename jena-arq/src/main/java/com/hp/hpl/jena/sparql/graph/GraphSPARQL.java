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

package com.hp.hpl.jena.sparql.graph;

import java.util.ArrayList ;
import java.util.List ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.graph.impl.AllCapabilities ;
import com.hp.hpl.jena.graph.impl.GraphBase ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/** This class provides the Jena Graph interface to a graph in a Dataset.
 *  It enables the full Jena API to be used with data only accessible
 *  via a Dataset. */

public class GraphSPARQL extends GraphBase implements Graph
{
    private static Logger log = LoggerFactory.getLogger(GraphSPARQL.class) ;

    private DatasetGraph dataset ;
    private QueryEngineFactory factory ;
    
    // Use the dataset graph uninterpretted.
    public GraphSPARQL(DatasetGraph dataset, QueryEngineFactory factory)
    {  
        this.dataset = dataset ;
        this.factory = factory ;
    }
    
    public GraphSPARQL(DatasetGraph dataset, String uri)
    {
        this(dataset, NodeFactory.createURI(uri)) ;
    }

    public GraphSPARQL(DatasetGraph dsg, Node gn)
    { 
        this.dataset = DatasetGraphFactory.create(dsg) ;
        dataset.setDefaultGraph(dsg.getGraph(gn)) ; 
        factory = null ;
    }

    
    public GraphSPARQL(Graph graph)
    { this(graph, null) ; }
    
    public GraphSPARQL(Graph graph, QueryEngineFactory factory)
    {
        this.dataset = DatasetGraphFactory.createOneGraph(graph) ;
        this.factory = factory ;
    }
    
    private DatasetGraph toDSG(DatasetGraph dataset, Node graphNode)
    {
        Graph graph = ( graphNode == null ) ? dataset.getDefaultGraph() : dataset.getGraph(graphNode) ;
        DatasetGraph dsGraph = DatasetGraphFactory.create(graph) ;
        return dsGraph ;
    }

    
    /* We don't support value tests, hence handlesLiteralTyping is false */
    @Override
    public Capabilities getCapabilities()
    { 
    	if (capabilities == null)
            capabilities = new AllCapabilities()
        	  { @Override
                public boolean handlesLiteralTyping() { return false; } 
        	  };
        return capabilities;
    }
    
    public DatasetGraph getDataset() { return dataset ; } 

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        Node s = m.getMatchSubject() ;
        Var sVar = null ;
        if ( s == null )
        {
            sVar = Var.alloc("s") ;
            s = sVar ;
        }
        
        Node p = m.getMatchPredicate() ;
        Var pVar = null ;
        if ( p == null )
        {
            pVar = Var.alloc("p") ;
            p = pVar ;
        }
        
        Node o = m.getMatchObject() ;
        Var oVar = null ;
        if ( o == null )
        {
            oVar = Var.alloc("o") ;
            o = oVar ;
        }
        
        Triple triple = new Triple(s, p ,o) ;
        
        // Evaluate as an algebra expression
        BasicPattern pattern = new BasicPattern() ;
        pattern.add(triple) ;
        Op op = new OpBGP(pattern) ;
        Plan plan = factory.create(op, getDataset(), BindingRoot.create(), null) ;
        
        QueryIterator qIter = plan.iterator() ;
        //List<Triple> triples = new ArrayList<Triple>() ;
        List<Triple> triples = new ArrayList<>() ;
        
        for (; qIter.hasNext() ; )
        {
            Binding b = qIter.nextBinding() ;
            Node sResult = s ;
            Node pResult = p ;
            Node oResult = o ;
            if ( sVar != null )
                sResult = b.get(sVar) ;
            if ( pVar != null )
                pResult = b.get(pVar) ;
            if ( oVar != null )
                oResult = b.get(oVar) ;
            Triple resultTriple = new Triple(sResult, pResult, oResult) ;
            if ( log.isDebugEnabled() )
                log.debug("  "+resultTriple) ;
            triples.add(resultTriple) ;
        }
        qIter.close() ;
        return WrappedIterator.createNoRemove(triples.iterator()) ;
    }

    
//    class GraphIterator extends TrackingTripleIterator
//    {
//        GraphIterator(Iterator iter) { super(iter) ; }
//        
//        @Override
//        public void remove()
//        { delete((Triple)current) ; }
//    }
//    
//    public StoreLoader getBulkLoader() { return store.getLoader() ; }
//    
//    @Override
//    public BulkUpdateHandler getBulkUpdateHandler()
//    {
//    	if (bulkHandler == null) bulkHandler = new UpdateHandlerSDB(this);
//    	return bulkHandler;
//    }
//    
//    @Override
//    public GraphEventManager getEventManager()
//    {
//    	if (gem == null) gem = new EventManagerSDB( this );
//        return gem;
//    }
//    
//    @Override
//    public void performAdd( Triple triple )
//    {
//    	if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
//        store.getLoader().addTriple(triple) ;
//        if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
//    }
//    
//    @Override
//    public void performDelete( Triple triple ) 
//    {
//    	if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
//        store.getLoader().deleteTriple(triple) ;
//        if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
//    }
}
