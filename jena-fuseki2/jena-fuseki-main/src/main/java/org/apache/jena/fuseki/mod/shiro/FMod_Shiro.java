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

package org.apache.jena.fuseki.mod.shiro;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import jakarta.servlet.Filter;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.rdf.model.Model;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fuseki Module for Apache Shiro.
 * <p>
 * Looks for an argument {@code --shiro=file}, and
 * in environment variable {@code FUSEKI_SHIRO}
 * (including via system proprties).
 */
public class FMod_Shiro implements FusekiModule {

//    @Override
//    public void start() {
//        Fuseki.serverLog.info("FMod Shiro");
//    }
    //
//  @Override
//  public int level() {
//      return FusekiApp.levelFModShiro;
//  }

    @Override
    public String name() {
        return "FMod Shiro";
    }

    public static FMod_Shiro create() {
        return new FMod_Shiro();
    }

    // Assumes the whole system is "Shiro".
    // No setup?

    public static final Logger shiroConfigLog = LoggerFactory.getLogger(Fuseki.PATH + ".Shiro");

    private static List<String> defaultIniFileLocations = List.of("file:shiro.ini", "file:/etc/fuseki/shiro.ini");
    private static List<String> iniFileLocations = null;

    private static ArgDecl argShiroIni = new ArgDecl(true, "shiro", "shiro-ini");

    // Module state (for reload).
    private String shiroFile = null;

    public FMod_Shiro() {
        this(null);
    }

    public FMod_Shiro(String shiroFile) {
        this.shiroFile = shiroFile;
    }

    // ---- If used from the command line
    @Override
    public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        fusekiCmd.add(argShiroIni);
    }

    @Override
    public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        if ( fusekiCmd.contains(argShiroIni) ) {
            shiroFile = fusekiCmd.getValue(argShiroIni);
            Path path = Path.of(shiroFile);
            IOX.checkReadableFile(path, CmdException::new);
        }
    }

    // The filter is added in prepare().
    // This allows other Fuseki modules, such as FMod_Admin, to setup shiro.ini.
    // FMod_Admin unpacks a default one to FUSEKI_BASE/shiro.ini (usually "run/shiro.ini")

     /**
      * Determine the Shiro configuration file.
      * This applies whether command line arguments used for programmatic setup.
      */
    @Override
    public void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) {
        if ( shiroFile == null ) {
            // Environment variable:  FUSEKI_SHIRO
            shiroFile = Lib.getenv(FusekiServerCtl.envFusekiShiro);
        }

        if ( shiroFile == null ) {
            return;
        }

        if ( shiroFile != null ) {
            IOX.checkReadableFile(shiroFile, FusekiConfigException::new);
            Filter filter = new FusekiShiroFilter(shiroFile);
            // This is a "before" filter.
            serverBuilder.addFilter("/*", filter);
        }

        // Clear.
        shiroFile = null;
    }

    /**
     * FusekiShiroFilter, includes Shiro initialization. Fuseki is a
     * not a webapp so it needs to trigger off servlet initialization.
     */
    private static class FusekiShiroFilter extends ShiroFilter {

        private final String shiroInitializationFile;

        FusekiShiroFilter(String filename) {
            shiroInitializationFile = IRILib.filenameToIRI(filename);
        }

        @Override
        public void init() throws Exception {
            // Intercept Shiro initialization.
            List<String> locations = List.of();
            if ( shiroInitializationFile != null ) {
                locations = List.of(shiroInitializationFile);
            }
            FusekiShiroLib.shiroEnvironment(getServletContext(), locations);
            super.init();
        }
    }

    @Override
    public void serverBeforeStarting(FusekiServer server) {
        // Shiro requires a session handler.
        // This needs the Jetty server to have been created.
        org.eclipse.jetty.server.Server jettyServer = server.getJettyServer();
        try {
            ServletContextHandler servletContextHandler = (ServletContextHandler)(jettyServer.getHandler());
            if ( servletContextHandler.getSessionHandler() == null ) {
                SessionHandler sessionsHandler = new SessionHandler();
                servletContextHandler.setHandler(sessionsHandler);
            }
        } catch (RuntimeException ex) {
            shiroConfigLog.error("Failed to set a session manager - server aborted");
            throw ex;
        }
    }

    // Later:
    // Reload shirio.ini file and reset.
//    // Currently, no actual - the server admin area does not move during the run of a server.
//    /** {@inheritDoc} */
//    @Override
//    public void serverReload(FusekiServer server) { }
}
