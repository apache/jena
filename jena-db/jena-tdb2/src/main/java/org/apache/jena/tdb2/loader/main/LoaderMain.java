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

package org.apache.jena.tdb2.loader.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.graph.Node;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.loader.DataLoader;
import org.apache.jena.tdb2.loader.base.*;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.DatasetPrefixesTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.sys.TDBInternal;

/**
 * The phased {@link DataLoader}, which runs loading in a number of phases. 
 * Options are controlled by a {@link LoaderPlan}. 
 * <p>
 * The first phase is data to primary indexes, and maybe some additional indexes. 
 * Then phases of replaying an index (triples or quads) to drive creating more secondary
 * indexes.
 * </p>
 * <p>
 * {@link LoaderPlans#loaderPlanParallel} is the parallel loader - do everything in
 * the first phase, one thread for pasring, for node table insertion and one for each index built.
 * </p>
 * <p>  
 * {@link LoaderPlans#loaderPlanPhased} is the phased loader. 
 * </p>
 * <p>
 * The process is: 
 * <blockquote> 
 * Data phase: {@code parser -> to NodeIds/Tuples -> TupleIndex+}
 * <br/>
 * Additional index phases: {@code primary index -> Indexer*}
 * </blockquote>
 * <p><b>Data Phase</b></p>
 * <p>
 * {@link DataBatcher} produces {@link DataBlock DataBlocks} - grouping of triples and
 * quads and sends them to a handler {@code Consumer<DataBlock>}. This is wired up to be
 * the feed for {@link DataToTuples}.
 * </p><p>
 * {@link DataToTuples} processes {@link DataBlock DataBlocks} to create 2 outputs blocks
 * of {@code Tuple<NodeId>}, one output for triples, one for quads, and sends these to
 * {@link Indexer}s for triples and quads.
 * </p><p>
 * {@link Indexer} processes blocks of {@code Tuple<NodeId>} (of the same tuple length)
 * and writes them to a number of indexes. Each index being written is a separate thread.
 * </p><p>
 * The normal execution of {@link InputStage#MULTI} is provided by {@code executeData}
 * and is {@code DataBatcher -> DataToTuples -> Indexer+} on separate threads.
 * </p><p>
 * One alternative is {@link InputStage#PARSE_NODE}, provided by {@code executeDataParseId},
 * which uses on thread for both parsing and node table
 * {@code DataToTuplesInline -> Indexer+}.
 * </p><p>
 * The third alternative {@link InputStage#PARSE_NODE}, provided by {@code executeDataOneThread},
 * which do doesa all input stage operations on the calling parser thread.
 * </p>
 * <p><b>Index Phase</b></p>
 * <p>
 * Additional indexes are built in a number of later phases. Each phase copies the primary index for triples
 * to other indexes in controllable groups.  This happens for triples and for quads. See {@code executeSecondary}.
 * </p>
 * @see LoaderPlans
 */
public class LoaderMain extends LoaderBase implements DataLoader {
    public static final int DataTickPoint   = 500_000;
    public static final int DataSuperTick   = 10;
    public static final int IndexTickPoint  = 1_000_000;
    public static final int IndexSuperTick  = 10;
    
    private final LoaderPlan loaderPlan;
    
    private final DatasetGraphTDB dsgtdb;
    private final StreamRDF stream;
    private final Map<String, TupleIndex> indexMap;

    private final StreamRDFCounting dataInput;
    private final List<BulkStartFinish> dataProcess = new ArrayList<>();
    
    public LoaderMain(LoaderPlan loaderPlan, DatasetGraph dsg, MonitorOutput output) {
        this(loaderPlan, dsg, null, output);
    }
    
    public LoaderMain(LoaderPlan loaderPlan, DatasetGraph dsg, Node graphName, MonitorOutput output) {
        super(dsg, graphName, output);
        this.loaderPlan = loaderPlan;
        dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        indexMap = PhasedOps.indexMap(dsgtdb);
        // Phase 1.
        switch ( loaderPlan.dataInputType() ) {
            case MULTI :
                dataInput = executeData(loaderPlan, dsgtdb, indexMap, dataProcess, output);
                break;
            case PARSE_NODE :
                dataInput = executeDataParseId(loaderPlan, dsgtdb, indexMap, dataProcess, output);
                break;
            case PARSE_NODE_INDEX :
                dataInput = executeDataOneThread(loaderPlan, dsgtdb, indexMap, dataProcess, output);
                break;
            default :
                throw new IllegalStateException();
        }
        stream = LoaderOps.toNamedGraph(dataInput, graphName);
    }

