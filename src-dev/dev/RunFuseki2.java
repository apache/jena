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

package dev;

import java.nio.file.Paths ;

import org.apache.jena.fuseki.FusekiCmd ;
import org.apache.jena.fuseki.server.FusekiServer ;

public class RunFuseki2
{
    public static void main(String[] args) throws Exception {
        //demo() ;
        //FusekiCmd.main("--config=config.ttl") ; System.exit(0) ;
        main1() ;
    }
    
    public static void demo() {
        String DIR="DemoServer" ;
        FusekiCmd.main("--config="+name(DIR,"config.ttl"), "--pages="+name(DIR, "demo-pages")) ;
        System.exit(0) ;
    }
    
    public static String name(String DIR, String filename) {
        StringBuilder sb = new StringBuilder() ;
        if ( ! filename.startsWith("/") )
        {
            sb.append(DIR) ;
            if ( ! DIR.endsWith("/") )
                sb.append("/") ;
        }
        sb.append(filename) ;
        return sb.toString() ;
    }
    
    private static void main1() {
        FusekiServer.FUSEKI_HOME = Paths.get("").toAbsolutePath() ;
        FusekiServer.FUSEKI_BASE = Paths.get("run") ;
        FusekiServer.init() ;
        
        String tmpdir = System.getenv("TMPDIR") ;
        if ( tmpdir == null )
            tmpdir = System.getenv("TMP") ;
        if ( tmpdir == null )
            tmpdir = System.getenv("HOME")+"/tmp" ;
        if ( ! tmpdir.endsWith("/") )
            tmpdir = tmpdir+"/" ;
        
        FusekiCmd.main(
                     //  "-v",
                     //"--update", "--memtdb", "--mgt", "/ds"
                     
                     "--update", "--mem", "--mgt", "/ds"
                     
                     //"--update", "--loc="+tmpdir+"DB", "--set=tdb:unionDefaultGraph=true", "/ds"
                     //"--update", "--mem", "/ds"

                     //"--update", "--memtdb", "--set=tdb:unionDefaultGraph=true", "/ds"
                     
                    //"--debug",
                    //"--update",
                    //"--timeout=1000,5000",
                    //"--set=arq:queryTimeout=1000",
                    //"--port=3030",
                    //"--mgtPort=3031",
                    //"--mem",
                    //"--home=/home/afs/Projects/Fuseki",
                    //"--loc=DB",
                    //"--file=D.nt",
                    //"--gzip=no",
                    //"--desc=desc.ttl",
                    //--pages=
                    //"--jetty-config=jetty-fuseki.xml",
                    //"--config=config-tdb.ttl"
                    // "/ds"
                    ) ;
        System.exit(0) ;
    }

}
