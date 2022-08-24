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

package org.apache.jena.tdb ;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.ontology.OntModel ;
import org.apache.jena.ontology.impl.OntModelImpl ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.reasoner.InfGraph ;
import org.apache.jena.riot.lang.ReaderRIOTRDFXML;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.main.StageBuilder ;
import org.apache.jena.sparql.engine.main.StageGenerator ;
import org.apache.jena.sparql.mgt.SystemInfo ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.MappingRegistry ;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb.assembler.AssemblerTDB ;
import org.apache.jena.tdb.modify.UpdateEngineTDB ;
import org.apache.jena.tdb.setup.DatasetBuilderStd ;
import org.apache.jena.tdb.solver.QueryEngineTDB ;
import org.apache.jena.tdb.solver.StageGeneratorDirectTDB ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.sys.EnvTDB ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.jena.tdb.sys.TDBInternal;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;
import org.apache.jena.util.Metadata;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class TDB {

    private TDB() {}

    // Initialization statics must be first in the class to avoid
    // problems with recursive initialization.  Specifcally,
    // initLock being null because elsewhere started the initialization
    // and is calling into the TDB class.
    // The best order is:
    //    Initialization controls
    //    All calculated constants
    //    static { JenaSystem.init() ; }
    private static final Object initLock = new Object() ;
    private static volatile boolean initialized = false ;

    /** IRI for TDB */
    public static final String  tdbIRI                           = "http://jena.hpl.hp.com/#tdb" ;


    /** Root of TDB-defined parameter names */
    public static final String  tdbParamNS                       = SystemTDB.symbolNamespace;

    /** Prefix for TDB-defined parameter names */
    public static final String  tdbSymbolPrefix                  = SystemTDB.tdbSymbolPrefix;

    // Internal logging
    private static final Logger log                              = LoggerFactory.getLogger(TDB.class) ;

    /** Logger for loading information */
    public static final String  logLoaderName                    = "org.apache.jena.tdb.loader" ;
    /** Logger for loading information */
    public static final Logger  logLoader                        = LoggerFactory.getLogger(logLoaderName) ;

    /** Logger for general information */
    public static final String  logInfoName                      = "org.apache.jena.info" ;
    /** Logger for general information */
    public static final Logger  logInfo                          = LoggerFactory.getLogger(logInfoName) ;

    // /** Logger for execution information */
    // public static final String logExecName = "org.apache.jena.tdb.exec" ;
    // /** Logger for execution information */
    // public static final Logger logExec = LoggerFactory.getLogger(logExecName) ;

    public final static String  namespace                        = "http://jena.hpl.hp.com/2008/tdb#" ;

    /** Symbol to use the union of named graphs as the default graph of a query */
    public static final Symbol  symUnionDefaultGraph             = SystemTDB.allocSymbol("unionDefaultGraph") ;

    /**
     * A String enum Symbol that specifies the type of temporary storage for
     * transaction journal write blocks.
     * <p>
     * "mem" = Java heap memory (default) <br>
     * "direct" = Process heap memory <br>
     * "mapped" = Memory mapped temporary file <br>
     */
    public static final Symbol  transactionJournalWriteBlockMode = SystemTDB.allocSymbol("transactionJournalWriteBlockMode") ;

    public static final String  tdbFaqsLink                      = "See https://jena.apache.org/documentation/tdb/faqs.html for more information.";

    public static Context getContext() {
        return ARQ.getContext() ;
    }

    /**
     * Release any and all system resources held by TDB. This does NOT close or
     * release datasets or graphs held by client code.
     */
    public static void closedown() {
        TDBInternal.reset() ;
    }

    /**
     * Set the global flag that control the "No BGP optimizer" warning. Set to
     * false to silence the warning
     */
    public static void setOptimizerWarningFlag(boolean b) {
        DatasetBuilderStd.setOptimizerWarningFlag(b) ;
    }

    /** Sync a TDB-backed Model. Do nothing if not TDB-backed. */
    public static void sync(Model model) {
        if ( model instanceof OntModelImpl ) {
            OntModelImpl ontModel = (OntModelImpl)model ;
            sync(ontModel.getBaseGraph()) ;
            return ;
        }
        // This never happens (there is only one OntModel implementation)
        if ( model instanceof OntModel ) {
            OntModel ontModel = (OntModel)model ;
            sync(ontModel.getBaseModel()) ;
            return ;
        }

        sync(model.getGraph()) ;
    }

    /** Sync a TDB-backed Graph. Do nothing if not TDB-backed. */
    public static void sync(Graph graph) {
        if ( graph == null )
            return ;

        if ( graph instanceof InfGraph ) {
            InfGraph infGraph = (InfGraph)graph ;
            sync(infGraph.getRawGraph()) ;
            return ;
        }
        syncObject(graph) ;
    }

    /** Sync a TDB-backed Dataset. Do nothing if not TDB-backed. */
    public static void sync(Dataset dataset) {
        if ( dataset == null )
            return ;
        DatasetGraph ds = dataset.asDatasetGraph() ;
        sync(ds) ;
    }

    /** Sync a TDB-backed DatasetGraph. Do nothing if not TDB-backed. */
    public static void sync(DatasetGraph dataset) {
        if ( dataset == null )
            return ;

        // Should be: SystemARQ.sync(dataset) ;
        if ( dataset instanceof DatasetGraphTDB ) {
            syncObject(dataset) ;
            return ;
        }

        if ( dataset instanceof DatasetGraphTransaction ) {
            DatasetGraphTransaction dsgt = (DatasetGraphTransaction)dataset ;
            // This only sync if the dataset has not been used transactionally.
            // Can't sync transactional datasets (it's meaningless)
            dsgt.syncIfNotTransactional() ;
            return ;
        }

        // May be a general purpose dataset with TDB objects in it.
        sync(dataset.getDefaultGraph()) ;
        Iterator<Node> iter = dataset.listGraphNodes() ;
        iter = Iter.toList(iter).iterator() ; // Avoid iterator concurrency.
        for (; iter.hasNext();) {
            Node n = iter.next() ;
            Graph g = dataset.getGraph(n) ;
            sync(g) ;
        }
    }

    /**
     * Sync a TDB synchronizable object (model, graph, dataset). If force is
     * true, synchronize as much as possible (e.g. file metadata) else make a
     * reasonable attempt at synchronization but does not guarantee disk state.
     * Do nothing otherwise
     */
    private static void syncObject(Object object) {
        if ( object == null )
            return ;
        if ( object instanceof Sync )
            ((Sync)object).sync() ;
    }

    // ---- Static constants read by modVersion
    // ---- Must be after initialization.

    static private String      metadataLocation = "org/apache/jena/tdb/tdb-properties.xml" ;
    static private Metadata    metadata         = new Metadata(metadataLocation) ;
    /** The root package name for TDB */
    public static final String PATH             = "org.apache.jena.tdb" ;
    // The names known to ModVersion : "NAME", "VERSION", "BUILD_DATE"

    public static final String NAME             = "TDB1" ;
    /** The full name of the current TDB version */
    public static final String VERSION          = metadata.get(PATH + ".version", "DEV") ;
    /** The date and time at which this release was built */
    public static final String BUILD_DATE       = metadata.get(PATH + ".build.datetime", "unset") ;

    static { JenaSystem.init(); }

    /**
     * TDB System initialization - normally, this is not explicitly called
     * because Jena system wide initialization occurs automatically.
     * However, calling it repeatedly is safe and low cost.
     */
    public static void init() {
        // Called at start.
        if ( initialized ) {
            return ;
        }

        synchronized(initLock) {
            if ( initialized ) {
                JenaSystem.logLifecycle("TDB1.init - return") ;
                return ;
            }
            initialized = true ;
            JenaSystem.logLifecycle("TDB1.init - start") ;
            ReaderRIOTRDFXML.RiotUniformCompatibility = true ;
            EnvTDB.processGlobalSystemProperties() ;

            MappingRegistry.addPrefixMapping(SystemTDB.tdbSymbolPrefix,  SystemTDB.symbolNamespace) ;
            MappingRegistry.addPrefixMapping(SystemTDB.tdbSymbolPrefix1, SystemTDB.symbolNamespace) ;
            AssemblerTDB.init() ;
            QueryEngineTDB.register() ;
            UpdateEngineTDB.register() ;

            wireIntoExecution() ;
            JenaSystem.logLifecycle("TDB1.init - finish") ;
        }
    }

    private static void wireIntoExecution() {
        // Globally change the stage generator to intercept BGP on TDB
        Context cxt = ARQ.getContext() ;
        StageGenerator orig = StageBuilder.chooseStageGenerator(cxt) ;

        // Wire in the TDB stage generator which will make TDB work whether
        // or not the TDB executor is used. This means that datasets of mixed
        // graph types inside a general purpose dataset work.
        StageGenerator stageGenerator = new StageGeneratorDirectTDB(orig) ;
        StageBuilder.setGenerator(ARQ.getContext(), stageGenerator) ;
    }

    // ---- Static constants read by modVersion
    // ---- Must be after initialization.



    // The names known to ModVersion : "NAME", "VERSION", "BUILD_DATE"

    // Final initialization (in case any statics in this file are important).
    static {
        initialization2() ;
    }

    private static void initialization2() {
        // Set management information.
        SystemInfo systemInfo = new SystemInfo(TDB.tdbIRI, TDB.PATH, TDB.VERSION, TDB.BUILD_DATE) ;
        SystemARQ.registerSubSystem(systemInfo) ;
    }

}
