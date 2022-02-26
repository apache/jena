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

package org.apache.jena.tdb2.xloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.block.BlockMgrFactory;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.base.file.FileFactory;
import org.apache.jena.dboe.base.file.FileSet;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeParams;
import org.apache.jena.dboe.trans.bplustree.rewriter.BPlusTreeRewriter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.progress.ProgressIterator;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.slf4j.Logger;

/**
 * From a file of records, build a (packed) index by sorting the input records and
 * the writing the B+Tree bottom up.
 */
public class ProcBuildIndexX
{
    // Sort and build.

    // K1="-k 1,1"
    // K2="-k 2,2"
    // K3="-k 3,3"
    // K4="-k 4,4"
    //
    // generate_index "$K1 $K2 $K3" "$DATA_TRIPLES" SPO
    // generate_index "$K2 $K3 $K1" "$DATA_TRIPLES" POS
    // generate_index "$K3 $K1 $K2" "$DATA_TRIPLES" OSP
    // generate_index "$K1 $K2 $K3 $K4" "$DATA_QUADS" GSPO
    // generate_index "$K1 $K3 $K4 $K2" "$DATA_QUADS" GPOS
    // generate_index "$K1 $K4 $K2 $K3" "$DATA_QUADS" GOSP
    // generate_index "$K2 $K3 $K4 $K1" "$DATA_QUADS" SPOG
    // generate_index "$K3 $K4 $K2 $K1" "$DATA_QUADS" POSG
    // generate_index "$K4 $K2 $K3 $K1" "$DATA_QUADS" OSPG

    public static void exec(String location, String indexName, int sortThreads, /*unused*/String sortIndexArgs, XLoaderFiles loaderFiles) {

        Timer timer = new Timer();
        FmtLog.info(BulkLoaderX.LOG_Index, "Build index %s", indexName);

        timer.startTimer();
        long items = ProcBuildIndexX.exec2(location, indexName, sortThreads, sortIndexArgs, loaderFiles);
        long timeMillis = timer.endTimer();

        double xSec = timeMillis/1000.0;
        double rate = items/xSec;
        String elapsedStr = BulkLoaderX.milliToHMS(timeMillis);
        String rateStr = BulkLoaderX.rateStr(items, timeMillis);

        FmtLog.info(BulkLoaderX.LOG_Index, "%s Index %s : %s seconds - %s at %s TPS", BulkLoaderX.StepMarker, indexName, Timer.timeStr(timeMillis), elapsedStr, rateStr);
    }

    private static long exec2(String location, String indexName, int sortThreads, String sortIndexArgs, XLoaderFiles loaderFiles) {
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(location);
        long x = buildIndex(dsg, indexName, sortThreads, sortIndexArgs, loaderFiles);
        TDBInternal.expel(dsg);
        return x;
    }

