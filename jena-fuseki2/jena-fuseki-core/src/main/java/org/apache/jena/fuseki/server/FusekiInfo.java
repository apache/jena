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

import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.function.Function;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.slf4j.Logger;

public class FusekiInfo {
    
    public static void info(FusekiInitialConfig serverConfig, DataAccessPointRegistry registry) {
        if ( ! serverConfig.verbose )
            return;
        if ( serverConfig.quiet )
            return;

        Logger log = Fuseki.serverLog;
        FmtLog.info(log,  "Apache Jena Fuseki");
        
        // Dataset -> Endpoints
        Map<String, List<String>> z = description(registry);
        
//        if ( serverConfig.empty ) {
//            FmtLog.info(log, "No SPARQL datasets services"); 
//        } else {
//            if ( serverConfig.datasetPath == null && serverConfig.serverConfig == null )
//                log.error("No dataset path nor server configuration file");
//        }
        
        if ( serverConfig.datasetPath != null ) {
            if ( z.size() != 1 )
                log.error("Expected only one dataset");
            List<String> endpoints = z.get(serverConfig.datasetPath); 
            FmtLog.info(log,  "Dataset Type = %s", serverConfig.datasetDescription);
            FmtLog.info(log,  "Path = %s; Services = %s", serverConfig.datasetPath, endpoints);
        }
        if ( serverConfig.fusekiServerConfigFile != null ) {
            // May be many datasets and services.
            FmtLog.info(log,  "Configuration file %s", serverConfig.fusekiServerConfigFile);
            z.forEach((name, endpoints)->{
                FmtLog.info(log,  "Path = %s; Services = %s", name, endpoints);
            });
        }
        FusekiInfo.logDetails(log);
    }
    
    private static Map<String, List<String>> description(DataAccessPointRegistry reg) {
        Map<String, List<String>> desc = new LinkedHashMap<>();
        reg.forEach((ds,dap)->{
            List<String> endpoints = new ArrayList<>();
            desc.put(ds, endpoints);
            DataService dSrv = dap.getDataService();
            dSrv.getOperations().forEach((op)->{
                dSrv.getEndpoints(op).forEach(ep-> {
                    String x = ep.getName();
                    if ( x.isEmpty() )
                        x = "quads";
                    endpoints.add(x);   
                });
            });
        });
        return desc;
    }
    
    public static void logDetails(Logger log) {
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;
        Function<Long, String> f = FusekiInfo::strNum2;
        //FmtLog.info(log, "Apache Jena %s", Jena.VERSION);
        FmtLog.info(log, "  Fuseki: %s", Fuseki.VERSION);
        FmtLog.info(log, "  Java:   %s", System.getProperty("java.version"));
        //FmtLog.info(log, "Memory: max=%s  total=%s  used=%s  free=%s", f.apply(maxMem), f.apply(totalMem), f.apply(usedMem), f.apply(freeMem));
        FmtLog.info(log, "  Memory: max=%s", f.apply(maxMem));
        FmtLog.info(log, "  OS:     %s %s %s", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
    }
    
    public static void logDetailsVerbose(Logger log) {
        logDetails(log);
        logOne(log, "java.vendor");
        logOne(log, "java.home");
        logOne(log, "java.runtime.version");
        logOne(log, "java.runtime.name");
        //logOne(log, "java.endorsed.dirs");
        logOne(log, "user.language");
        logOne(log, "user.timezone");
        logOne(log, "user.country");
        logOne(log, "user.dir");
        //logOne(log, "file.encoding");
    }
    
    private static void logOne(Logger log, String property) {
        FmtLog.info(log, "    %-20s = %s", property, System.getProperty(property));
    }

    /** Create a human-friendly string for a number based on Kilo/Mega/Giga/Tera (powers of 2) */
    public static String strNumMixed(long x) {
        // https://en.wikipedia.org/wiki/Kibibyte
        if ( x < 1024 )
            return Long.toString(x);
        if ( x < 1024*1024 )
            return String.format("%.1fK", x/1024.0);
        if ( x < 1024*1024*1024 )
            return String.format("%.1fM", x/(1024.0*1024));
        if ( x < 1024L*1024*1024*1024 )
            return String.format("%.1fG", x/(1024.0*1024*1024));
        return String.format("%.1fT", x/(1024.0*1024*1024*1024));
    }
    

    /** Create a human-friendly string for a number based on Kilo/Mega/Giga/Tera (powers of 10) */
    public static String strNum10(long x) {
        if ( x < 1_000 )
            return Long.toString(x);
        if ( x < 1_000_000 )
            return String.format("%.1fK", x/1000.0);
        if ( x < 1_000_000_000 )
            return String.format("%.1fM", x/(1000.0*1000));
        if ( x < 1_000_000_000_000L )
            return String.format("%.1fG", x/(1000.0*1000*1000));
        return String.format("%.1fT", x/(1000.0*1000*1000*1000));
    }
    
    /** Create a human-friendly string for a number based on Kibi/Mebi/Gibi/Tebi (powers of 2) */
    public static String strNum2(long x) {
        // https://en.wikipedia.org/wiki/Kibibyte
        if ( x < 1024 )
            return Long.toString(x);
        if ( x < 1024*1024 )
            return String.format("%.1f KiB", x/1024.0);
        if ( x < 1024*1024*1024 )
            return String.format("%.1f MiB", x/(1024.0*1024));
        if ( x < 1024L*1024*1024*1024 )
            return String.format("%.1f GiB", x/(1024.0*1024*1024));
        return String.format("%.1fTiB", x/(1024.0*1024*1024*1024));
    }
}
