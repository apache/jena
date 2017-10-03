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

package org.apache.jena.tdb2.loader;

import java.util.* ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.other.BatchedStreamRDF ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.setup.TDBDatasetDetails;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.tupletable.TupleTable;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Batch by size.
 * @see BatchedStreamRDF BatchedStreamRDF, which batches by subject
 */
public class StreamRDFBatchSplit implements StreamRDF {
    private static Logger log = LoggerFactory.getLogger(StreamRDFBatchSplit.class) ;
    protected static NodeId placeholder = NodeIdFactory.genUnique();
    protected final List<Triple> triples ;
    protected final List<Tuple<NodeId>> tuples ;
    protected final Map<Node, NodeId> mapping ;
    
    private final int batchSize ;
    private final TDBDatasetDetails details ;
    private final DatasetGraphTDB dsg ;
    private Transaction txn = null ;
    
    public StreamRDFBatchSplit(DatasetGraphTDB dsg, int batchSize) {
        this.dsg = dsg ;
        this.batchSize = batchSize ;
        this.triples = new ArrayList<>(batchSize) ;
        this.tuples = new ArrayList<>(triples.size()) ;
        this.mapping = new HashMap<>(2*batchSize) ;
        this.details = new TDBDatasetDetails(dsg) ;
    }
        
    @Override
    public void start() {
        log.info("Batch size: "+batchSize);
        // Multiple starts in one transaction are possible.
        if ( txn == null )
            txn = dsg.getTxnSystem().getThreadTransaction() ;
        if ( txn == null )
            throw new TDBException("Not in a transaction") ;
    }

    @Override
    public void triple(Triple triple) {
        //Find nodes.
        //log.info("Triple: "+triple) ;
        processNode(triple.getSubject()) ;
        processNode(triple.getPredicate()) ;
        processNode(triple.getObject()) ;
        triples.add(triple) ;
        if ( triples.size() >= batchSize )
            processBatch() ;
    }

    int batchNumber = 0 ;
    
    protected void processBatch() {
        //if ( batchNumber < 10 )
        batchNumber++ ;
        //FmtLog.info(log, ">>processBatch: [%d]->%d", batchNumber, triples.size()) ;

        Set<Node> requiredNodes = mapping.keySet() ;

        boolean executeBatchNodesPhase = true ;
        boolean executeIndexPhase = true ;
        // Derived control.
        boolean batchUpdateIndexes = true ;
        
        if ( executeBatchNodesPhase )
            // Check this is a cache node table.
            batchUpdateNodes(requiredNodes, details) ;
        
        if ( executeIndexPhase ) {
            if ( batchUpdateIndexes )
                batchUpdateIndexes(dsg, details, triples, /*tuples*/null) ;
            else
                incrementalUpdateIndexes(triples, dsg) ;
        }
        triples.clear();
        tuples.clear() ;
        //FmtLog.info(log, "<<processBatch") ;
        mapping.clear();
//        if ( batchSize < 10 )
//            System.exit(0) ;
    }
   
    private static void incrementalUpdateIndexes(List<Triple> triples, DatasetGraphTDB dsg) {
        for ( Triple triple : triples ) {
            dsg.getTripleTable().add(triple); 
        }
    }

    /** This files the cache so that the tuples adds are faster */ 
    private static void batchUpdateNodes(Set<Node> required, TDBDatasetDetails details) {
        List<Node> nodes = new ArrayList<>() ;
        // Resolve NodeIds
        
        // ** Move this into cache - code. 
        
        for ( Node n : required ) {
            // 
            if ( details.ntCache.getNodeIdForNodeCache(n) == null /* set input - no need :: && ! nodes.contains(n) /* Not good?*/ )
                nodes.add(n) ;
        }
        //log.info("Batch nodes: "+nodes.size()) ;
        // This drops into the default method.
        details.ntTop.bulkNodeToNodeId(nodes, true) ;
        
        // Check
        // Resolve NodeIds
        for ( Node n : required ) {
            if ( details.ntCache.getNodeIdForNodeCache(n) == null  )
                log.info("Not in cache: "+n) ;
        }
        //details.ntCluster.bulkNodeToNodeId(nodes, true) ;
        
        
        
    }

    private static void batchUpdateIndexes(DatasetGraphTDB dsg, TDBDatasetDetails details, List<Triple> batchTriples, List<Tuple<NodeId>> workspace) {
        List<Tuple<NodeId>> tuples = workspace ;
        if ( tuples == null )
            tuples = new ArrayList<>(batchTriples.size()) ;

        convert(batchTriples, tuples, details.ntTop) ;
        //log.info("Batch triples: "+tuples.size()) ;

        TupleTable tupleTable = dsg.getTripleTable().getNodeTupleTable().getTupleTable() ;
        tupleTable.addAll(tuples);
    }

    // check for duplicate code
    private static List<Tuple<NodeId>> convert(List<Triple> triples, NodeTable nodeTable) {
        return triples.stream().map(t -> TupleFactory.tuple(nodeTable.getAllocateNodeId(t.getSubject()),
                                                            nodeTable.getAllocateNodeId(t.getPredicate()),
                                                            nodeTable.getAllocateNodeId(t.getObject())))
            .collect(Collectors.toList());
    }
    
    private static void convert(List<Triple> triples, List<Tuple<NodeId>> tuples, NodeTable nodeTable) {
        // Slightly faster.  But larger batches?
        for ( Triple t : triples ) {
            NodeId nid_s = nodeTable.getAllocateNodeId(t.getSubject()) ;
            NodeId nid_p = nodeTable.getAllocateNodeId(t.getPredicate()) ;
            NodeId nid_o = nodeTable.getAllocateNodeId(t.getObject()) ;
            Tuple<NodeId> x = TupleFactory.tuple(nid_s, nid_p, nid_o) ;
            tuples.add(x) ;
        }
        
//        triples.stream().map(t->
//                  TupleFactory.tuple
//                  (nodeTable.getAllocateNodeId(t.getSubject()),
//                   nodeTable.getAllocateNodeId(t.getPredicate()),
//                   nodeTable.getAllocateNodeId(t.getObject())))
//                .collect(Collectors.toCollection(()->tuples)) ;
    }
    
    
    private void processNode(Node node) {
        
        if ( mapping.containsKey(node)) 
            return ;
        
        if ( NodeId.hasInlineDatatype(node) ) {
            NodeId nodeId = NodeId.inline(node) ;
            if ( nodeId != null )
                return ;
        }
        mapping.put(node, placeholder) ;
    }
    
    @Override
    public void quad(Quad quad) {}

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {}

    @Override
    public void finish() { 
        if ( ! triples.isEmpty() )
            processBatch() ;
    }

}

