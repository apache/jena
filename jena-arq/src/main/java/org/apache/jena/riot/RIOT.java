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

package org.apache.jena.riot ;

import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.SysJSONLD11;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.mgt.SystemInfo ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sys.JenaSystem ;

public class RIOT {
    // Initialization statics must be first in the class to avoid
    // problems with recursive initialization.
    private static volatile boolean initialized = false ;
    private static Object           initLock    = new Object() ;

    /** IRI for RIOT */
    public static final String riotIRI = "http://jena.apache.org/#riot" ;

    /** The product name */
    public static final String NAME    = "RIOT" ;

    // Unsafe to touch ARQ in class initialization
    // See init(). these are set in register()
    // public static final String VERSION = NAME+"/"+ARQ.VERSION ;
    // public static final String BUILD_DATE = ARQ.BUILD_DATE ;

    public static String       VERSION ;
    public static String       BUILD_DATE ;

    /** The root package name for RIOT */
    public static final String PATH    = "org.apache.jena.riot" ;

    /** Control of multiline literals */
    public static final Symbol multilineLiterals = Symbol.create("riot.multiline_literals") ;

    /** The system-wide context */
    public static Context getContext() {
        return ARQ.getContext();
    }

    public static void init() {
        if ( initialized )
            return ;
        synchronized (initLock) {
            if ( initialized ) {
                JenaSystem.logLifecycle("RIOT.init - skip") ;
                return ;
            }
            initialized = true ;
            JenaSystem.logLifecycle("RIOT.init - start") ;
            RDFLanguages.init() ;
            RDFParserRegistry.init() ;
            RDFWriterRegistry.init() ;
            ResultSetLang.init();

            MappingRegistry.addPrefixMapping("ttl", TURTLE_SYMBOL_BASE) ;
            MappingRegistry.addPrefixMapping("trig", TURTLE_SYMBOL_BASE) ;

            IO_Jena.wireIntoJena() ;

            // JSON-LD 11 while still an optional feature.
            SysJSONLD11.init();

            // Don't register JMX info with ARQ as it may not be initialized
            // itself and we can get into a circularity.
            // This is done in ARQ.init at the proper moment.
            JenaSystem.logLifecycle("RIOT.init - finish") ;
        }
    }

    private static boolean registered = false ;

    public static void register() {
        if ( registered )
            return ;
        registered = true ;

        VERSION = getVersion() ;
        BUILD_DATE = getBuildDate() ;

        SystemInfo sysInfo2 = new SystemInfo(RIOT.riotIRI, RIOT.PATH, VERSION, BUILD_DATE) ;
        SystemARQ.registerSubSystem(sysInfo2) ;
    }

    public static String getVersion() {
        return ARQ.VERSION ;
    }

    public static String getBuildDate() {
        return ARQ.BUILD_DATE ;
    }

    // ---- Symbols

    private static String TURTLE_SYMBOL_BASE = "http://jena.apache.org/riot/turtle#";

    /**
     * Printing style. One of "RDF11" or RDF10". Controls {@literal @prefix} vs PREFIX.
     * Values causing SPARQL-style keyword output are "sparql","keyword" and "rdf11".
     */
    public static final Symbol symTurtleDirectiveStyle = SystemARQ.allocSymbol(TURTLE_SYMBOL_BASE, "directiveStyle");

    /**
     * Printing style. Whether to output "BASE"/"@base" (according to
     * {@link #symTurtleDirectiveStyle} or not. BASE is normally written if there is
     * a base URI passed to the writer or, for a streaming writer, if
     * {@link StreamRDF#base} is called. If this context setting is set true, then do
     * not output BASE even when given.
     */
    public static final Symbol symTurtleOmitBase = SystemARQ.allocSymbol(TURTLE_SYMBOL_BASE, "omitBase");
}
