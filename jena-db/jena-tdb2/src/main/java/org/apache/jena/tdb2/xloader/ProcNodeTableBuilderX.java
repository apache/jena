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
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.progress.ProgressIterator;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.lib.NodeLib;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.Hash;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableTRDF;
import org.apache.jena.tdb2.store.nodetable.TReadAppendFileTransport;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;

public class ProcNodeTableBuilderX {

    /** Pair<triples, indexed nodes> */
    // [BULK] Output, not return.
    public static Pair<Long, Long> exec(Logger LOG1, Logger LOG2, String DB, XLoaderFiles loaderFiles, List<String> datafiles, String sortNodeTableArgs) {
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

        // ** Step 2: The sort
        Process proc2;
        try {
            //LOG.info("Step : external sort");
            List<String> sortCmdBasics = Arrays.asList(
                  "sort",
                    "--temporary-directory="+loaderFiles.TMPDIR, "--buffer-size=50%",
                    "--parallel=2", "--unique",
                    //"--compress-program=/usr/bin/gzip",
                    "--key=1,1"
                    );

            List<String> sortCmd = new ArrayList<>(sortCmdBasics);

            //if ( sortNodeTableArgs != null ) {}

            // See javadoc for CompressSortNodeTableFiles - usually false
            if ( BulkLoaderX.CompressSortNodeTableFiles )
                sortCmd.add("--compress-program=/usr/bin/gzip");
            proc2 = new ProcessBuilder(sortCmd).start();

            // To process.
            toSortOutputStream = proc2.getOutputStream(); // Needs buffering
            // From process
            fromSortInputStream = proc2.getInputStream(); // Needs buffering
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

        //LOG.info("Step : parse & send to external sort");
        Runnable task1 = ()->{
            ProgressMonitorOutput monitor = ProgressMonitorOutput.create(LOG1, "Nodes", tickPoint, superTick);
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
            FmtLog.info(LOG1, "== Parse: %s seconds : %,d triples/quads %,.0f TPS", Timer.timeStr(x), count, rate);
        };

        // [BULK] XXX AsyncParser.asyncParse(files, output)
        Thread thread1 = async(task1, "AsyncParser");

        // Step3: build node table.
        Runnable task3 = ()->{
            Timer timer = new Timer();
            // Don't start timer until sort send something
            InputStream input = IO.ensureBuffered(fromSortInputStream);

            FileSet fileSet = new FileSet(dsgtdb.getLocation(), Names.nodeTableBaseName);
            BufferChannel blkState = FileFactory.createBufferChannel(fileSet, Names.extBptState);
            long idxTickPoint = BulkLoaderX.DataTick;
            int idxSuperTick = BulkLoaderX.DataSuperTick;
            ProgressMonitorOutput monitor = ProgressMonitorOutput.create(LOG2, "Index", idxTickPoint, idxSuperTick);

            // Library of tools!
            dsg.executeWrite(()->{
                BinaryDataFile objectFile = nodeTable.getData();
                Iterator<Record> rIter = records(LOG2, input, objectFile);
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
            FmtLog.info(LOG2, "==  Index: %s seconds : %,d indexed RDF terms : %s PerSecond", Timer.timeStr(x), count, rateStr);

        };
        Thread thread3 = async(task3, "AsyncBuild");

        try {
            int exitCode = proc2.waitFor();
            if ( exitCode != 0 )
                FmtLog.error(LOG2, "Sort RC = %d", exitCode);
            else
                LOG2.info("Sort finished");
            System.exit(exitCode);
            IO.close(toSortOutputStream);
            IO.close(fromSortInputStream);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BulkLoaderX.waitFor(thread1);
        BulkLoaderX.waitFor(thread3);

        return Pair.create(countParseTicks.get(), countIndexedNodes.get());
    }

    // [BULK] XXX Make this a separate class.
    static Iterator<Record> records(Logger LOG, InputStream input, BinaryDataFile objectFile) {
        RecordFactory factory = new RecordFactory(SystemTDB.LenNodeHash,  NodeId.SIZE);
        byte[] bHash = new byte[SystemTDB.LenNodeHash];
        byte[] bbNodeId = new byte[NodeId.SIZE];
        RDF_Term term = new RDF_Term();
        // Better?
        TProtocol protocol;
        try {
            TTransport transport = new TReadAppendFileTransport(objectFile);
            if ( ! transport.isOpen() )
                transport.open();
            protocol = TRDF.protocol(transport);
        }
        catch (Exception ex) {
            throw new TDBException("NodeTableTRDF", ex);
        }

        return new IteratorSlotted<Record>() {
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
        };
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
