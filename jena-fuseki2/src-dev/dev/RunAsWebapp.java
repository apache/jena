/**
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

import org.apache.jena.fuseki.FusekiLogging ;
import org.apache.jena.fuseki.jetty.JettyFuseki ;
import org.apache.jena.fuseki.server.FusekiServer ;
import org.apache.jena.fuseki.server.FusekiServletContextListener ;
import org.eclipse.jetty.server.Server ;

public class RunAsWebapp
{
 // See also http://www.eclipse.org/jetty/documentation/current/embedded-examples.html#embedded-one-webapp
    public static void main(String[] args) //throws Exception
    {
        FusekiServer.FUSEKI_HOME = Paths.get("").toAbsolutePath() ;
        FusekiServer.FUSEKI_BASE = Paths.get("run") ;
        FusekiLogging.setLogging() ; 
        // No command line.
        FusekiServletContextListener.initialSetup = null ;
        // Create a basic jetty-hosted Fuseki server
        Server server = JettyFuseki.create("/fuseki", 3030) ;
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            System.out.flush() ;
            ex.printStackTrace(System.err);
        }
    }
}

