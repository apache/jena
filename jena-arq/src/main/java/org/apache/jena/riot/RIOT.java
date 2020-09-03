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
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.mgt.SystemInfo ;
import org.apache.jena.sparql.util.Context ;
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
            // Be careful with what this touches - don't touch ARQ.*
            // because that depends on Jena core and we may be
            // initializing because IO_Ctl (ie. Jena core)
            // called RIOT.init.
            RDFLanguages.init() ;
            RDFParserRegistry.init() ;
            RDFWriterRegistry.init() ;
            ResultSetLang.init();

            IO_Jena.wireIntoJena() ;

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

    /** @deprecated Use {@link #symTurtleDirectiveStyle}. */
    @Deprecated
    public static final Symbol symTurtlePrefixStyle = SystemARQ.allocSymbol(TURTLE_SYMBOL_BASE, "prefixStyle");
}
