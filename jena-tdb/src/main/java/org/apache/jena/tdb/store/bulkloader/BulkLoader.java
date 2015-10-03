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

package org.apache.jena.tdb.store.bulkloader ;

import java.io.InputStream ;
import java.util.List ;

import org.apache.jena.atlas.event.EventType ;
import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBException ;
import org.apache.jena.tdb.solver.stats.Stats ;
import org.apache.jena.tdb.solver.stats.StatsCollector ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable ;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTableView ;
import org.apache.jena.tdb.sys.Names ;
import org.slf4j.Logger ;

/** Overall framework for bulk loading */
public class BulkLoader {
    // Coordinate the NodeTupleTable loading.

    /** Tick point for messages during loading of data */
    public static int       DataTickPoint         = 50 * 1000 ;
    /** Tick point for messages during secondary index creation */
    public static long      IndexTickPoint        = 100 * 1000 ;

    /** Number of ticks per super tick */
    public static int       superTick             = 10 ;

    private static String   baseName              = "http://jena.apache.org/TDB/bulkload/event#" ;

    public static EventType evStartBulkload       = new EventType(baseName + "start-bulkload") ;
    public static EventType evFinishBulkload      = new EventType(baseName + "finish-bulkload") ;

    public static EventType evStartDataBulkload   = new EventType(baseName + "start-bulkload-data") ;
    public static EventType evFinishDataBulkload  = new EventType(baseName + "finish-bulkload-data") ;

    public static EventType evStartIndexBulkload  = new EventType(baseName + "start-bulkload-index") ;
    public static EventType evFinishIndexBulkload = new EventType(baseName + "finish-bulkload-index") ;

    static private Logger   loadLogger            = TDB.logLoader ;

    // Event callbacks for the load stages?
    // On what object? The dataset.

    // /** Load into default graph */
    // public static void loadTriples(DatasetGraphTDB dsg, String url, boolean
    // showProgress)
    // {
    // loadTriples(dsg, asList(url) , showProgress) ;
    // }

    /** Load into default graph */
    public static void loadDefaultGraph(DatasetGraphTDB dsg, List<String> urls, boolean showProgress, boolean collectStats) {
        BulkStreamRDF dest = destinationDefaultGraph(dsg, showProgress, collectStats) ;
        loadTriples$(dest, urls) ;
    }

    /** Load into default graph */
    public static void loadDefaultGraph(DatasetGraphTDB dsg, InputStream input, boolean showProgress, boolean collectStats) {
        BulkStreamRDF dest = destinationDefaultGraph(dsg, showProgress, collectStats) ;
        loadTriples$(dest, input) ;
    }

    private static BulkStreamRDF destinationDefaultGraph(DatasetGraphTDB dsg, boolean showProgress, boolean collectStats) {
        return destinationGraph(dsg, null, showProgress, collectStats) ;
    }

    /** Load into named graph */
    public static void loadNamedGraph(DatasetGraphTDB dsg, Node graphNode, List<String> urls, boolean showProgress, boolean collectStats) {
        BulkStreamRDF dest = destinationNamedGraph(dsg, graphNode, showProgress, collectStats) ;
        loadTriples$(dest, urls) ;
    }

    /** Load into named graph */
    public static void loadNamedGraph(DatasetGraphTDB dsg, Node graphNode, InputStream input, boolean showProgress, boolean collectStats) {
        BulkStreamRDF dest = destinationNamedGraph(dsg, graphNode, showProgress, collectStats) ;
        loadTriples$(dest, input) ;
    }

    /** Load into a dataset */
    public static void loadDataset(DatasetGraphTDB dsg, List<String> urls, boolean showProgress, boolean collectStats) {
        BulkStreamRDF dest = destinationDataset(dsg, showProgress, collectStats) ;
        loadQuads$(dest, urls) ;
    }

    /** Load into a dataset */
    public static void loadDataset(DatasetGraphTDB dsg, InputStream input, boolean showProgress, boolean collectStats) {
        BulkStreamRDF dest = destinationDataset(dsg, showProgress, collectStats) ;
        loadQuads$(dest, input) ;
    }

    /** Load into a graph */
    private static void loadTriples$(BulkStreamRDF dest, List<String> urls) {
        dest.startBulk() ;
        for ( String url : urls ) {
            loadLogger.info("Load: " + url + " -- " + DateTimeUtils.nowAsString()) ;
            Lang lang = RDFLanguages.filenameToLang(url, Lang.NTRIPLES) ;
            RDFDataMgr.parse(dest, url, lang) ;
        }
        dest.finishBulk() ;
    }

