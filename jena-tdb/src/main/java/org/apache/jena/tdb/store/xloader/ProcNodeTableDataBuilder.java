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

package org.apache.jena.tdb.store.xloader;

import java.io.OutputStream ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.atlas.lib.ProgressMonitor ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.irix.IRIProvider;
import org.apache.jena.irix.SystemIRIx;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.setup.DatasetBuilderStd ;
import org.apache.jena.tdb.solver.stats.Stats ;
import org.apache.jena.tdb.solver.stats.StatsCollectorNodeId ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.bulkloader.BulkLoader ;
import org.apache.jena.tdb.store.bulkloader.BulkStreamRDF ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable ;
import org.apache.jena.tdb.sys.Names ;
import org.apache.jena.tdb.sys.TDBInternal;
import org.slf4j.Logger ;

/** Create the Node table and write the triples/quads temporary files */
public class ProcNodeTableDataBuilder {
    // See also TDB2 ProcNodeTableBuilder
    private static Logger cmdLog = TDB.logLoader ;

    public static void exec(Location location,
                            String dataFileTriples, String dataFileQuads,
                            List<String> datafiles, boolean collectStats) {
        // Possible parser speed up. This has no effect if parsing in parallel
        // because the parser isn't the slowest step in loading at scale.
        IRIProvider provider = SystemIRIx.getProvider();
        //SystemIRIx.setProvider(new IRIProviderAny());

        // This formats the location correctly.
        // But we're not really interested in it all.
        DatasetGraphTDB dsg = DatasetBuilderStd.create(location) ;

        // so close indexes and the prefix table.
        dsg.getTripleTable().getNodeTupleTable().getTupleTable().close();
        dsg.getQuadTable().getNodeTupleTable().getTupleTable().close();

        ProgressMonitor monitor = ProgressMonitor.create(cmdLog, "Data", BulkLoader.DataTickPoint, BulkLoader.superTick) ;
        // WriteRows does it's own buffering and has direct write-to-buffer.
        // Do not buffer here.
        OutputStream outputTriples = IO.openOutputFile(dataFileTriples);
        OutputStream outputQuads = IO.openOutputFile(dataFileQuads);

        build(dsg, monitor, outputTriples, outputQuads,datafiles);

        TDBInternal.expel(dsg);
        SystemIRIx.setProvider(provider);
    }

    private static void build(DatasetGraph dsg, ProgressMonitor monitor,
                              OutputStream outputTriples, OutputStream outputQuads,
                              List<String> datafiles) {
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        NodeTableBuilder sink = new NodeTableBuilder(dsgtdb, monitor, outputTriples, outputQuads, false) ;
        monitor.start() ;
        sink.startBulk() ;
        AsyncParser.asyncParse(datafiles, sink);
//        for( String filename : datafiles) {
//            if ( datafiles.size() > 0 )
//                cmdLog.info("Load: "+filename+" -- "+DateTimeUtils.nowAsString()) ;
//            RDFParser.source(filename).parse(sink);
//        }
        sink.finishBulk() ;
        IO.close(outputTriples) ;
        IO.close(outputQuads) ;

        // ---- Stats

        // See Stats class.
        if ( sink.getCollector() != null ) {
            Location location = dsgtdb.getLocation();
            if ( ! location.isMem() )
                Stats.write(location.getPath(Names.optStats), sink.getCollector().results()) ;
        }

        // ---- Monitor
        long time = monitor.finish() ;

        long total = monitor.getTicks() ;
        float elapsedSecs = time/1000F ;
        float rate = (elapsedSecs!=0) ? total/elapsedSecs : 0 ;
        String str =  String.format("Total: %,d tuples : %,.2f seconds : %,.2f tuples/sec [%s]",
                                    total, elapsedSecs, rate, DateTimeUtils.nowAsString()) ;
        cmdLog.info(str) ;
    }

    static class NodeTableBuilder implements BulkStreamRDF
    {
        private DatasetGraphTDB dsg ;
        private NodeTable nodeTable ;
        private WriteRows writerTriples ;
        private WriteRows writerQuads ;
        private ProgressMonitor monitor ;
        private StatsCollectorNodeId stats ;

        NodeTableBuilder(DatasetGraphTDB dsg, ProgressMonitor monitor,
                         OutputStream outputTriples, OutputStream outputQuads,
                         boolean collectStats)
        {
            this.dsg = dsg ;
            this.monitor = monitor ;
            NodeTupleTable ntt = dsg.getTripleTable().getNodeTupleTable() ;
            this.nodeTable = ntt.getNodeTable() ;
            this.writerTriples = new WriteRows(outputTriples, 3, 20000) ;
            this.writerQuads = new WriteRows(outputQuads, 4, 20000) ;
            if ( collectStats )
                this.stats = new StatsCollectorNodeId(nodeTable) ;
        }

        @Override
        public void startBulk()
        {}

        @Override
        public void start()
        {}

        @Override
        public void finish()
        {}

        @Override
        public void finishBulk()
        {
            writerTriples.flush() ;
            writerQuads.flush() ;
            nodeTable.sync() ;
            dsg.getStoragePrefixes().sync() ;
        }

        @Override
        public void triple(Triple triple)
        {
            Node s = triple.getSubject() ;
            Node p = triple.getPredicate() ;
            Node o = triple.getObject() ;
            process(Quad.tripleInQuad,s,p,o);
        }

        @Override
        public void quad(Quad quad)
        {
            Node s = quad.getSubject() ;
            Node p = quad.getPredicate() ;
            Node o = quad.getObject() ;
            Node g = null ;
            // Union graph?!
            if ( ! quad.isTriple() && ! quad.isDefaultGraph() )
                g = quad.getGraph() ;
            process(g,s,p,o);
        }


        private void process(Node g, Node s, Node p, Node o)
        {
            NodeId sId = nodeTable.getAllocateNodeId(s) ;
            NodeId pId = nodeTable.getAllocateNodeId(p) ;
            NodeId oId = nodeTable.getAllocateNodeId(o) ;

            if ( g != null )
            {
                NodeId gId = nodeTable.getAllocateNodeId(g) ;
                writerQuads.write(gId.getId()) ;
                writerQuads.write(sId.getId()) ;
                writerQuads.write(pId.getId()) ;
                writerQuads.write(oId.getId()) ;
                writerQuads.endOfRow() ;
                if ( stats != null )
                    stats.record(gId, sId, pId, oId) ;
            }
            else
            {
                writerTriples.write(sId.getId()) ;
                writerTriples.write(pId.getId()) ;
                writerTriples.write(oId.getId()) ;
                writerTriples.endOfRow() ;
                if ( stats != null )
                    stats.record(null, sId, pId, oId) ;
            }
            monitor.tick() ;
        }

        public StatsCollectorNodeId getCollector() { return stats ; }

        @Override
        public void base(String base)
        {}

        @Override
        public void prefix(String prefix, String iri)
        {
            dsg.getStoragePrefixes().getPrefixMap().add(prefix, iri) ;
        }
    }


}

