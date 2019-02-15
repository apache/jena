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

package org.apache.jena.tdb2 ;

import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.lang.ReaderRIOTRDFXML;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;
import org.apache.jena.sparql.engine.main.StageBuilder ;
import org.apache.jena.sparql.engine.main.StageGenerator ;
import org.apache.jena.sparql.mgt.SystemInfo ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.MappingRegistry ;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.apache.jena.tdb2.modify.UpdateEngineTDB;
import org.apache.jena.tdb2.solver.QueryEngineTDB;
import org.apache.jena.tdb2.solver.StageGeneratorDirectTDB;
import org.apache.jena.tdb2.sys.EnvTDB;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.apache.jena.util.Metadata;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class TDB2 {
    
    private TDB2() {}

    /** IRI for TDB */
    public static final String  tdbIRI                           = "http://jena.apache.org/#tdb2" ;

    /** Root of TDB-defined parameter names */
    public static final String  tdbParamNS                       = SystemTDB.symbolNamespace;

    /** Prefix for TDB-defined parameter names */
    public static final String  tdbSymbolPrefix                  = SystemTDB.tdbSymbolPrefix;
    
    // Internal logging
    private static final Logger log                              = LoggerFactory.getLogger(TDB2.class) ;

    /** Logger for loading information */
    public static final String  logLoaderName                    = "org.apache.jena.tdb2.loader" ;
    /** Logger for loading information */
    public static final Logger  logLoader                        = LoggerFactory.getLogger(logLoaderName) ;

    /** Logger for general information */
    public static final String  logInfoName                      = "org.apache.jena.info" ;
    /** Logger for general information */
    public static final Logger  logInfo                          = LoggerFactory.getLogger(logInfoName) ;

    // /** Logger for execution information */
    // public static final String logExecName = "org.apache.jena.tdb.exec";
    // /** Logger for execution information */
    // public static final Logger logExec = LoggerFactory.getLogger(logExecName);

    /** Used by the assembler */
    public final static String  namespace                        = "http://jena.apache.org/2016/tdb#" ;

    // Union default graph symbols for context setting.
    // Used in QueryEngineTDB.
    
    // This is not the name of the union graph which is 
    // "urn:x-arq:UnionGraph".
    // See Quad.unionGraph = "urn:x-arq:UnionGraph"
    
    /** TDB1 namespace version of the context symbol for union default graph */
    public static final Symbol  symUnionDefaultGraph1            = SystemTDB.allocSymbol(SystemTDB.symbolNamespace1, "unionDefaultGraph") ;
    /** TDB2 namespace version of the context symbol for union default graph */
    public static final Symbol  symUnionDefaultGraph2            = SystemTDB.allocSymbol(SystemTDB.symbolNamespace2, "unionDefaultGraph") ;
    
    /**
     * Symbol to use the union of named graphs as the default graph of a query.
     * This must use the TDB1 compatible namespace.
     */
    public static final Symbol  symUnionDefaultGraph             = symUnionDefaultGraph2;

    public static Context getContext() {
        return ARQ.getContext() ;
    }

    /**
     * Release any and all system resources held by TDB.
     * All release datasets or graphs held by client code are no longer valid. 
     */
    public static void closedown() {
        TDBInternal.reset() ;
    }

    private static final Object initLock = new Object() ;
    private static volatile boolean initialized = false ;
    static { JenaSystem.init(); }
    
    /**
     * TDB System initialization - normally, this is not explicitly called
     * because all routes to use TDB will cause initialization to occur.
     * However, calling it repeatedly is safe and low cost.
     */
    public static void init() {
        if ( initialized )
            return ;
        synchronized(initLock) {
            if ( initialized ) {
                if ( JenaSystem.DEBUG_INIT )
                    System.err.println("TDB2.init - return") ;
                return ;
            }
            initialized = true ;
            if ( JenaSystem.DEBUG_INIT )
                System.err.println("TDB2.init - start") ;

            SystemTDB.init() ;
            ARQ.init() ;
            ReaderRIOTRDFXML.RiotUniformCompatibility = true ;
            EnvTDB.processGlobalSystemProperties() ;

            MappingRegistry.addPrefixMapping(SystemTDB.tdbSymbolPrefix, SystemTDB.symbolNamespace) ;
            AssemblerUtils.init() ;
            VocabTDB2.init() ;
            QueryEngineTDB.register() ;
            UpdateEngineTDB.register() ;
            MappingRegistry.addPrefixMapping(TDB2.tdbSymbolPrefix, TDB2.tdbParamNS) ;

            wireIntoExecution() ;
            if ( JenaSystem.DEBUG_INIT )
                System.err.println("TDB.init - finish") ;
        }
    }

    private static void wireIntoExecution() {
        // Globally change the stage generator to intercept BGP on TDB
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

    static private String      metadataLocation = "org/apache/jena/tdb2/tdb2-properties.xml" ;
    static private Metadata    metadata         = new Metadata(metadataLocation) ;

    /** The root package name for TDB */
    public static final String PATH             = "org.apache.jena.tdb2" ;

    // The names known to ModVersion : "NAME", "VERSION", "BUILD_DATE"

    public static final String NAME             = "TDB" ;

    /** The full name of the current TDB version */
    public static final String VERSION          = metadata.get(PATH + ".version", "DEV") ;

    /** The date and time at which this release was built */
    public static final String BUILD_DATE       = metadata.get(PATH + ".build.datetime", "unset") ;

    // Final initialization (in case any statics in this file are important).
    static {
        initialization2() ;
    }

    private static void initialization2() {
        // Set management information.
        SystemInfo systemInfo = new SystemInfo(TDB2.tdbIRI, TDB2.PATH, TDB2.VERSION, TDB2.BUILD_DATE) ;
        SystemARQ.registerSubSystem(systemInfo) ;
    }

}
