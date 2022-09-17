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

package org.apache.jena.fuseki.server;

import static java.lang.String.format;

import java.util.*;

import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.slf4j.Logger;

/** Functions that log information about the core Fuseki engine. */
public class FusekiCoreInfo {

    /** Details of the code version. */
    public static void logCode(Logger log) {
        String version = Fuseki.VERSION;
        String buildDate = Fuseki.BUILD_DATE;
        String serverName = Fuseki.NAME;

        if ( version != null && version.equals("${project.version}") )
            version = null;
        if ( buildDate != null && buildDate.equals("${build.time.xsd}") )
            buildDate = DateTimeUtils.nowAsXSDDateTimeString();
        if ( version != null ) {
            if ( Fuseki.developmentMode && buildDate != null )
                FmtLog.info(log, "%s %s %s", serverName, version, buildDate);
            else
                FmtLog.info(log, "%s %s", serverName, version);
        }
    }

    /** Log details - this function is about command line details */
    // Shared between FusekiMain and Fuseki Webapp (currently).
    public static void logServerCmdSetup(Logger log, boolean verbose, DataAccessPointRegistry dapRegistry,
                                         String datasetPath, String datasetDescription, String serverConfigFile, String staticFiles) {
        if ( datasetPath != null )
            FmtLog.info(log, "Database: %s", datasetDescription);
        if ( serverConfigFile != null )
            FmtLog.info(log, "Configuration file: %s", serverConfigFile);

        FusekiCoreInfo.logDataAccessPointRegistry(log, dapRegistry, verbose);

        if ( staticFiles != null )
            FmtLog.info(log, "Static files: %s", staticFiles);

        if ( verbose ) {
            PlatformInfo.logDetailsSystem(log);
            PlatformInfo.logDetailsJVM(log);
        }
        else
            PlatformInfo.logDetailsSystemPlain(log);
    }

    /** Log a {@link DataAccessPointRegistry} */
    public static void logDataAccessPointRegistry(Logger log, DataAccessPointRegistry dapRegistry, boolean longForm) {
        if ( longForm )
            infoPathsOperations(log, dapRegistry);
        else
            infoPaths(log, dapRegistry);
   }

   private static void infoPaths(Logger log, DataAccessPointRegistry reg) {
       reg.keys().stream().sorted().forEach(datasetPath -> {
           //DataAccessPoint dap = reg.get(datasetPath);
           log.info("Path = "+datasetPath);
       });
   }

   private static void infoPathsOperations(Logger log, DataAccessPointRegistry reg) {
       reg.keys().stream().sorted().forEach(datasetPath -> {
           log.info("Path = "+datasetPath);
           DataAccessPoint dap = reg.get(datasetPath);
           logDataAccessPoint(log, dap);
       });
    }

    /** Log a {@link DataAccessPoint} in detail */
    public static void logDataAccessPoint(Logger log, DataAccessPoint dap) {
        operations(dap.getDataService()).forEach(operation->{
            StringBuilder sb = new StringBuilder();
            sb.append(format("  Operation = %-6s", operation.getName()));
            sb.append("  Endpoints = ");
            StringJoiner sj = new StringJoiner(", ", "[ ", " ]");
            dap.getDataService().getEndpoints(operation).stream()
                .map(ep->"\""+ep.getName()+"\"")
                .sorted()
                .forEach(sj::add);
            sb.append(sj.toString());
            if ( false ) {
                // Don't print access! Development only.
                sb.append("  Access = ");
                StringJoiner sj2 = new StringJoiner(", ", "[ ", " ]");
                dap.getDataService().getEndpoints(operation).stream()
                .map(Endpoint::getAuthPolicy)
                .map(auth-> auth==null?"*":auth.toString())
                .sorted()
                .forEach(sj2::add);
                sb.append(sj2.toString());
            }
            FmtLog.info(log,sb.toString());
        });
    }

    // Canonical order.
    static List<Operation> stdOperations = Arrays.asList(
        Operation.Query,
        Operation.Update,
        Operation.GSP_RW,
        Operation.GSP_R
        );

    /** Operations, in a canonical order for logging */
    private static List<Operation> operations(DataService dataService) {
        Collection<Operation> registered = dataService.getOperations();
        List<Operation> nice  = new ArrayList<>();
        for ( Operation op : stdOperations ) {
            if ( registered.contains(op) )
                nice.add(op);
        }
        List<Operation> others = new ArrayList<>();
        for ( Operation op : registered ) {
            if ( ! nice.contains(op) )
                others.add(op);
        }

        Comparator<Operation> order = (Operation o1, Operation o2)->
                o1.getName().compareTo(o2.getName());
        others.stream().sorted(order).forEach(nice::add);
        return nice;
    }

}