    private static long buildIndex(DatasetGraph dsg, String indexName, int sortThreads, String sortIndexArgs, XLoaderFiles loaderFiles) {
        long tickPoint = BulkLoaderX.DataTick;
        int superTick = BulkLoaderX.DataSuperTick;
        String K1 = "--key=1,1";
        String K2 = "--key=2,2";
        String K3 = "--key=3,3";
        String K4 = "--key=4,4";

        switch (indexName) {
            case "SPO" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.triplesFile, dsg, "SPO", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K1, K2, K3));
            case "POS" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.triplesFile, dsg, "POS", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K2, K3, K1));
            case "OSP" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.triplesFile, dsg, "OSP", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K3, K1, K2));
            case "GSPO" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.quadsFile, dsg, "GSPO", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K1, K2, K3, K4));
            case "GPOS" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.quadsFile, dsg, "GPOS", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K1, K3, K4, K2));
            case "GOSP" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.quadsFile, dsg, "GOSP", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K1, K4, K2, K3));
            case "SPOG" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.quadsFile, dsg, "SPOG", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K2, K3, K4, K1));
            case "POSG" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.quadsFile, dsg, "POSG", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K3, K4, K2, K1));
            case "OSPG" :
                return sort_build_index(BulkLoaderX.LOG_Index, loaderFiles.quadsFile, dsg, "OSPG", sortThreads, sortIndexArgs, tickPoint, superTick, loaderFiles.TMPDIR, List.of(K4, K2, K3, K1));
            default :
                throw new TDBException("Index name '" + indexName + "' not recognized");
        }
    }

    private static boolean isEmpty(String datafile) {
        // If empty file, do nothing.
        Path pathData = Paths.get(datafile);
        try {
            if ( Files.isDirectory(pathData) ) {}
            long x = Files.size(pathData);
            return x == 0;
        } catch (IOException ex) {
            IO.exception(ex);
            return true;
        }
    }

    private static long sort_build_index(Logger LOG, String datafile, DatasetGraph dsg, String indexName,
                                         int sortThreads, String sortIndexArgs, long tickPoint, int superTick,
                                         String TMPDIR,
                                         List<String>sortKeyArgs) {
        if ( isEmpty(datafile) )
            return 0;
        // Sort task.
        Process proc2;
        OutputStream toSortOutputStream; // Not used. Input is a file.
        InputStream fromSortInputStream;

        if ( sortThreads <= 0 )
            sortThreads = 2;

        try {
            //LOG.info("Step : external sort : "+indexName);
            //if ( sortArgs != null ) {}

            List<String> sortCmd = new ArrayList<>(Arrays.asList(
                 "sort",
                    "--temporary-directory="+TMPDIR,
                    "--buffer-size=50%",
                    "--parallel="+sortThreads,
                    "--unique"
            ));

            if ( BulkLoaderX.CompressSortIndexFiles )
                sortCmd.add("--compress-program="+BulkLoaderX.gzipProgram());

            // Sort order
            sortCmd.addAll(sortKeyArgs);

            // Add the file to sort if not compressed.
            if ( ! BulkLoaderX.CompressDataFiles )
                sortCmd.add(datafile);
            // else this process will decompress and send the data.

            //if ( sortIndexArgs != null ) {}

            ProcessBuilder pb2 = new ProcessBuilder(sortCmd);
            pb2.environment().put("LC_ALL","C");
            proc2 = pb2.start();

            // To process. Not used if uncompressed file.
            toSortOutputStream = proc2.getOutputStream();
            // From process
            fromSortInputStream = proc2.getInputStream(); // Needs buffering
//            // Debug sort process.
//            InputStream fromSortErrortStream = proc2.getErrorStream();
//            IOUtils.copy(fromSortErrortStream, System.err);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if ( BulkLoaderX.CompressDataFiles ) {
            // Handles .gz
            InputStream inData = IO.openFile(datafile);
            try {
                inData.transferTo(toSortOutputStream);
                toSortOutputStream.close();
            } catch (IOException ex) { IO.exception(ex); }
        }

        // From sort, buffered.
        InputStream input = IO.ensureBuffered(fromSortInputStream);
        // This thread - run builder.
        long count = indexBuilder(dsg, input, indexName);
        try {
            int exitCode = proc2.waitFor();
            if ( exitCode != 0 ) {
                String msg = IO.readWholeFileAsUTF8(proc2.getErrorStream());
                String logMsg = String.format("Sort RC = %d : Error: %s", exitCode, msg);
                Log.error(LOG, logMsg);
                // ** Exit process
                System.exit(exitCode);
            }
//            else
//                LOG.info("Sort finished");
        } catch (InterruptedException e) {
            LOG.error("Failed to cleanly wait-for the subprocess");
            throw new RuntimeException(e);
        } finally {
            IO.close(toSortOutputStream);
            IO.close(fromSortInputStream);
        }
        return count;
    }

    private static long indexBuilder(DatasetGraph dsg, InputStream input, String indexName) {
        long tickPoint = BulkLoaderX.DataTick;
        int superTick = BulkLoaderX.DataSuperTick;

        // Location of storage, not the DB.
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        Location location = dsgtdb.getLocation();

        int keyLength = SystemTDB.SizeOfNodeId * indexName.length();
        int valueLength = 0;

        // The name is the order.
        String primary = indexName;

        String primaryOrder;
        int dftKeyLength;
        int dftValueLength;
        int tupleLength = indexName.length();

        TupleIndex index;
        if ( tupleLength == 3 ) {
            primaryOrder = Names.primaryIndexTriples;
            dftKeyLength = SystemTDB.LenIndexTripleRecord;
            dftValueLength = 0;
            // Find index.
            index = findIndex(dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes()
                             , indexName);
        } else if ( tupleLength == 4 ) {
            primaryOrder = Names.primaryIndexQuads;
            dftKeyLength = SystemTDB.LenIndexQuadRecord;
            dftValueLength = 0;
            index = findIndex(dsgtdb.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes()
                             , indexName);
        } else {
            throw new TDBException("Index name: " + indexName);
        }

        TupleMap colMap = TupleMap.create(primaryOrder, indexName);

        int readCacheSize = 10;
        int writeCacheSize = 100;

        int blockSize = SystemTDB.BlockSize;
        RecordFactory recordFactory = new RecordFactory(dftKeyLength, dftValueLength);

        int order = BPlusTreeParams.calcOrder(blockSize, recordFactory);
        BPlusTreeParams bptParams = new BPlusTreeParams(order, recordFactory);

        int blockSizeNodes = blockSize;
        int blockSizeRecords = blockSize;

        FileSet destination = new FileSet(location, indexName);
        BufferChannel blkState = FileFactory.createBufferChannel(destination, Names.extBptState);
        BlockMgr blkMgrNodes = BlockMgrFactory.create(destination, Names.extBptTree, blockSizeNodes, readCacheSize, writeCacheSize);
        BlockMgr blkMgrRecords = BlockMgrFactory.create(destination, Names.extBptRecords, blockSizeRecords, readCacheSize, writeCacheSize);

        int rowBlock = 1000;
        Iterator<Record> iter = new RecordsFromInput(input, tupleLength, colMap, rowBlock);
        // ProgressMonitor.
        ProgressMonitor monitor = ProgressMonitorOutput.create(BulkLoaderX.LOG_Index, indexName, tickPoint, superTick);
        ProgressIterator<Record> iter2 = new ProgressIterator<>(iter, monitor);
        monitor.start();
        BPlusTree bpt2 = BPlusTreeRewriter.packIntoBPlusTree(iter2, bptParams, recordFactory, blkState, blkMgrNodes, blkMgrRecords);
        bpt2.close();
        monitor.finish();
        // [BULK] End stage.
        long count = monitor.getTicks();
        return count;
    }

    private static TupleIndex findIndex(TupleIndex[] indexes, String indexName) {
        for ( TupleIndex idx : indexes ) {
            if ( indexName.equals(idx.getName()) )
                return idx;
        }
        throw new TDBException("Failed to find index: "+indexName+" in "+Arrays.asList(indexes));
    }
}
