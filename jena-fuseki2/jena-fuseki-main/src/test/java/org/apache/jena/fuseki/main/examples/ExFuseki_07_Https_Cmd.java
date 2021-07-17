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

package org.apache.jena.fuseki.main.examples;

import org.apache.jena.fuseki.main.cmds.FusekiMainCmd;

/** Run a Fuseki server with https from the command line */
public class ExFuseki_07_Https_Cmd {

    // curl -k -d 'query=ASK{}' https://localhost:3443/ds

    public static void main(String...argv) {
        try {
            cmdHttps();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static void cmdHttps() {
        FusekiMainCmd.main(
            "--https="+ExConst.DIR+"/certs/https-details",
            "--port=3030",
            "--httpsPort=3443",
            "--mem",
            "/ds");
    }
}
