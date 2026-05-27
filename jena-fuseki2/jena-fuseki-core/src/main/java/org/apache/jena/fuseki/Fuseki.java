/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletContext;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.lib.Version;
import org.apache.jena.fuseki.system.FusekiCore;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.system.streammgr.LocatorFTP;
import org.apache.jena.riot.system.streammgr.LocatorHTTP;
import org.apache.jena.riot.system.streammgr.StreamManager;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fuseki {
    // General fixed constants.

    /** Path as package name */
    public static final String PATH               = "org.apache.jena.fuseki";

    /** a unique IRI for the Fuseki namespace */
    public static final String FusekiIRI          = "http://jena.apache.org/Fuseki";

    /**
     * A Fuseki base IRI for {@link Symbol Symbols}
     */
    public static final String FusekiSymbolIRI    = "http://jena.apache.org/fuseki#";

    /** Dummy base URI string for parsing SPARQL Query and Update requests */
    public static final String BaseParserSPARQL   = "http://server/unset-base/";

    /** Dummy base URI string for parsing SPARQL Query and Update requests */
    public static final String BaseUpload         = "http://server/unset-base/";

    /** The name of the Fuseki server.*/
    public static final String  NAME              = "Apache Jena Fuseki";

    /** Version of this Fuseki instance */
    public static final String  VERSION           = Version.versionForClass(Fuseki.class).orElse("<development>");

    /**
     * Supporting Graph Store Protocol direct naming.
     * <p>
     *  A GSP "direct name" is a request, not using ?default or ?graph=, that names the graph
     *  by the request URL so it is of the form {@code http://server/dataset/graphname...}.
     *  There are two cases: looking like a service {@code http://server/dataset/service} and
     *  a longer URL that can't be a service {@code http://server/dataset/segment/segment/...}.
     *  <p>
     *  GSP "direct name" is not part of the standard default Fuseki configuration.
     *  It needs to enabled by configuration using {@code fuseki:operation fuseki:gsp-direct-r}
     *  or {@code  fuseki:operation fuseki:gsp-direct-rw}.
     *
     *  It conflicts with having static file and overalpping service endpoint names.
     *  Service endpoint names takes precidence.
     *  <p>
     *  The following places use this switch:
     *  <ul>
     *  <li>{@code Dispatcher}.
     *  <li>{@code GraphTarget.determineTargetGSP} This is all-purpose code - should not get there because of other checks.
     *  <li>{@code OperationRegistry} where it endbles operation registry.
     *  <li>{@code FusekiServer.Builder.applyAccessControl}
     *  </ul>
     *  <p>
     * <b>Note</b><br/>
     * GSP Direct Naming was primarily implemented to provide two implementations for the SPARQL 1.1 implementation report.
     */
    public static final boolean GSP_DIRECT_NAMING = true;

    /**
     * Path prefix reserved for admin and server operations such as /$/ping
     */
    public static final String reservedPathPrefix = "/$/";
    /**
     * Return a URL in the server function area.
     */
    public static String serverFunctionPath(String path) {
        if ( path.startsWith(reservedPathPrefix) )
            return path;
        if ( path.startsWith("/") )
            return "/$"+path;
        else
            return reservedPathPrefix+path;
    }

    /** Are we in development mode?  That means a SNAPSHOT, or no VERSION
     * because maven has not filtered the fuseki-properties.xml file.
     */
    public static boolean developmentMode;
    static {
        // See ServletBase.setCommonheaders
        // If it look like a SNAPSHOT, or it's not set, we are in development mode.
        developmentMode = ( VERSION == null || VERSION.equals("development") || VERSION.contains("SNAPSHOT") );
    }

    // @formatter:off
    public static boolean outputJettyServerHeader   = developmentMode;
    public static boolean outputFusekiServerHeader  = developmentMode;
    // @formatter:on

    /**
     * Initialize is class.
     * See also {@link FusekiCore} for Fuseki core initialization.
     */
    public static void initConsts() {}

    /** An identifier for the HTTP Fuseki server instance */
    public static final String  serverHttpName          = NAME + " (" + VERSION + ")";

    /** Logger name for operations */
    public static final String  actionLogName     = PATH + ".Fuseki";

    /** Instance of log for operations */
    public static final Logger  actionLog         = LoggerFactory.getLogger(actionLogName);

    /** Instance of log for operations : alternative variable name */
    public static final Logger  fusekiLog         = LoggerFactory.getLogger(actionLogName);

    /** Logger name for standard webserver log file request log */
    public static final String  requestLogName    = PATH + ".Request";

    // See HttpAction.finishRequest.
    // Normally OFF
    /** Instance of a log for requests: format is NCSA. */
    public static final Logger  requestLog        = LoggerFactory.getLogger(requestLogName);

    /** Admin log file for operations. */
    public static final String  adminLogName      = PATH + ".Admin";

    /** Instance of log for operations. */
    public static final Logger  adminLog          = LoggerFactory.getLogger(adminLogName);

//    // Now validation uses the action logger.
//    /** Validation log file for operations. */
//    public static final String        validationLogName = PATH + ".Validate";

    /** Instance of log for validation. */
    public static final Logger  validationLog     = actionLog;

    /** Actual log file for general server messages. */
    public static final String  serverLogName     = PATH + ".Server";

    /** Instance of log for general server messages. */
    public static final Logger  serverLog         = LoggerFactory.getLogger(serverLogName);

    /**
     * Logger used for the servletContent.log operations (if settable -- depends on environment).
     * This is both the display name of the servlet context and the logger name.
     */
    public static final String  servletRequestLogName     = PATH + ".Servlet";

    /** log for config server messages. */
    public static final String  configLogName     = PATH + ".Config";

    /** Instance of log for config server messages. */
    public static final Logger  configLog         = LoggerFactory.getLogger(configLogName);

    public static final String  backupLogName     = PATH + ".Backup";
    public static final Logger  backupLog         = LoggerFactory.getLogger(backupLogName);

    public static final String  compactLogName    = PATH + ".Compact";
    public static final Logger  compactLog        = LoggerFactory.getLogger(compactLogName);

    // There isn't an ideal status code for a cancelled query.
    // HTTP 408 "Request timeout" is not appropriate.
    // It is about connection management, not for general timeouts.
    public static int SC_QueryCancelled                 = HttpSC.SERVICE_UNAVAILABLE_503;

    // Servlet context attribute names used by the core engine.
    // Also in FusekiServerCtl for Fuseki server
    public static final String attrVerbose              = "org.apache.jena.fuseki:verbose";
    public static final String attrNameRegistry         = "org.apache.jena.fuseki:DataAccessPointRegistry";
    public static final String attrOperationRegistry    = "org.apache.jena.fuseki:OperationRegistry";
    public static final String attrMetricsProvider      = "org.apache.jena.fuseki:MetricsProvider";
    public static final String attrShiroResource        = "org.apache.jena.fuseki:ShiroResource";

    // Use by jena-fuseki-access
    public static final String attrAuthorizationService = "org.apache.jena.fuseki:AuthorizationService";

    // Servlet context attribute names used by Fuseki Server.
    // The server (so we can go from servlet context, available in request, to the server).
    public static final String attrFusekiServer         = "org.apache.jena.fuseki:Server";
    // The FusekiServerCtl object for the admin area; may be null
    public static final String attrFusekiServerCtl      = "org.apache.jena.fuseki:ServerCtl";

    // @formatter:on

    public static void setVerbose(ServletContext cxt, boolean verbose) {
        cxt.setAttribute(attrVerbose, Boolean.valueOf(verbose));
    }

    public static boolean getVerbose(ServletContext cxt) {
        Object x = cxt.getAttribute(attrVerbose);
        if ( x == null )
            return false;
        if ( x instanceof Boolean bool )
            return bool.booleanValue();
        throw new FusekiException("attrVerbose: unknown object class: "+x.getClass().getName());
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

    // Server start time and uptime.
    private static final long startMillis = System.currentTimeMillis();
    private static final String startDateTime = calculateServerStartTime(startMillis);

    /** Calculate an XSD string for the server start time, in timezone 00:00 (hide server locale) */
    private static String calculateServerStartTime(long milliseconds) {
        // Use same milliseconds as startMillis to get exactly the same point in time.
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("00:00"));
        // Exactly the same start point in timezone Z
        cal.setTimeInMillis(milliseconds);
        String x = DateTimeUtils.calendarToXSDDateTimeString(cal);
        return x;
    }

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
     * Get server global {@link org.apache.jena.sparql.util.Context}.
     *
     * @return {@link org.apache.jena.query.ARQ#getContext()}
     */
    public static Context getContext() {
        return ARQ.getContext();
    }
}
