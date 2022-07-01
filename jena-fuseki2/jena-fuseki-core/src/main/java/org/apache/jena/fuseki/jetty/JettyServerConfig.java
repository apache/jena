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

package org.apache.jena.fuseki.jetty;


/** Configuration of the Jetty server when run from the command line directly,
 *  not as a webapp/war file.
 */

public class JettyServerConfig
{
    public JettyServerConfig() {}

    /** Port to run the server service on */
    public int port;

    /** Path to run the server service under */
    public String contextPath;

    /** Jetty config file - if null, use the built-in configuration of Jetty */
    public String jettyConfigFile = null;

    /** Listen only on the loopback (localhost) interface */
    public boolean loopback = false;

    /** Enable Accept-Encoding compression. Set to false by default.*/
    public boolean enableCompression = false;

    /** Enable additional logging */
    public boolean verboseLogging = false;

    /** Authentication config file used to setup Jetty Basic auth,
     * if a Jetty config file was set this is ignored since Jetty config
     * allows much more complex auth methods to be implemented.
     * Using Apache Shiro is better as well.
     */
    public String authConfigFile;

}

