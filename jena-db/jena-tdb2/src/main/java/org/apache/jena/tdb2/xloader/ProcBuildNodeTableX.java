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

import static org.apache.jena.tdb2.xloader.BulkLoaderX.async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.atlas.lib.*;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.base.file.FileFactory;
import org.apache.jena.dboe.base.file.FileSet;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeParams;
import org.apache.jena.dboe.trans.bplustree.rewriter.BPlusTreeRewriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIProvider;
import org.apache.jena.irix.SystemIRIx;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.thrift.RiotThriftException;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.progress.ProgressIterator;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.lib.NodeLib;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.Hash;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableTRDF;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.slf4j.Logger;

/*
 * Build the node table.
 *
 * <ul>
 * <li>Step 1: Extract nodes from the input parser, writes (hash, terms in encoded RDF Thrift).
 * <li>Step 2: Sort by hash and remove duplicates.
 * <li>Step 2: Write node table data file and write node table index (B+tree).
 * </ul>
 * Outcome: complete node table.
 */
public class ProcBuildNodeTableX {
    public static void exec(String location, XLoaderFiles loaderFiles, int sortThreads, String sortNodeTableArgs, List<String> datafiles) {
        Timer timer = new Timer();
        timer.startTimer();
        FmtLog.info(BulkLoaderX.LOG_Nodes, "Build node table");
//        FmtLog.info(LOG1, "  Database   = %s", location);
//        FmtLog.info(LOG1, "  TMPDIR     = %s", tmpdir==null?"unset":tmpdir);
//        FmtLog.info(LOG1, "  Data files = %s", StrUtils.strjoin(datafiles, " "));
        Pair<Long/*triples or quads*/, Long/*indexed nodes*/> buildCounts =
                ProcBuildNodeTableX.exec2(location, loaderFiles, sortThreads, sortNodeTableArgs, datafiles);
        long timeMillis = timer.endTimer();

        long items = buildCounts.getLeft();
        double xSec = timeMillis/1000.0;
        double rate = items/xSec;
        String elapsedStr = BulkLoaderX.milliToHMS(timeMillis);
        String rateStr = BulkLoaderX.rateStr(items, timeMillis);

        FmtLog.info(BulkLoaderX.LOG_Terms, "%s NodeTable : %s seconds - %s at %s terms per second", BulkLoaderX.StepMarker,
                    Timer.timeStr(timeMillis), elapsedStr, rateStr);
    }

