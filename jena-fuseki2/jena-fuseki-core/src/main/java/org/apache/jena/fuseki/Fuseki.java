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

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.system.stream.LocatorFTP;
import org.apache.jena.riot.system.stream.LocatorHTTP;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.mgt.SystemInfo;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.transaction.TransactionManager;
import org.apache.jena.util.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fuseki {
    // General fixed constants.
    // See also FusekiServer for the naming on the filesystem

    /** Path as package name */
    static public String    PATH                         = "org.apache.jena.fuseki";

    /** a unique IRI for the Fuseki namespace */
    static public String    FusekiIRI                    = "http://jena.apache.org/Fuseki";

    /**
     * a unique IRI including the symbol notation for which properties should be
     * appended
     */
    static public String    FusekiSymbolIRI              = "http://jena.apache.org/fuseki#";

    /** Default location of the pages for the Fuseki UI  */
    static public String    PagesStatic                  = "pages";

    /** Dummy base URi string for parsing SPARQL Query and Update requests */
    static public final String BaseParserSPARQL          = "http://server/unset-base/";

    /** Dummy base URi string for parsing SPARQL Query and Update requests */
    static public final String BaseUpload                = "http://server/unset-base/";

    /** Add CORS header */
    static public final boolean CORS_ENABLED = false;

    /**
     * A relative resources path to the location of
     * <code>fuseki-properties.xml</code> file.
     */
    static private String   metadataLocation             = "org/apache/jena/fuseki/fuseki-properties.xml";

    /**
     * Object which holds metadata specified within
     * {@link Fuseki#metadataLocation}
     */
    static private Metadata metadata                     = initMetadata();

    private static Metadata initMetadata() {
        Metadata m = new Metadata();
        // m.addMetadata(metadataDevLocation);
        m.addMetadata(metadataLocation);
        return m;
    }

    /** The name of the Fuseki server.*/
    static public final String        NAME              = "Apache Jena Fuseki";

    /** Version of this Fuseki instance */
    static public final String        VERSION           = metadata.get(PATH + ".version", "development");

    /** Date when Fuseki was built */
    static public final String        BUILD_DATE        = metadata.get(PATH + ".build.datetime", "unknown");

    /** Supporting Graph Store Protocol direct naming.
     * <p>
     *  A GSP "direct name" is a request, not using ?default or ?graph=, that names the graph
     *  by the request URL so it is of the form {@code http://server/dataset/graphname...}.
     *  There are two cases: looking like a service {@code http://server/dataset/service} and
     *  a longer URL that can't be a service {@code http://server/dataset/segment/segment/...}.
     *  <p>
     *  GSP "direct name" is usually off.  It is a rare feature and because of hard wiring to the URL
     *  quite sensitive to request route.
     *  <p>
     *  The following places use this switch:
     *  <ul>
     *  <li>{@code FusekiFilter} for the "clearly not a service" case
     *  <li>{@code ServiceRouterServlet}, end of dispatch (after checking for http://server/dataset/service)
     *  <li>{@code SPARQL_GSP.determineTarget} This is all-purpose code - should not get there because of other checks.
     *  </ul>
     *  <p>
     * <b>Note</b><br/>
     * GSP Direct Naming was implemented to provide two implementations for the SPARQL 1.1 implementation report.
     */
    static public final boolean       GSP_DIRECT_NAMING = false;

    /** Are we in development mode?  That means a SNAPSHOT, or no VERSION
     * because maven has not filtered the fuseki-properties.xml file.
     */
    public static boolean   developmentMode;
    static {
        // See ServletBase.setCommonheaders
        // If it look like a SNAPSHOT, or it's not set, we are in development mode.
        developmentMode = ( VERSION == null || VERSION.equals("development") || VERSION.contains("SNAPSHOT") );
    }

    public static boolean   outputJettyServerHeader     = developmentMode;
    public static boolean   outputFusekiServerHeader    = developmentMode;

    /** An identifier for the HTTP Fuseki server instance */
    static public final String  serverHttpName          = NAME + " (" + VERSION + ")";

    /** Logger name for operations */
    public static final String        actionLogName     = PATH + ".Fuseki";

    /** Instance of log for operations */
    public static final Logger        actionLog         = LoggerFactory.getLogger(actionLogName);

    /** Logger name for standard webserver log file request log */
    public static final String        requestLogName    = PATH + ".Request";

    // See HttpAction.finishRequest.
    // Normally OFF
    /** Instance of a log for requests: format is NCSA. */
    public static final Logger        requestLog        = LoggerFactory.getLogger(requestLogName);

    /** Admin log file for operations. */
    public static final String        adminLogName      = PATH + ".Admin";

    /** Instance of log for operations. */
    public static final Logger        adminLog          = LoggerFactory.getLogger(adminLogName);

    /** Admin log file for operations. */
    public static final String        builderLogName    = PATH + ".Builder";

    /** Instance of log for operations. */
    public static final Logger        builderLog        = LoggerFactory.getLogger(builderLogName);

    /** Validation log file for operations. */
    public static final String        validationLogName = PATH + ".Validate";

    /** Instance of log for validation. */
    public static final Logger        validationLog     = LoggerFactory.getLogger(adminLogName);

    /** Actual log file for general server messages. */
    public static final String        serverLogName     = PATH + ".Server";

    /** Instance of log for general server messages. */
    public static final Logger        serverLog         = LoggerFactory.getLogger(serverLogName);

    /** Logger used for the servletContent.log operations (if settable -- depends on environment) */
    public static final String        servletRequestLogName     = PATH + ".Servlet";

    /** Actual log file for config server messages. */
    public static final String        configLogName     = PATH + ".Config";

    /** Instance of log for config server messages. */
    public static final Logger        configLog         = LoggerFactory.getLogger(configLogName);

    /** Instance of log for config server messages.
     * This is the global default used to set attribute
     * in each server created.
     */
    public static boolean             verboseLogging    = false;

    // Servlet context attribute names,

    public static final String attrVerbose                 = "org.apache.jena.fuseki:verbose";
    public static final String attrNameRegistry            = "org.apache.jena.fuseki:DataAccessPointRegistry";
    public static final String attrOperationRegistry       = "org.apache.jena.fuseki:OperationRegistry";
    public static final String attrAuthorizationService    = "org.apache.jena.fuseki:AuthorizationService";

    public static void setVerbose(ServletContext cxt, boolean verbose) {
        cxt.setAttribute(attrVerbose, Boolean.valueOf(verbose));
    }

    public static boolean getVerbose(ServletContext cxt) {
        return (Boolean)cxt.getAttribute(attrVerbose);
    }

    /**
     * An instance of management for stream opening, including redirecting
     * through a location mapper whereby a name (e.g. URL) is redirected to
     * another name (e.g. local file).
     * */
    public static final StreamManager webStreamManager;
    static {
        webStreamManager = new StreamManager();
        // Only know how to handle http URLs
        webStreamManager.addLocator(new LocatorHTTP());
        webStreamManager.addLocator(new LocatorFTP());
    }

    // HTTP response header inserted to aid tracking. 
    public static String FusekiRequestIdHeader = "Fuseki-Request-Id";

    private static boolean            initialized       = false;

    // Server start time and uptime.
    private static final long startMillis = System.currentTimeMillis();
    // Hide server locale
    private static final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("00:00"));
    static { cal.setTimeInMillis(startMillis); }  // Exactly the same start point!

    private static final String startDateTime = DateTimeUtils.calendarToXSDDateTimeString(cal);

    /** Return the number of milliseconds since the server started */
    public static long serverUptimeMillis() {
        return System.currentTimeMillis() - startMillis;
    }

    /** Server uptime in seconds */
    public static long serverUptimeSeconds() {
        long x = System.currentTimeMillis() - startMillis;
        return TimeUnit.MILLISECONDS.toSeconds(x);
    }

    /** XSD DateTime for when the server started */
    public static String serverStartedAt() {
        return startDateTime;
    }

    /**
     * Initialize an instance of the Fuseki server stack.
     * This is not done via Jena's initialization mechanism
     * but done explicitly to give more control.
     * Touching this class causes this to happen
     * (see static block at the end of this class).
     */
    public synchronized static void init() {
        if ( initialized )
            return;
        initialized = true;
        JenaSystem.init();
        SystemInfo sysInfo = new SystemInfo(FusekiIRI, PATH, VERSION, BUILD_DATE);
        SystemARQ.registerSubSystem(sysInfo);
        MappingRegistry.addPrefixMapping("fuseki", FusekiSymbolIRI);

        TDB.setOptimizerWarningFlag(false);
        // Don't set TDB batch commits.
        // This can be slower, but it less memory hungry and more predictable.
        TransactionManager.QueueBatchSize = 0;
    }

    /**
     * Get server global {@link org.apache.jena.sparql.util.Context}.
     *
     * @return {@link org.apache.jena.query.ARQ#getContext()}
     */
    public static Context getContext() {
        return ARQ.getContext();
    }

    // Force a call to init.
    static {
        init();
    }

    /** Retrun a free port */
    public static int choosePort() {
        return FusekiNetLib.choosePort();
    }

    /**
     * Test whether a URL identifies a Fuseki server. This operation can not guarantee to
     * detect a Fuseki server - for example, it may be behind a reverse proxy that masks
     * the signature.
     */
    public static boolean isFuseki(String datasetURL) {
        HttpOptions request = new HttpOptions(datasetURL);
        HttpClient httpClient = HttpOp.getDefaultHttpClient();
        if ( httpClient == null )
            httpClient = HttpClients.createSystem();
        return isFuseki(request, httpClient, null);
    }

    /**
     * Test whether a {@link RDFConnectionRemote} connects to a Fuseki server. This
     * operation can not guaranteed to detech a Fuseki server - for example, it may be
     * behind a reverse proxy that masks the signature.
     */
    public static boolean isFuseki(RDFConnectionRemote connection) {
        HttpOptions request = new HttpOptions(connection.getDestination());
        HttpClient httpClient = connection.getHttpClient();
        if ( httpClient == null )
            httpClient = HttpClients.createSystem();
        HttpContext httpContext = connection.getHttpContext();
        return isFuseki(request, httpClient, httpContext);
    }

    private static boolean isFuseki(HttpOptions request, HttpClient httpClient, HttpContext httpContext) {
        try {
            HttpResponse response = httpClient.execute(request);
            // Fuseki does not send "Server" in release mode.
            // (best practice).
            // We can do is try for the "Fuseki-Request-Id"
            String reqId = safeGetHeader(response, FusekiRequestIdHeader);
            if ( reqId != null )
                return true;

            // If returning "Server"
            String serverIdent = safeGetHeader(response, "Server");
            if ( serverIdent != null ) {
                FmtLog.debug(ARQ.getHttpRequestLogger(), "Server: %s", serverIdent);
                boolean isFuseki = serverIdent.startsWith("Apache Jena Fuseki");
                if ( !isFuseki )
                    isFuseki = serverIdent.toLowerCase().contains("fuseki");
                return isFuseki;
            }
            return false;
        } catch (IOException ex) {
            throw new HttpException("Failed to check for a Fuseki server", ex);
        }
    }

    static String safeGetHeader(HttpResponse response, String header) {
        Header h = response.getFirstHeader(header);
        if ( h == null )
            return null;
        return h.getValue();
    }
}
