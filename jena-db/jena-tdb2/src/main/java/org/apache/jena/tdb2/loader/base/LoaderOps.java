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

package org.apache.jena.tdb2.loader.base;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.jena.dboe.index.Index;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.data.TransBinaryDataFile;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableTRDF;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.slf4j.Logger;

/**
 * A collection of useful functions for the TDB2 loader subsystem. This class is not
 * considered to be in the public API.
 */
public class LoaderOps {
    /** Get the node storage for a {@Link NodeTable} */
    public static TransBinaryDataFile ntDataFile(NodeTable nt) {
        NodeTableTRDF ntt = (NodeTableTRDF)(nt.baseNodeTable());
        BinaryDataFile bdf = ntt.getData();
        TransBinaryDataFile tbdf = (TransBinaryDataFile)bdf;
        return tbdf;
    }

    /** Get the BPlusTree index for a {@Link NodeTable} */
    public static BPlusTree ntBPTree(NodeTable nt) {
        NodeTableTRDF ntt = (NodeTableTRDF)(nt.baseNodeTable());
        Index idx = ntt.getIndex();
        return (BPlusTree)idx;
    }

    /** Get the BPlusTree index for a {@Link TupleIndex} */
    public static BPlusTree idxBTree(TupleIndex idx) {
        TupleIndexRecord idxr = (TupleIndexRecord)idx;
        RangeIndex rIndex = idxr.getRangeIndex();
        BPlusTree bpt = (BPlusTree)rIndex;
        return bpt;
    }

    /** Wrap an existing {@link StreamRDF} to add output of progress messages. */
    private static StreamRDF streamWithProgressMonitor(StreamRDF dest, String label, MonitorOutput output, int dataTickPoint, int dataSuperTick) {
        ProgressMonitor monitor = ProgressMonitorOutput.create(output, label, dataTickPoint, dataSuperTick);
        return new ProgressStreamRDF(dest, monitor);
    }

    /** Calculate a label for a progress monitor. */
    public static String label(String fileName) {
        String basename = FileOps.splitDirFile(fileName).get(1);
        return basename;
    }

    /**
     * Parse one file, with an optional progress output.
     */
    public static void inputFile(StreamRDF dest, String source, MonitorOutput output, int dataTickPoint, int dataSuperTick) {
        Objects.requireNonNull(dest);
        ProgressMonitor monitor = null;
        if ( output != null )
            monitor = ProgressMonitorFactory.progressMonitor(label(source), output, dataTickPoint, dataSuperTick);
        inputFile(dest, source, monitor);
    }

    /**
     * Parse one file, with an optional progress monitor. Pass null to {@code monitor} for
     * "no output".
     */
    public static void inputFile(StreamRDF sink, String source, ProgressMonitor monitor) {
        if ( monitor != null ) {
            sink = new ProgressStreamRDF(sink, monitor);
            //monitor.start();
        }
        sink.start();
        RDFDataMgr.parse(sink, source);
        sink.finish();
        if ( monitor != null ) {
            //monitor.finish();
            //monitor.finishMessage("Data");
        }
    }

    /** Copy a stream to several indexes (sequential version) */
    public static void copyIndex(Iterator<Tuple<NodeId>> srcIter, TupleIndex[] destIndexes, ProgressMonitor monitor) {
        long counter = 0;
        for (; srcIter.hasNext() ; ) {
            counter++;
            Tuple<NodeId> tuple = srcIter.next();
            monitor.tick();
            for ( TupleIndex destIdx : destIndexes ) {
                if ( destIdx != null )
                    destIdx.add(tuple);
            }
        }
    }

    /**
     * Convert to quads: triples from the default graph of parsing become quads using the
     * {@code graphName}. If {@code graphName} is null, return the {@code stream}
     * argument.
     */
    public static StreamRDF toNamedGraph(StreamRDF stream, Node graphName) {
        Objects.requireNonNull(stream);
        if ( graphName == null )
            return stream;
        // Rename the default graph triples. Data quads are dropped.
        return new StreamRDFWrapper(stream) {
            @Override
            public void triple(Triple triple) {
                super.quad(Quad.create(graphName, triple));
            }

            // Only triples can be read into a graph - drop quads from the parser.
            @Override
            public void quad(Quad quad) {}
        };
    }

    private static Logger LOG = TDB2.logLoader;

    /** Output to the nothing. */
    public static MonitorOutput nullOutput() {
        return (x, y) -> {};
    }

    /** Output to the loader logger. */
    public static MonitorOutput outputToLog() {
        return LoaderOps.outputToLog(LOG);
    }

    /** {@link MonitorOutput} to a logger. */
    public static MonitorOutput outputToLog(Logger logger) {
        Objects.requireNonNull(logger);
        return (fmt, args) -> {
            if ( logger.isInfoEnabled() )
                FmtLog.info(logger, fmt, args);
        };
    }

    /** {@link MonitorOutput} to a PrintStream. */
    public static MonitorOutput outputTo(PrintStream output) {
        Objects.requireNonNull(output);
        return (fmt, args) -> {
            if ( fmt.endsWith("\n") || fmt.endsWith("\r") )
                output.print(String.format(fmt, args));
            else
                output.println(String.format(fmt, args));
        };
    }
}
