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

package org.apache.jena.fuseki.mod.shiro;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.runner.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.rdf.model.Model;
import org.apache.shiro.lang.io.ResourceUtils;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.SessionHandler;
import org.slf4j.Logger;

/**
 * Fuseki Module for Apache Shiro.
 * <p>
 * Looks for an argument {@code --shiro=file}, and
 * in environment variable {@code FUSEKI_SHIRO}
 * (including via system proprties).
 */
public class FMod_Shiro implements FusekiModule {

    @Override
    public String name() {
        return "FMod Shiro";
    }

    public static FMod_Shiro create() {
        return new FMod_Shiro();
    }

    public static final Logger shiroConfigLog = FusekiShiro.shiroLog;

    private static List<String> iniFileLocations = null;

    private static ArgDecl argShiroIni = new ArgDecl(true, "shiro", "shiro-ini");

    /** Name of the servlet attribute in the builder for in the admin area. */
    public static String adminShiroFile = "org.apache.jena.fuseki:AdminShiroFile";

    /** Shiro configuration file given a a command line argument. */
    private String argShiroFile = null;

    /**
     * Control for whether to have Jetty/Jakarta sessions.
     * But the HTTP sessions don't get cleared up and keep hold of RAM.
     * See <a href="https://github.com/apache/jena/issues/4033">GH-4033</a>.
     * <p>
     * If NO_SESSIONS is {@code true}, then disable Shiro session creation
     * on every request, and don't create a Jetty sever session manager.
     * <p>
     * It the future, it is likely that this flag is dropped
     * and the code support only "no sessions" for {@code FMod_Shiro}.
     */
    private static final boolean NO_SESSIONS = true;

    public FMod_Shiro() {}

    // ---- If used from the command line
    @Override
    public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        fusekiCmd.add(argShiroIni, "--shiro", "Set the shiro.ini file");
        argShiroFile = null;
    }

    @Override
    public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        if ( fusekiCmd.contains(argShiroIni) ) {
            argShiroFile = fusekiCmd.getValue(argShiroIni);
            Path path = Path.of(argShiroFile);
            IOX.checkReadableFile(path, msg-> {
                FmtLog.error(shiroConfigLog, msg);
               return new CmdException();
            });
        }
    }

    // ---- Build cycle

    // The filter is added in prepare().
    // This allows other Fuseki modules, such as FMod_Admin, to setup shiro.ini.
    // FMod_Admin unpacks a default one to FUSEKI_BASE/shiro.ini (usually "run/shiro.ini")

    /**
     * Find the Shiro configuration. In order:
     * <ol>
     * <li>From a command line argument.</li>
     * <li>From the environment variable "FUSEKI_SHIRO".</li>
     * <li>From 'shiro.ini' in the admin run time area.
     * </ol>
     * */
    private String decideShiroConfiguration(FusekiServer.Builder serverBuilder) {
        String shiroConfig = this.argShiroFile;
        if ( shiroConfig != null )
            return shiroConfig;

        // Environment variable: FUSEKI_SHIRO
        shiroConfig = Lib.getenv(FusekiServerCtl.envFusekiShiro);
        if ( shiroConfig != null )
            return shiroConfig;

        // FMod_admin.
        Object obj = serverBuilder.getServletAttribute(adminShiroFile);
        if ( obj == null )
            return null;
        if ( obj instanceof Path path )
            return path.toString();
        else {
            FmtLog.error(shiroConfigLog,"Servlet attribute '%s' is not a Path: got: %s", adminShiroFile, obj);
        }
        return null;
    }

    /**
     * Determine the Shiro configuration file.
     * This applies whether command line arguments used for programmatic setup.
     */
    @Override
    public void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) {
        String shiroConfig = decideShiroConfiguration(serverBuilder);
        if ( shiroConfig == null )
            return;

        // Shiro-style.
        String shiroResourceName = FusekiShiro.withResourcePrefix(shiroConfig);
        if ( ! ResourceUtils.resourceExists(shiroResourceName) )
            throw new FusekiConfigException("Shiro resource does not exist");

        Filter filter = new FusekiShiroFilter(shiroResourceName, NO_SESSIONS);
        // This is a "before" filter.
        serverBuilder.addFilter("/*", filter);
        serverBuilder.setServletAttribute(Fuseki.attrShiroResource, shiroResourceName);
        // Clear.
        this.argShiroFile = null;
        FmtLog.info(shiroConfigLog, "Shiro configuration: %s", shiroResourceName);
    }

    // When NO_SESSIONS becomes permanent, serverBeforeStarting(), addHttpSessionListener() can be removed.
    @Override
    public void serverBeforeStarting(FusekiServer server) {
        if ( NO_SESSIONS )
            return;

        try {
            String x =(String)server.getServletContext().getAttribute(Fuseki.attrShiroResource);
            if ( x == null )
                return ;
        } catch (ClassCastException ex) {
            FmtLog.warn(shiroConfigLog, "Unexpected Shiro configuration: %s", server.getServletContext().getAttribute(Fuseki.attrShiroResource));
        }

        if ( ! NO_SESSIONS ) {
            // If shiro requires a session handler.
            // This needs the Jetty server to have been created.
            // Allocate a Jakarta SessionHandler
            try {
                org.eclipse.jetty.server.Server jettyServer = server.getJettyServer();

                ServletContextHandler servletContextHandler = (ServletContextHandler)(jettyServer.getHandler());
                if ( servletContextHandler.getSessionHandler() == null ) {
                    SessionHandler sessionHandler = new SessionHandler();
                    if ( false )
                        addHttpSessionListener(sessionHandler);
                    servletContextHandler.setHandler(sessionHandler);
                }
            } catch (RuntimeException ex) {
                shiroConfigLog.error("Failed to set a session manager - server aborted");
                throw ex;
            }
        }
    }

    // Later:
    // Reload shiro.ini file and reset. See FusekiShiroEnvironmentLoader.
//    /** {@inheritDoc} */
//    @Override
//    public void serverReload(FusekiServer server) { }


    private static void addHttpSessionListener( SessionHandler sessionHandler) {
        // Development
        HttpSessionListener httpSessionListener = new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                Log.info(Fuseki.serverLog, "Jakarta session created");
            }
            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                Log.info(Fuseki.serverLog, "Jakarta session destroyed");
            }
        };
        sessionHandler.addEventListener(httpSessionListener);
    }
}
