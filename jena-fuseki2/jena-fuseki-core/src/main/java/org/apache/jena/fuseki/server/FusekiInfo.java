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

/** Information about the server */
public class FusekiInfo {

    public static void server(Logger log) {
        String version = Fuseki.VERSION;
        String buildDate = Fuseki.BUILD_DATE;
        logServer(log, Fuseki.NAME, version, buildDate);
    }

    public static void logServer(Logger log, String serverName, String version, String buildDate) {
        if ( version != null && version.equals("${project.version}") )
            version = null;
        if ( buildDate != null && buildDate.equals("${build.time.xsd}") )
            buildDate = DateTimeUtils.nowAsXSDDateTimeString();

        String fusekiName = serverName;
        //serverName = serverName +" (basic server)";

        if ( version != null ) {
            if ( Fuseki.developmentMode && buildDate != null )
                FmtLog.info(log, "%s %s %s", fusekiName, version, buildDate);
            else
                FmtLog.info(log, "%s %s", fusekiName, version);
        }
    }

    public static void logServerSetup(Logger log, boolean verbose,
                                      DataAccessPointRegistry dapRegistry,
                                      String datasetPath, String datasetDescription, String serverConfigFile, String staticFiles) {
        if ( datasetPath != null ) {
            FmtLog.info(log,  "Database: %s", datasetDescription);
        }
        if ( serverConfigFile != null )
            FmtLog.info(log,  "Configuration file: %s", serverConfigFile);

        FusekiInfo.logDataAccessPointRegistry(log, dapRegistry, verbose);

        if ( staticFiles != null )
            FmtLog.info(log,  "Static files: %s", staticFiles);

        FmtLog.info(log,"System");
        if ( verbose )
            PlatformInfo.logDetailsVerbose(log);
        else
            PlatformInfo.logDetails(log);
    }

    /** Log a {@link DataAccessPointRegistry} */
    public static void logDataAccessPointRegistry(Logger log, DataAccessPointRegistry dapRegistry, boolean verbose) {
        dapRegistry.forEach((name, dap)->{
            FmtLog.info(log,  "Path = %s", name);
            if ( verbose )
                FusekiInfo.logDataAccessPointDetails(log, dapRegistry, name);
        });
    }

    /** Log a {@link DataAccessPoint} in detail */
    public static void logDataAccessPointDetails(Logger log, DataAccessPointRegistry dapRegistry, String datasetPath) {
        DataAccessPoint dap = dapRegistry.get(datasetPath);
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

            // Don't print access!
//            sb.append("  Access = ");
//            StringJoiner sj2 = new StringJoiner(", ", "[ ", " ]");
//            dap.getDataService().getEndpoints(operation).stream()
//                .map(Endpoint::getAuthPolicy)
//                .map(auth-> auth==null?"*":auth.toString())
//                .sorted()
//                .forEach(sj2::add);
//            sb.append(sj2.toString());
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
