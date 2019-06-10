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
package org.apache.jena.fuseki.geosparql;

import com.beust.jcommander.JCommander;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import org.apache.jena.geosparql.configuration.SrsException;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.fuseki.geosparql.cli.ArgsConfig;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Main {
    static { FusekiLogging.setLogging(); }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Apache SIS j.u.l logging redirection.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        
        
        LOGGER.info("Arguments Received: {}", Arrays.asList(args));

        ArgsConfig argsConfig = new ArgsConfig();

        JCommander jCommander = JCommander.newBuilder()
                .addObject(argsConfig)
                .build();

        jCommander.setProgramName("GeoSPARQL Fuseki");
        jCommander.parse(args);
        if (argsConfig.isHelp()) {
            jCommander.usage();
            return;
        }

        //Setup dataset
        try {
            Dataset dataset = DatasetOperations.setup(argsConfig);

            //Configure server
            GeosparqlServer server = new GeosparqlServer(argsConfig.getPort(), argsConfig.getDatsetName(), argsConfig.isLoopbackOnly(), dataset, argsConfig.isUpdateAllowed());
            server.start();
        } catch (SrsException | DatasetException | SpatialIndexException ex) {
            LOGGER.error("GeoSPARQL Server:  Exiting - {}: {}", ex.getMessage(), argsConfig.getDatsetName());
        }

    }

}
