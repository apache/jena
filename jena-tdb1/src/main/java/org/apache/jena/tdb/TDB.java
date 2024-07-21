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

import org.apache.jena.atlas.lib.Version;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb1.sys.SystemTDB;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * @deprecated Use {@link org.apache.jena.tdb1.TDB1}
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("all")
public class TDB {

    private TDB() {}

    // Initialization statics must be first in the class to avoid
    // problems with recursive initialization.  Specifically,
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

    public static Context getContext() {
        return TDB1.getContext();
    }

    /**
     * Release any and all system resources held by TDB. This does NOT close or
     * release datasets or graphs held by client code.
     */
    public static void closedown() {
        TDB1.closedown();
    }

    /**
     * Set the global flag that control the "No BGP optimizer" warning. Set to
     * false to silence the warning
     */
    public static void setOptimizerWarningFlag(boolean b) {
        TDB1.setOptimizerWarningFlag(b);
    }

    /** Sync a TDB-backed Model. Do nothing if not TDB-backed. */
    @SuppressWarnings({"deprecated", "removal"})
    public static void sync(Model model) {
        TDB1.sync(model);
    }

    /** Sync a TDB-backed Graph. Do nothing if not TDB-backed. */
    @SuppressWarnings({"deprecated", "removal"})
    public static void sync(Graph graph) {
        TDB1.sync(graph);
    }

    /** Sync a TDB-backed Dataset. Do nothing if not TDB-backed. */
    @SuppressWarnings({"deprecated", "removal"})
    public static void sync(Dataset dataset) {
        TDB1.sync(dataset);
    }

    /** Sync a TDB-backed DatasetGraph. Do nothing if not TDB-backed. */
    @SuppressWarnings({"deprecated", "removal"})
    public static void sync(DatasetGraph dataset) {
        TDB1.sync(dataset);
    }

    /** The root package name for TDB */
    public static final String PATH             = "org.apache.jena.tdb" ;
    // The names known to ModVersion : "NAME", "VERSION", "BUILD_DATE"

    public static final String NAME             = "TDB1" ;
    /** The full name of the current TDB version */
    public static final String VERSION          = Version.versionForClass(TDB.class).orElse("<development>");

    static { JenaSystem.init(); }

    /**
     * TDB System initialization - normally, this is not explicitly called
     * because Jena system wide initialization occurs automatically.
     * However, calling it repeatedly is safe and low cost.
     */
    public static void init() {
        TDB1.init();
    }
}
