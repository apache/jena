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

package org.apache.jena.fuseki ;

import org.apache.jena.riot.RIOT ;
import org.apache.jena.riot.stream.LocatorFTP ;
import org.apache.jena.riot.stream.LocatorHTTP ;
import org.apache.jena.riot.stream.StreamManager ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.lib.Metadata ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.MappingRegistry ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

public class Fuseki {
    // General fixed constants.
    // See also FusekiServer for the naming on the filesystem
    
    /** Path to ??? */
    static public String    PATH                         = "org.apache.jena.fuseki" ;

    /** a unique IRI for the Fuseki namespace */
    static public String    FusekiIRI                    = "http://jena.apache.org/Fuseki" ;

    /**
     * a unique IRI including the symbol notation for which properties should be
     * appended
     */
    static public String    FusekiSymbolIRI              = "http://jena.apache.org/fuseki#" ;

    /** Default location of the pages for the Fuseki UI  */
    static public String    PagesStatic                  = "pages" ;
    
    /** Dummy base URi string for parsing SPARQL Query and Update requests */
    static public final String BaseParserSPARQL          = "http://server/unset-base/" ;
    
    /** Dummy base URi string for parsing SPARQL Query and Update requests */
    static public final String BaseUpload                = "http://server/unset-base/" ;

    /**
     * A relative resources path to the location of 
     * <code>fuseki-properties.xml</code> file.
     */
    static private String   metadataLocation             = "org/apache/jena/fuseki/fuseki-properties.xml" ;

    /**
     * Object which holds metadata specified within
     * {@link Fuseki#metadataLocation}
     */
    static private Metadata metadata                     = initMetadata() ;

    private static Metadata initMetadata() {
        Metadata m = new Metadata() ;
        // m.addMetadata(metadataDevLocation) ;
        m.addMetadata(metadataLocation) ;
        return m ;
    }

    /** The name of the Fuseki server. Set to the string <code>Fuseki</code> by default. */
    static public final String        NAME              = "Fuseki" ;

    /** Version of this Fuseki instance */
    static public final String        VERSION           = metadata.get(PATH + ".version", "development") ;

    /** Date when Fuseki was built */
    static public final String        BUILD_DATE        = metadata.get(PATH + ".build.datetime", "unknown") ;

    /** An identifier for the HTTP Fuseki server instance */
    static public final String        serverHttpName    = NAME + " (" + VERSION + ")" ;

    /** Actual log file for operations */
    public static final String        requestLogName    = PATH + ".Fuseki" ;

    /** Instance of log for operations */
    public static final Logger        requestLog        = LoggerFactory.getLogger(requestLogName) ;

    /** Admin log file for operations */
    public static final String        adminLogName      = PATH + ".Admin" ;

    /** Instance of log for operations */
    public static final Logger        adminLog          = LoggerFactory.getLogger(adminLogName) ;

    /** Admin log file for operations */
    public static final String        builderLogName    = PATH + ".Builder" ;

    /** Instance of log for operations */
    public static final Logger        builderLog        = LoggerFactory.getLogger(builderLogName) ;

    /** Validation log file for operations */
    public static final String        validationLogName = PATH + ".Validate" ;

    /** Instance of log for validation */
    public static final Logger        validationLog     = LoggerFactory.getLogger(adminLogName) ;

    /** Actual log file for general server messages. */
    public static final String        serverLogName     = PATH + ".Server" ;

    /** Instance of log for general server messages */
    public static final Logger        serverLog         = LoggerFactory.getLogger(serverLogName) ;

    /** Logger used for the servletContent.log operations (if settable -- depends on environment) */
    public static final String        servletRequestLogName     = PATH + ".Request" ;

    /** Actual log file for config server messages. */
    public static final String        configLogName     = PATH + ".Config" ;

    /** Instance of log for config server message s */
    public static final Logger        configLog         = LoggerFactory.getLogger(configLogName) ;

    /** Instance of log for config server message s */
    public static boolean             verboseLogging    = false ;

    /**
     * An instance of management for stream opening, including redirecting
     * through a location mapper whereby a name (e.g. URL) is redirected to
     * another name (e.g. local file).
     * */
    public static final StreamManager webStreamManager ;
    static {
        webStreamManager = new StreamManager() ;
        // Only know how to handle http URLs
        webStreamManager.addLocator(new LocatorHTTP()) ;
        webStreamManager.addLocator(new LocatorFTP()) ;
    }

    /** Default (and development) root of the Fuseki installation for fixed files. */ 
    public static String DFT_FUSEKI_HOME = "." ;
    /** Default (and development) root of the varying files in this deployment. */ 
    public static String DFT_FUSEKI_BASE = "." ;
    
    private static boolean            initialized       = false ;

    /**
     * Initialize an instance of the Fuseki server stack.
     */
    public synchronized static void init() {
        if ( initialized )
            return ;
        initialized = true ;
        ARQ.init() ;
        SystemInfo sysInfo = new SystemInfo(FusekiIRI, PATH, VERSION, BUILD_DATE) ;
        SystemARQ.registerSubSystem(sysInfo) ;
        RIOT.init() ;
        TDB.init() ;
        MappingRegistry.addPrefixMapping("fuseki", FusekiSymbolIRI) ;

        TDB.setOptimizerWarningFlag(false) ;
        // Don't set TDB batch commits.
        // This can be slower, but it less memory hungry and more predictable.
        TransactionManager.QueueBatchSize = 0 ;
    }
    
    /**
     * Get server global {@link com.hp.hpl.jena.sparql.util.Context}.
     * 
     * @return {@link com.hp.hpl.jena.query.ARQ#getContext()}
     */
    public static Context getContext() {
        return ARQ.getContext() ;
    }

    // Force a call to init.
    static {
        init() ;
    }
}
