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

import java.io.OutputStream;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.BitsLong;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIProvider;
import org.apache.jena.irix.SystemIRIx;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.solver.stats.Stats;
import org.apache.jena.tdb2.solver.stats.StatsCollectorNodeId;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.value.DoubleNode62;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.slf4j.Logger;

/** Create the Node table and write the triples/quads temporary files */
public class ProcNodeTableDataBuilder {
    // See also TDB1 ProcNodeTableBuilder

    private static Logger cmdLog = TDB2.logLoader;

    // Node Table.
    public static void exec(DatasetGraph dsg,
                            String dataFileTriples, String dataFileQuads,
                            List<String> datafiles, boolean collectStats) {
        // Possible parser speed up. This has no effect if parsing in parallel
        // because the parser isn't the slowest step in loading at scale.
        IRIProvider provider = SystemIRIx.getProvider();
        //SystemIRIx.setProvider(new IRIProviderAny());

        // This builds via the container.
        Location location = TDBInternal.getDatabaseContainer(dsg).getLocation();
        //System.out.printf("ProcNodeTableBuilder: location=%s\n", location);

        // This formats the location correctly.
        // But we're not really interested in it all, just setting up the node table.

        ProgressMonitor monitor = ProgressMonitorOutput.create(cmdLog, "Data", BulkLoaderX.DataTick, BulkLoaderX.DataSuperTick);
        // WriteRows does it's own buffering and has direct write-to-buffer.
        // Do not buffer here.
        OutputStream outputTriples = IO.openOutputFile(dataFileTriples);
        OutputStream outputQuads = IO.openOutputFile(dataFileQuads);

        dsg.executeWrite(()->build(dsg, monitor,
                                   outputTriples, outputQuads,
                                   datafiles));
        TDBInternal.expel(dsg);
        SystemIRIx.setProvider(provider);
    }

    private static void build(DatasetGraph dsg, ProgressMonitor monitor,
                              OutputStream outputTriples, OutputStream outputQuads,
                              List<String> datafiles) {
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        NodeTableBuilder sink = new NodeTableBuilder(dsgtdb, monitor, outputTriples, outputQuads, false);
        Timer timer = new Timer();
        timer.startTimer();
        monitor.start();
        sink.startBulk();
        AsyncParser.asyncParse(datafiles, sink);
//        for( String filename : datafiles) {
//            if ( datafiles.size() > 0 )
//                cmdLog.info("Load: "+filename+" -- "+DateTimeUtils.nowAsString());
//            RDFParser.source(filename).parse(sink);
//        }
        sink.finishBulk();
        IO.close(outputTriples);
        IO.close(outputQuads);

        // ---- Stats

        // See Stats class.
        if ( sink.getCollector() != null ) {
            Location location = dsgtdb.getLocation();
            if ( ! location.isMem() )
                Stats.write(location.getPath(Names.optStats), sink.getCollector().results());
        }

        // ---- Monitor
        monitor.finish();
        long time = timer.endTimer();
        long total = monitor.getTicks();
        float elapsedSecs = time/1000F;
        float rate = (elapsedSecs!=0) ? total/elapsedSecs : 0;
        String str =  String.format("Total: %,d tuples : %,.2f seconds : %,.2f tuples/sec [%s]",
                                    total, elapsedSecs, rate, DateTimeUtils.nowAsString());
        cmdLog.info(str);
    }

    static class NodeTableBuilder implements StreamRDF
    {
        private DatasetGraphTDB dsg;
        private NodeTable nodeTable;
        private WriteRows writerTriples;
        private WriteRows writerQuads;
        private ProgressMonitor monitor;
        private StatsCollectorNodeId stats;

        NodeTableBuilder(DatasetGraphTDB dsg, ProgressMonitor monitor,
                         OutputStream outputTriples, OutputStream outputQuads,
                         boolean collectStats)
        {
            this.dsg = dsg;
            this.monitor = monitor;
            NodeTupleTable ntt = dsg.getTripleTable().getNodeTupleTable();
            this.nodeTable = ntt.getNodeTable();
            this.writerTriples = new WriteRows(outputTriples, 3, 100_000);
            this.writerQuads = new WriteRows(outputQuads, 4, 100_000);
            if ( collectStats )
                this.stats = new StatsCollectorNodeId(nodeTable);
        }

        //@Override
        public void startBulk()
        {}

        @Override
        public void start()
        {}

        @Override
        public void finish()
        {}

        //@Override
        public void finishBulk()
        {
            writerTriples.flush();
            writerQuads.flush();
            nodeTable.sync();

           //dsg.getStoragePrefixes().sync();
        }

        @Override
        public void triple(Triple triple)
        {
            Node s = triple.getSubject();
            Node p = triple.getPredicate();
            Node o = triple.getObject();
            process(Quad.tripleInQuad,s,p,o);
        }

        @Override
        public void quad(Quad quad)
        {
            Node s = quad.getSubject();
            Node p = quad.getPredicate();
            Node o = quad.getObject();
            Node g = null;
            // Union graph?!
            if ( ! quad.isTriple() && ! quad.isDefaultGraph() )
                g = quad.getGraph();
            process(g,s,p,o);
        }

        // --> From NodeIdFactory
        private static long encode(NodeId nodeId) {
            long x = nodeId.getPtrLocation(); // Should be "getValue"
            switch(nodeId.type()) {
                case PTR:
                    return x;
                case XSD_DOUBLE:
                    // XSD_DOUBLE is special.
                    // Set value bit (63) and bit 62
                    x = DoubleNode62.insertType(x);
                    return x;
                default:
                    // Bit 62 is zero - tag is for doubles.
                    x = BitsLong.pack(x, nodeId.getTypeValue(), 56, 62);
                    // Set the high, value bit.
                    x = BitsLong.set(x, 63);
                    return x;
            }
        }

        private void write(WriteRows out, NodeId nodeId) {
            long x = encode(nodeId);
            out.write(x);
        }

        private void process(Node g, Node s, Node p, Node o) {
            NodeId sId = nodeTable.getAllocateNodeId(s);
            NodeId pId = nodeTable.getAllocateNodeId(p);
            NodeId oId = nodeTable.getAllocateNodeId(o);

            if ( g != null ) {
                NodeId gId = nodeTable.getAllocateNodeId(g);
                write(writerQuads, gId);
                write(writerQuads, sId);
                write(writerQuads, pId);
                write(writerQuads, oId);
                writerQuads.endOfRow();
                if ( stats != null )
                    stats.record(gId, sId, pId, oId);
            } else {
                write(writerTriples, sId);
                write(writerTriples, pId);
                write(writerTriples, oId);
                writerTriples.endOfRow();
                if ( stats != null )
                    stats.record(null, sId, pId, oId);
            }
            monitor.tick();
        }

        public StatsCollectorNodeId getCollector() { return stats; }

        @Override
        public void base(String base)
        {}

        @Override
        public void prefix(String prefix, String iri) {
            dsg.prefixes().add(prefix, iri);
        }
    }
}