    /**
     * Create data ingestion and primary index building of a {@link LoaderPlan}.
     * Separate threads for parsing, node table loading and primary index building.  
     */
    private static StreamRDFCounting executeData(LoaderPlan loaderPlan, DatasetGraphTDB dsgtdb, Map<String, TupleIndex> indexMap, List<BulkStartFinish> dataProcess, MonitorOutput output) {
        DatasetPrefixesTDB dps = (DatasetPrefixesTDB)dsgtdb.getPrefixes();
        PrefixHandler prefixHandler = new PrefixHandler(dps, output);
        dataProcess.add(prefixHandler);
    
        // Must be one index at least of each triples and quads.
            
        TupleIndex[] idx3 = PhasedOps.indexSetFromNames(loaderPlan.primaryLoad3(), indexMap);
        Indexer indexer3 = new Indexer(output, idx3);
        TupleIndex[] idx4 = PhasedOps.indexSetFromNames(loaderPlan.primaryLoad4(), indexMap);
        Indexer indexer4 = new Indexer(output, idx4);
    
        dataProcess.add(indexer4);
        dataProcess.add(indexer3);
        
        Destination<Tuple<NodeId>> functionIndexer3 = indexer3.index();
        Destination<Tuple<NodeId>> functionIndexer4 = indexer4.index();
        
        DataToTuples dtt = new DataToTuples(dsgtdb, functionIndexer3, functionIndexer4, output);
        Consumer<DataBlock> dest = dtt.data();
        DataBatcher dataBatcher = new DataBatcher(dest, prefixHandler.handler(), output);
        StreamRDF baseInput = dataBatcher;
        
        dataProcess.add(dtt);
        dataProcess.add(dataBatcher);
        return dataBatcher;
    }

    /**
     * Create data ingestion and primary index building of a {@link LoaderPlan}.
     * One thread for parsing and node table building and one for each primary index building.  
     * This version uses a thread for parse/NodeTable/Tuple and a thread for each of triple and quad index for phase one.  
     */
    private static StreamRDFCounting executeDataParseId(LoaderPlan loaderPlan, DatasetGraphTDB dsgtdb, Map<String, TupleIndex> indexMap, List<BulkStartFinish> dataProcess, MonitorOutput output) {
        // One thread for parse/NodeTable.
        // Two steps of phase one on the invoking thread.
        // Chunk and dispatch to indexers for the tuple loading.
        
        TupleIndex[] idx3 = PhasedOps.indexSetFromNames(loaderPlan.primaryLoad3(), indexMap);
        Indexer indexer3 = new Indexer(output, idx3);
        TupleIndex[] idx4 = PhasedOps.indexSetFromNames(loaderPlan.primaryLoad4(), indexMap);
        Indexer indexer4 = new Indexer(output, idx4);
        
        DataToTuplesInline dttInline = new DataToTuplesInline(dsgtdb, indexer3.index(), indexer4.index(), output);
        dataProcess.add(indexer3);
        dataProcess.add(indexer4);
        dataProcess.add(dttInline);
        return dttInline;
    }

    /**
     * Create data ingestion and primary index building of a {@link LoaderPlan}.
     * This version uses a thread for parse/NodeTable/Tuple/Index.  
     */
    private static StreamRDFCounting executeDataOneThread(LoaderPlan loaderPlan, DatasetGraphTDB dsgtdb, Map<String, TupleIndex> indexMap, List<BulkStartFinish> dataProcess, MonitorOutput output) {
        // One thread input stage.
        // All three phase one steps on the invoking thread.

        TupleIndex[] idx3 = PhasedOps.indexSetFromNames(loaderPlan.primaryLoad3(), indexMap);
        IndexerInline indexer3 = new IndexerInline(output, idx3);
        Consumer<Tuple<NodeId>> dest3 = tuple->indexer3.load(tuple);
        
        TupleIndex[] idx4 = PhasedOps.indexSetFromNames(loaderPlan.primaryLoad4(), indexMap);
        IndexerInline indexer4 = new IndexerInline(output, idx4);
        Consumer<Tuple<NodeId>> dest4 = tuple->indexer4.load(tuple);
        
        DataToTuplesInlineSingle dataToTuples = new DataToTuplesInlineSingle(dsgtdb, dest3, dest4, output);
        dataProcess.add(indexer3);
        dataProcess.add(indexer4);
        dataProcess.add(dataToTuples);
        return dataToTuples;
    }
    
    @Override
    public StreamRDF stream() {
        return stream;
    }
    
