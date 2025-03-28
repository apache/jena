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

package org.apache.jena.fuseki.mod.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.fuseki.validation.*;
import org.apache.jena.rdf.model.Model;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.Logger;

public class FMod_UI implements FusekiModule {

    private static FusekiModule singleton = new FMod_UI();
    public static FusekiModule create() {
        return new FMod_UI();
    }

    public FMod_UI() {}

    private static Logger LOG = Fuseki.configLog;

//    // After FMod_admin
//    @Override
//    public int level() {
//        return FusekiApp.levelFModUI;
//    }

    private static ArgDecl argUIFiles = new ArgDecl(true, "ui");

    /** Java resource name used to find the UI files. */
    private static String resourceNameUI = "webapp";
    /** Directory name of the root of UI files */
    private static String directoryNameUI = "webapp";

    // UI resources location.
    private String uiAppLocation = null;

    @Override
    public String name() {
        return "FMod UI";
    }

    // ---- If used from the command line
    @Override
    public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        fusekiCmd.add(argUIFiles);
    }

    @Override
    public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        if ( fusekiCmd.contains(argUIFiles) ) {
            uiAppLocation = fusekiCmd.getValue(argUIFiles);
            IOX.checkReadableDirectory(uiAppLocation, CmdException::new);
        }
    }

    @Override
    public void prepare(FusekiServer.Builder builder, Set<String> datasetNames, Model configModel) {
        if ( builder.staticFileBase() != null ) {
            FmtLog.warn(LOG, "Static content location has already been set: %s", builder.staticFileBase());
            return;
        }

        FusekiServerCtl serverCtl = (FusekiServerCtl)builder.getServletAttribute(Fuseki.attrFusekiServerCtl);
        if ( serverCtl == null ) {
            LOG.warn("No server control");
            return;
        }

        if ( uiAppLocation == null ) {
            uiAppLocation = findFusekiApp(serverCtl);
            if ( uiAppLocation == null ) {
                LOG.warn("No static content location");
                return;
            }
        } else {
            FmtLog.info(LOG, "UI file area = %s", uiAppLocation);
        }

        builder.staticFileBase(uiAppLocation)       // Set the UI files area.
               .addServlet("/$/validate/query",     new QueryValidator())
               .addServlet("/$/validate/update",    new UpdateValidator())
               .addServlet("/$/validate/iri",       new IRIValidator())
               .addServlet("/$/validate/langtag",   new LangTagValidator())
               .addServlet("/$/validate/data",      new DataValidator())
               .enableStats(true);
        // LOG.info("Fuseki UI loaded");
    }

    // Currently, fixed location during the run of a server.
    /** {@inheritDoc} */
    @Override
    public void serverReload(FusekiServer server) { }

    /**
     * Locate the UI files.
     * <ol>
     * <li>Command line name of a directory</li>
     * <li>{@code FusekiServerCtl.getFusekibase()/webapp}</li>
     * <li>Classpath java resource {@code webapp}</li>
     * <ol>
     */
    private String findFusekiApp(FusekiServerCtl serverCtl) {
        // 1:: Command line setting.
        if ( uiAppLocation != null )
            return uiAppLocation;

        // 2::FusekiServerCtl.getFusekibase()/webapp
        if ( serverCtl != null ) {
            Path fusekiBase =  serverCtl.getFusekiBase();
            String x = fromPath(fusekiBase, directoryNameUI);
            if ( x != null ) {
                LOG.info("Fuseki UI - path resource: "+x);
                return x;
            }
        }

        // 3:: From a jar.
        // Format  jar:file:///.../jena-fuseki-ui-VERSION.jar!/webapp/"
        String r = fromClasspath(resourceNameUI);
        if ( r != null ) {
            // Simplify name.
            String displayName = loggingName(r);
            FmtLog.info(LOG, "UI Base = %s", displayName);
            return r;
        }
        // Bad!
        return null;
    }

    // Look for "$resourceName" on the classpath.
    private static String fromClasspath(String resourceName) {
        // Jetty 12.0.15  => warning "Leaked mount"
        // Logger : "org.eclipse.jetty.util.resource.ResourceFactory"
        //ResourceFactory resourceFactory = ResourceFactory.root();

        ResourceFactory resourceFactory = ResourceFactory.closeable();
        Resource resource = resourceFactory.newClassLoaderResource(resourceName);
        if ( resource != null )
            return resource.getURI().toString();
        return null;
    }

    // Look for "$path/$resourceName"
    private static String fromPath(Path path, String resourceName) {
        if ( path != null ) {
            Path path2 = path.resolve(resourceName);
            if ( Files.exists(path2) ) {
                IOX.checkReadableDirectory(path2, FusekiConfigException::new);
                return path2.toAbsolutePath().toString();
            }
        }
        return null;
    }

    private static Pattern regex = Pattern.compile("([^/]*)!");

    private String loggingName(String r) {
        Matcher matcher = regex.matcher(r);
        if ( ! matcher.find() )
            return r;
        return matcher.group(1);
    }
}
