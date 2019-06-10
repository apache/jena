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

package org.apache.jena.fuseki.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.sys.JenaSystem;

/** Setup the environment and logging.
 *  Runs before the {@link ShiroEnvironmentLoader}.
 *  The main configuration happens in {@link FusekiServerListener} which runs after {@link ShiroEnvironmentLoader}.
 */
public class FusekiServerEnvironmentInit implements ServletContextListener {

    public FusekiServerEnvironmentInit() { }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // These two do not touch Jena.
        FusekiEnv.setEnvironment();
        FusekiLogging.setLogging(FusekiEnv.FUSEKI_BASE);
        JenaSystem.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Stop handling requests.

        // ActionService uses DataAccessPointRegistry to map URI to services (DataAccessPoint)

        // DataService -> DataService
//        DataAccessPointRegistry.shutdown();
//        DatasetDescriptionRegistry.reset();
        JenaSystem.shutdown();
    }
}