    /** Load into a graph */
    private static void loadTriples$(BulkStreamRDF dest, InputStream input) {
        loadLogger.info("Load: from input stream -- " + DateTimeUtils.nowAsString()) ;
        dest.startBulk() ;
        RDFDataMgr.parse(dest, input, Lang.NTRIPLES) ;
        dest.finishBulk() ;
    }

    /** Load quads into a dataset */
    private static void loadQuads$(BulkStreamRDF dest, List<String> urls) {
        dest.startBulk() ;
        for ( String url : urls ) {
            loadLogger.info("Load: " + url + " -- " + DateTimeUtils.nowAsString()) ;
            Lang lang = RDFLanguages.filenameToLang(url, Lang.NQUADS) ;
            RDFDataMgr.parse(dest, url, lang) ;
        }
        dest.finishBulk() ;
    }

    /** Load quads into a dataset */
    private static void loadQuads$(BulkStreamRDF dest, InputStream input) {
        loadLogger.info("Load: from input stream -- " + DateTimeUtils.nowAsString()) ;
        dest.startBulk() ;
        RDFDataMgr.parse(dest, input, RDFLanguages.NQUADS) ;
        dest.finishBulk() ;
    }

    private static BulkStreamRDF destinationNamedGraph(DatasetGraphTDB dsg, Node graphName, boolean showProgress, boolean collectStats) {
        if ( graphName == null )
            return destinationDefaultGraph(dsg, showProgress, collectStats) ;
        return destinationGraph(dsg, graphName, showProgress, collectStats) ;
    }

    public static LoadMonitor createLoadMonitor(DatasetGraphTDB dsg, String itemName, boolean showProgress) {
        if ( showProgress )
            return new LoadMonitor(dsg, loadLogger, itemName, DataTickPoint, IndexTickPoint) ;
        else
            return new LoadMonitor(dsg, null, itemName, DataTickPoint, IndexTickPoint) ;
    }

    private static BulkStreamRDF destinationDataset(DatasetGraphTDB dsg, boolean showProgress, boolean collectStats) {
        return new DestinationDSG(dsg, showProgress, collectStats) ;
    }

    private static BulkStreamRDF destinationGraph(DatasetGraphTDB dsg, Node graphNode, boolean showProgress, boolean collectStats) {
        return new DestinationGraph(dsg, graphNode, showProgress, collectStats) ;
    }

    // Load triples and quads into a dataset.
    private static final class DestinationDSG implements BulkStreamRDF {
        final private DatasetGraphTDB      dsg ;
        final private boolean              startedEmpty ;
        final private LoadMonitor          monitor1 ;
        final private LoadMonitor          monitor2 ;
        final private LoaderNodeTupleTable loaderTriples ;
        final private LoaderNodeTupleTable loaderQuads ;
        final private boolean              showProgress ;
        private long                       count = 0 ;
        private StatsCollector             stats = null ;
        private final boolean collectStats ;

        DestinationDSG(final DatasetGraphTDB dsg, boolean showProgress, boolean collectStats) {
            this.dsg = dsg ;
            startedEmpty = dsg.isEmpty() ;
            monitor1 = createLoadMonitor(dsg, "triples", showProgress) ;
            monitor2 = createLoadMonitor(dsg, "quads", showProgress) ;

            loaderTriples = new LoaderNodeTupleTable(dsg.getTripleTable().getNodeTupleTable(), "triples", monitor1) ;
            loaderQuads = new LoaderNodeTupleTable(dsg.getQuadTable().getNodeTupleTable(), "quads", monitor2) ;
            this.showProgress = showProgress ;
            this.collectStats = collectStats ;
        }

        @Override
        final public void startBulk() {
            loaderTriples.loadStart() ;
            loaderQuads.loadStart() ;

            loaderTriples.loadDataStart() ;
            loaderQuads.loadDataStart() ;
            if ( collectStats )
                this.stats = new StatsCollector() ;
        }

        @Override
        public void triple(Triple triple) {
            Node s = triple.getSubject() ;
            Node p = triple.getPredicate() ;
            Node o = triple.getObject() ;
            process(Quad.tripleInQuad, s, p, o) ;
        }

        @Override
        public void quad(Quad quad) {
            Node s = quad.getSubject() ;
            Node p = quad.getPredicate() ;
            Node o = quad.getObject() ;
            Node g = null ;
            // Union graph?!
            if ( !quad.isTriple() && !quad.isDefaultGraph() )
                g = quad.getGraph() ;
            process(g, s, p, o) ;
        }

        private void process(Node g, Node s, Node p, Node o) {
            if ( g == null )
                loaderTriples.load(s, p, o) ;
            else
                loaderQuads.load(g, s, p, o) ;
            count++ ;
            if ( stats != null )
                stats.record(g, s, p, o) ;
        }

