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

package devfuseki;

import java.nio.file.Paths ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.cmd.FusekiCmd ;

public class Exec {

    public static void main(String[] args) throws Exception {

        if ( true ) {
            //FusekiLogging.setLogging();
            LogCtl.setLog4j() ;
            String logbase = "org.apache.jena.fuseki" ;
            LogCtl.enable(logbase+".Config") ;
            LogCtl.enable("org.apache.shiro") ;
        }

        String fusekiHome = "/home/afs/Jena/jena-fuseki2/jena-fuseki-core" ;
        System.setProperty("FUSEKI_HOME", fusekiHome) ;
        
        if ( false ) {
            String fusekiBase = "/home/afs/tmp/run" ;
            String runArea = Paths.get(fusekiBase).toAbsolutePath().toString() ;
            System.setProperty("FUSEKI_HOME", fusekiHome) ;
            FileOps.ensureDir(runArea) ;
        }
        
        FusekiCmd.main(
                       "--conf=config.ttl"
                      ) ;
        System.exit(0) ;
    }

}