    /** Pair<triples, indexed nodes>
     * @param sortThreads */
    // [BULK] Output, not return.
    private static Pair<Long, Long> exec2(String DB, XLoaderFiles loaderFiles, int sortThreads, String sortNodeTableArgs, List<String> datafiles) {

        //Threads - 1 parser, 1 builder, 2 sort.
        // Steps:
        // 1 - parser to and pipe terms to sort
        // 2 - sort
        // 3 - build node table from unique sort

        IRIProvider provider = SystemIRIx.getProvider();
        //SystemIRIx.setProvider(new IRIProviderAny());

        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(DB);
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        NodeTable nt = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable();
        NodeTableTRDF nodeTable = (NodeTableTRDF)nt.baseNodeTable();

        OutputStream toSortOutputStream;
        InputStream fromSortInputStream;

        if ( sortThreads <= 0 )
            sortThreads = 2;

        // ** Step 2: The sort
        Process procSort;
        try {
            //LOG.info("Step : external sort");
            // Mutable list.
            List<String> sortCmd = new ArrayList<>(Arrays.asList(
                  "sort",
                    "--temporary-directory="+loaderFiles.TMPDIR,
                    "--buffer-size=50%",
                    "--parallel="+sortThreads,
                    "--unique",
                    "--key=1,1"
                    ));

            if ( BulkLoaderX.CompressSortNodeTableFiles )
                sortCmd.add("--compress-program="+BulkLoaderX.gzipProgram());

            //if ( sortNodeTableArgs != null ) {}

            ProcessBuilder pb2 = new ProcessBuilder(sortCmd);
            pb2.environment().put("LC_ALL","C");
            procSort = pb2.start();

            // To process.
            // Let the writer close it.
            toSortOutputStream = procSort.getOutputStream();
            // From process to the tree builder.
            // Let the reader side close it.
            fromSortInputStream = procSort.getInputStream();
//            // Debug sort process.
//            InputStream fromSortErrortStream = proc2.getErrorStream();
//            IOUtils.copy(fromSortErrortStream, System.err);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // ** Step 1 : write intermediate file (hash, thrift bytes).
        AtomicLong countParseTicks = new AtomicLong(-1);
        AtomicLong countIndexedNodes = new AtomicLong(-1);

        long tickPoint = BulkLoaderX.DataTick;
        int superTick = BulkLoaderX.DataSuperTick;

        Runnable task1 = ()->{
            ProgressMonitorOutput monitor = ProgressMonitorOutput.create(BulkLoaderX.LOG_Nodes, "Nodes", tickPoint, superTick);
            OutputStream output = IO.ensureBuffered(toSortOutputStream);
            // Counting.
            StreamRDF worker = new NodeHashTmpStream(output);
            ProgressStreamRDF stream = new ProgressStreamRDF(worker, monitor);
            monitor.start();
            String label = monitor.getLabel();
            datafiles.forEach( datafile->{
                String basename = FileOps.basename(datafile);
                monitor.setLabel(basename);
                stream.start();
                RDFParser.source(datafile).parse(stream);
                stream.finish();
            });
            monitor.finish();
            monitor.setLabel(label);

            IO.flush(output);
            IO.close(output);

            long x = monitor.getTime();

//            long x = timer.endTimer();
            long count = monitor.getTicks();
            countParseTicks.set(count);

            double xSec = x/1000.0;
            double rate = count/xSec;
            FmtLog.info(BulkLoaderX.LOG_Nodes, "%s Parse (nodes): %s seconds : %,d triples/quads %,.0f TPS", BulkLoaderX.StageMarker,
                        Timer.timeStr(x), count, rate);
        };

        // [BULK] XXX AsyncParser.asyncParse(files, output)
        Thread thread1 = async(task1, "AsyncParser");

        // Step3: build node table.
        Runnable task3 = ()->{
            Timer timer = new Timer();
            // Don't start timer until sort send something

            // Process stream are already buffered.
            InputStream input = IO.ensureBuffered(fromSortInputStream);

            FileSet fileSet = new FileSet(dsgtdb.getLocation(), Names.nodeTableBaseName);
            BufferChannel blkState = FileFactory.createBufferChannel(fileSet, Names.extBptState);
            long idxTickPoint = BulkLoaderX.DataTick;
            int idxSuperTick = BulkLoaderX.DataSuperTick;
            ProgressMonitorOutput monitor = ProgressMonitorOutput.create(BulkLoaderX.LOG_Terms, "Index", idxTickPoint, idxSuperTick);

            // Library of tools!
            dsg.executeWrite(()->{
                BinaryDataFile objectFile = nodeTable.getData();
                Iterator<Record> rIter = records(BulkLoaderX.LOG_Terms, input, objectFile);
                rIter = new ProgressIterator<>(rIter, monitor);
                // Record of (hash, nodeId)
                BPlusTree bpt1 = (BPlusTree)(nodeTable.getIndex());
                BPlusTreeParams bptParams = bpt1.getParams();
                RecordFactory factory = new RecordFactory(SystemTDB.LenNodeHash,  NodeId.SIZE);
                // Wait until something has been received from the sort step
                rIter.hasNext();
                monitor.start();
                // .. then start the timer. It is closed after the transaction finishes.
                timer.startTimer();

                BPlusTree bpt2 = BPlusTreeRewriter.packIntoBPlusTree(rIter,
                                                                     bptParams, factory, blkState,
                                                                     bpt1.getNodeManager().getBlockMgr(),
                                                                     bpt1.getRecordsMgr().getBlockMgr());
                bpt2.sync();
                bpt1.sync();
                objectFile.sync();
                monitor.finish();
            });
            blkState.sync();
            IO.close(input);
            long x = timer.endTimer();
            long count = monitor.getTicks();
            countIndexedNodes.set(count);
            String rateStr = BulkLoaderX.rateStr(count, x);
            FmtLog.info(BulkLoaderX.LOG_Terms, "%s Index terms: %s seconds : %,d indexed RDF terms : %s PerSecond", BulkLoaderX.StageMarker, Timer.timeStr(x), count, rateStr);
        };
        Thread thread3 = async(task3, "AsyncBuild");

        try {
            int exitCode = procSort.waitFor();
            if ( exitCode != 0 ) {
                String msg = IO.readWholeFileAsUTF8(procSort.getErrorStream());
                String logMsg = String.format("Sort RC = %d : Error: %s", exitCode, msg);
                Log.error(BulkLoaderX.LOG_Terms, logMsg);
                // ** Exit process
                System.exit(exitCode);
            } else
                BulkLoaderX.LOG_Terms.info("Sort finished");

            // I/O Stream toSortOutputStream and fromSortInputStream closed by
            // their users - step 1 and step 3.
        } catch (InterruptedException e) {
            BulkLoaderX.LOG_Nodes.error("Failed to cleanly wait-for the subprocess");
            throw new RuntimeException(e);
        }

        BulkLoaderX.waitFor(thread1);
        BulkLoaderX.waitFor(thread3);

        return Pair.create(countParseTicks.get(), countIndexedNodes.get());
    }

    private static Iterator<Record> records(Logger logger, InputStream input, BinaryDataFile objectFile) {
        return new IteratorNodeTableRecords(logger, input, objectFile);
    }

    private static class IteratorNodeTableRecords extends IteratorSlotted<Record> {
        private final static RecordFactory factory = new RecordFactory(SystemTDB.LenNodeHash,  NodeId.SIZE);
        private final byte[] bHash = new byte[SystemTDB.LenNodeHash];
        private final byte[] bbNodeId = new byte[NodeId.SIZE];
        private final RDF_Term term = new RDF_Term();
        private final Logger logger;
        private final InputStream input;
        private final BinaryDataFile objectFile;

        IteratorNodeTableRecords(Logger logger, InputStream input, BinaryDataFile objectFile) {
            this.logger = logger;
            this.input = input;
            this.objectFile = objectFile;
        }

        long count = 0;
        @Override
        protected Record moveToNext() {
            return calc();
        }

        @Override
        protected boolean hasMore() {
            return true;
        }

        // One line of file encoded data to record.
        private Record calc() {
            count++;
            try {
                // read hash.
                for ( int i = 0 ; i < 16 ; i++ ) {
                    int x = hexRead(input);
                    if ( x < 0 )
                        return null;
                    bHash[i] = (byte)(x&0xFF);
                }
                char ch0 = (char)input.read(); // space.
                byte[] key = bHash;

                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                // Read de-hexer
                for(;;) {
                    int v = hexRead(input);
                    if ( v < 0 )
                        break;
                    bout.write(v);
                }
                byte[] thrift = bout.toByteArray();
                ThriftConvert.termFromBytes(term, thrift);
                // write to nodes.dat -> NodeId
                long x = objectFile.length();
                NodeId nodeId = NodeIdFactory.createPtr(x);
                objectFile.write(thrift);
                Bytes.setLong(nodeId.getPtrLocation(), bbNodeId);
                Record r = factory.create(key, bbNodeId);
                return r;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public static int hexRead(InputStream input) throws IOException {
        int c1 = input.read();
        if ( c1 < 0 )
            return -1;
        if ( c1 == '\n' || c1 == ' ')
            return -1;
        int c2 = input.read();
        int b1 = Hex.hexByteToInt(c1);
        int b2 = Hex.hexByteToInt(c2);
        int b = (b1<<4)|b2;
        return b;
    }

    public static void hexWrite(OutputStream output, int bits8) throws IOException {
        int x1 = (bits8>>4) & 0xF;
        int x2 = bits8 & 0xF;
        byte ch1 = Bytes.hexDigitsUC[x1];
        byte ch2 = Bytes.hexDigitsUC[x2];
        output.write(ch1);
        output.write(ch2);
    }


    static byte[] hashNode(Node node) {
        NodeLib.setHash(hash, node);
        return hash.getBytes();
    }

    private static Hash hash = new Hash(SystemTDB.LenNodeHash);

    //Cache needed to reduce duplicates
    /** Write the intermediate sort file */
    static class NodeHashTmpStream implements StreamRDF {

        private final OutputStream outputData;
        private CacheSet<Node> cache = CacheFactory.createCacheSet(500_000);

        NodeHashTmpStream(OutputStream outputFile) {
            this.outputData = outputFile;
        }

        @Override
        public void start() {}

        @Override
        public void triple(Triple triple) {
            node(triple.getSubject());
            node(triple.getPredicate());
            node(triple.getObject());
        }

        @Override
        public void quad(Quad quad) {
            node(quad.getGraph());
            node(quad.getSubject());
            node(quad.getPredicate());
            node(quad.getObject());
        }

        static TSerializer serializer;
        static {
            try {
                serializer = new TSerializer(new TCompactProtocol.Factory());
            }
            catch (TException e) {
                throw new RiotThriftException(e);
            }
        }

        private void node(Node node) {
            NodeId nid = NodeId.inline(node);
            if ( nid != null )
                return ;
            if ( cache.contains(node) )
                return;
            cache.add(node);
            // -- Hash of node
            NodeLib.setHash(hash, node);
            try {
                byte k[] = hash.getBytes();
                RDF_Term term = ThriftConvert.convert(node, false);
                byte[] tBytes = serializer.serialize(term);
                write(outputData, k);
                outputData.write(' ');
                write(outputData, tBytes);
                outputData.write('\n');
            } catch (TException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void write(OutputStream outputData, byte[] bytes) throws IOException {
            for ( byte bits8 : bytes )
                hexWrite(outputData, bits8);
        }

        @Override
        public void base(String base) {}

        @Override
        public void prefix(String prefix, String iri) {}

        @Override
        public void finish() {
            IO.flush(outputData);
        }
    }
}