        @Override
        public void finishBulk() {
            loaderTriples.loadDataFinish() ;
            loaderQuads.loadDataFinish() ;

            loaderTriples.loadIndexStart() ;
            loaderQuads.loadIndexStart() ;

            loaderTriples.loadIndexFinish() ;
            loaderQuads.loadIndexFinish() ;

            loaderTriples.loadFinish() ;
            loaderQuads.loadFinish() ;
            if ( !dsg.getLocation().isMem() && startedEmpty && stats != null ) {
                String filename = dsg.getLocation().getPath(Names.optStats) ;
                Stats.write(filename, stats.results()) ;
            }
            forceSync(dsg) ;
        }

        @Override
        public void start() {}

        @Override
        public void base(String base) {}

        @Override
        public void prefix(String prefix, String iri) {
            dsg.getPrefixes().getPrefixMapping().setNsPrefix(prefix, iri) ;
        }

        @Override
        public void finish() {}
    }

    // Load triples into a specific NodeTupleTable
    private static final class DestinationGraph implements BulkStreamRDF {
        final private DatasetGraphTDB      dsg ;
        final private Node                 graphName ;
        final private LoadMonitor          monitor ;
        final private LoaderNodeTupleTable loaderTriples ;
        final private boolean              startedEmpty ;
        private long                       count = 0 ;
        private StatsCollector             stats = null ;
        private final boolean              collectStats ;

        // Graph node is null for default graph.
        DestinationGraph(final DatasetGraphTDB dsg, Node graphNode, boolean showProgress, boolean collectStats) {
            this.dsg = dsg ;
            this.graphName = graphNode ;
            this.collectStats = collectStats ;
            // Choose NodeTupleTable.
            NodeTupleTable nodeTupleTable ;
            if ( graphNode == null || Quad.isDefaultGraph(graphNode) )
                nodeTupleTable = dsg.getTripleTable().getNodeTupleTable() ;
            else {
                NodeTupleTable ntt = dsg.getQuadTable().getNodeTupleTable() ;
                nodeTupleTable = new NodeTupleTableView(ntt, graphName) ;
            }
            startedEmpty = dsg.isEmpty() ;
            monitor = createLoadMonitor(dsg, "triples", showProgress) ;
            loaderTriples = new LoaderNodeTupleTable(nodeTupleTable, "triples", monitor) ;
        }

        @Override
        final public void startBulk() {
            loaderTriples.loadStart() ;
            loaderTriples.loadDataStart() ;
            if ( collectStats )
                this.stats = new StatsCollector() ;
        }

        @Override
        final public void triple(Triple triple) {
            Node s = triple.getSubject() ;
            Node p = triple.getPredicate() ;
            Node o = triple.getObject() ;

            loaderTriples.load(s, p, o) ;
            if ( stats != null )
                stats.record(null, s, p, o) ;
            count++ ;
        }

        @Override
        final public void finishBulk() {
            loaderTriples.loadDataFinish() ;
            loaderTriples.loadIndexStart() ;
            loaderTriples.loadIndexFinish() ;
            loaderTriples.loadFinish() ;

            if ( !dsg.getLocation().isMem() && startedEmpty && stats != null ) {
                String filename = dsg.getLocation().getPath(Names.optStats) ;
                Stats.write(filename, stats.results()) ;
            }
            forceSync(dsg) ;
        }

        @Override
        public void start() {}

        @Override
        public void quad(Quad quad) {
            throw new TDBException("Quad encountered while loading a single graph") ;
        }

        @Override
        public void base(String base) {}

        @Override
        public void prefix(String prefix, String iri) {
            if ( graphName != null && graphName.isBlank() ) {
                loadLogger.warn("Prefixes for blank node graphs not stored") ;
                return ;
            }

            PrefixMapping pmap = (graphName == null)
                                                    ? dsg.getPrefixes().getPrefixMapping()
                                                    : dsg.getPrefixes().getPrefixMapping(graphName.getURI()) ;
            pmap.setNsPrefix(prefix, iri) ;
        }

        @Override
        public void finish() {}
    }

    static void forceSync(DatasetGraphTDB dsg) {
        // Force sync - we have been bypassing DSG tables.
        // THIS DOES NOT WORK IF modules check for SYNC necessity.
        dsg.getTripleTable().getNodeTupleTable().getNodeTable().sync() ;
        dsg.getQuadTable().getNodeTupleTable().getNodeTable().sync() ;
        dsg.getQuadTable().getNodeTupleTable().getNodeTable().sync() ;
        dsg.getPrefixes().getNodeTupleTable().getNodeTable().sync() ;
        // This is not enough -- modules check whether sync needed.
        dsg.sync() ;

    }
}