    @Override
    public boolean bulkUseTransaction() {
        // Manipulate the transactions directly by component. 
        return false;
    }

    @Override
    public void startBulk() {
        // Lock everyone else out while we multithread.
        dsgtdb.getTxnSystem().getTxnMgr().startExclusiveMode();
        super.startBulk();
        // Set the data pipeline
        BulkProcesses.start(dataProcess);
    }

    @Override
    public void finishBulk() {
        // Close off the data pipeline
        BulkProcesses.finish(dataProcess);
        
        boolean doTriples = countTriples() != 0;
        boolean doQuads = countQuads() != 0 ;
        
        if ( doTriples ) {
            TupleIndex srcIdx3 = PhasedOps.findInIndexMap(loaderPlan.primaryLoad3()[0], indexMap);
            TupleIndex[][] indexSets3 = PhasedOps.indexSetsFromNames(loaderPlan.secondaryIndex3(), indexMap);
            executeSecondary(srcIdx3, indexSets3, dsgtdb, output);
        }
        
        if ( doQuads ) {
            TupleIndex srcIdx4 = PhasedOps.findInIndexMap(loaderPlan.primaryLoad4()[0], indexMap);
            TupleIndex[][] indexSets4 = PhasedOps.indexSetsFromNames(loaderPlan.secondaryIndex4(), indexMap);
            executeSecondary(srcIdx4, indexSets4, dsgtdb, output);
        }
        super.finishBulk();
        dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }
    
    /** Execute secondary index building of a {@link LoaderPlan} */
    private static void executeSecondary(TupleIndex srcIdx, TupleIndex[][] indexSets, DatasetGraphTDB dsgtdb, MonitorOutput output) {
        if ( indexSets.length == 0 )
            return;
        List<BulkStartFinish> processes = new ArrayList<>();
        output.print("Start replay index %s", srcIdx.getName());
        // For each phase.
        for ( TupleIndex[] indexes : indexSets ) {
            if ( indexes.length == 0 )
                // Nothing in this phase. 
                continue;
            indexPhase(processes, srcIdx, indexes, output);
            // processes - wait now or wait later?
        }
        // Now make sure they are flushed.
        BulkProcesses.finish(processes);
    }

    private static void indexPhase(List<BulkStartFinish> processes, TupleIndex srcIdx, TupleIndex[] indexes, MonitorOutput output) {
        String indexSetLabel = PhasedOps.indexMappings(indexes);
        output.print("Index set:  %s => %s", srcIdx.getName(), indexSetLabel);
        Indexer indexer = new Indexer(output, indexes);
        Destination<Tuple<NodeId>> dest = indexer.index();
        indexer.startBulk();
        TransactionCoordinator coordinator = CoLib.newCoordinator();
        CoLib.add(coordinator, srcIdx);
        CoLib.start(coordinator);
        // READ transaction.
        Transaction transaction = coordinator.begin(TxnType.READ);
        // Add to processes - we can wait later if we do not touched indexes being built.
        processes.add(indexer);
        PhasedOps.ReplayResult result = PhasedOps.replay(srcIdx, dest, output);
        // End read tranaction on srcIdx
        transaction.end();
        
        String timeStr = "---";
        if ( result.elapsed != 0 ) {
            double time = result.elapsed / 1000.0;
            //long AvgRate = (result.items * 1000L) / result.elapsed;
            timeStr = String.format("%,.1f", time);
        }
        output.print("Index set:  %s => %s [%,d items, %s seconds]", srcIdx.getName(), indexSetLabel, result.items, timeStr);
    }

    
//    private static Map<String, TupleIndex> indexMap(DatasetGraphTDB dsgtdb) {
//        Map<String, TupleIndex> indexMap = new HashMap<>();
//        // All triple/quad indexes.
//        Arrays.stream(dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes())
//              .forEach(idx->indexMap.put(idx.getName(), idx));
//        Arrays.stream(dsgtdb.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes())
//              .forEach(idx->indexMap.put(idx.getName(), idx));
//        return indexMap;
//    }
    
    @Override
    public void finishException(Exception ex) {
        try { 
            dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
        } catch (Exception ex2) {
            ex.addSuppressed(ex2);
        }
    }
    
    @Override
    public long countTriples() {
        return dataInput.countTriples();
    }

    @Override
    public long countQuads() {
        return dataInput.countQuads();
    }

    @Override
    protected void loadOne(String filename) {
        LoaderOps.inputFile(stream, filename, output, DataTickPoint, DataSuperTick);
    }
}
