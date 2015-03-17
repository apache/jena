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

package org.apache.jena.fuseki;

import org.apache.jena.fuseki.server.SPARQLServer ;
import org.apache.jena.riot.RIOT ;
import org.apache.jena.riot.system.stream.LocatorFTP ;
import org.apache.jena.riot.system.stream.LocatorHTTP ;
import org.apache.jena.riot.system.stream.StreamManager ;
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

/**
 * <p>The main class enabling us to:</p> 
 * <ol>
 * <li>create instances of a Fuseki server e.g.
 * the ARQ, RIOT and TDB server stack</li>
 * <li>get server global {@link com.hp.hpl.jena.sparql.util.Context} e.g. 
 * named values used to pass implementation-specific parameters across 
 * general interfaces.</li>
 * <li>get the {@link org.apache.jena.fuseki.server.SPARQLServer} instance.</li>
 * <li>set the {@link org.apache.jena.fuseki.server.SPARQLServer} instance.</li>
 *
 */
public class Fuseki
{
    /** Path to ???*/
    static public String PATH = "org.apache.jena.fuseki" ;
    
    /** a unique IRI for the Fuseki namespace*/
    static public String FusekiIRI = "http://jena.apache.org/Fuseki" ;
    
    /** Fuseki home environment, usually set to $FUSEKI_HOME */
    static public String FusekiHomeEnv = "FUSEKI_HOME" ;
    
    /** a unique IRI including the symbol notation for which properties should be appended */
    static public String FusekiSymbolIRI = "http://jena.apache.org/fuseki#" ;
    
    /** ??? */
    static public String PagesStatic = "pages" ;
    
    /** 
     * TEMPORARY - this enables POST of triples to the dataset URI causing a graph to be created.
     * POSTing to /dataset?graph=uri is preferred 
     */
    static public boolean graphStoreProtocolPostCreate = false ;
    
    /** an relative path to the location of <code>fuseki-properties.xml</code> file */
    static private String metadataLocation = "org/apache/jena/fuseki/fuseki-properties.xml" ;
    
    /** Object which holds metadata specified within {@link Fuseki#metadataLocation} */
    static private Metadata metadata = initMetadata() ;
    
    private static Metadata initMetadata()
    {
        Metadata m = new Metadata() ;
        //m.addMetadata(metadataDevLocation) ;
        m.addMetadata(metadataLocation) ;
        return m ;
    }
    
    /** The name of the Fuseki server. Set to the string <code>Fuseki</code> by default.*/
    static public final String NAME             = "Fuseki" ;
    
    /** Version of this Fuseki instance */
    static public final String VERSION          = metadata.get(PATH+".version", "development");
    
    /** Date when Fuseki was built */
    static public final String BUILD_DATE       = metadata.get(PATH+".build.datetime", "unknown") ; // call Date if unavailable.
    
    /** An identifier for the HTTP Fuseki server instance*/
    static public final String serverHttpName   = NAME+" ("+VERSION+")" ;    
    
    /** Actual log file for operations */
    public static final String requestLogName   = PATH+".Fuseki" ;
    
    /** Instance of log for operations */
    public static final Logger requestLog       = LoggerFactory.getLogger(requestLogName) ;
    
    /** Actual log file for general server messages.*/
    public static final String serverLogName    = PATH+".Server" ;
    
    /** Instance of log for general server messages */
    public static final Logger serverLog        = LoggerFactory.getLogger(serverLogName) ;
    
    /** Actual log file for config server messages. */
    public static final String configLogName    = PATH+".Config" ;
    
    /** Instance of log for config server message s*/
    public static final Logger configLog        = LoggerFactory.getLogger(configLogName) ;
    
    /** Instance of log for config server message s*/
    public static boolean verboseLogging        = false ;
    
    /** An instance of management for stream opening, including redirecting through a 
     * location mapper whereby a name (e.g. URL) is redirected to another name (e.g. local file).
     * */
    public static final StreamManager webStreamManager ;
    static {
        webStreamManager = new StreamManager() ;
        // Only know how to handle http and ftp URLs - nothing local.
        webStreamManager.addLocator(new LocatorHTTP()) ;
        webStreamManager.addLocator(new LocatorFTP()) ;
    }
    
    private static boolean initialized = false ;
    
    /**
     * Initialize an instance of the Fuseki server stack.
     */
    public synchronized static void init()
    {
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
     * @return {@link com.hp.hpl.jena.query.ARQ#getContext()}
     */
    public static Context getContext()
    {
        return ARQ.getContext() ;
    }
    
    // Temporary ...
    private static SPARQLServer server ;
    
    /** set/specify the {@link org.apache.jena.fuseki.server.SPARQLServer} instance.*/
    public static void setServer(SPARQLServer _server)      { server = _server ; }
    
    /** get the {@link org.apache.jena.fuseki.server.SPARQLServer} instance. */
    public static SPARQLServer getServer()                  { return server ; }

    // Force a call to init.
    static { init() ; }
}
