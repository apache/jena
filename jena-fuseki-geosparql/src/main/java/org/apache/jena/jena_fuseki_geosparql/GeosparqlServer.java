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
package org.apache.jena.jena_fuseki_geosparql;

import java.lang.invoke.MethodHandles;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiServer.Builder;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class GeosparqlServer extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final int port;
    private final String datasetName;
    private final String localServiceURL;
    private final boolean loopbackOnly;
    private final boolean allowUpdate;
    private final FusekiServer server;
    private Thread shutdownThread = null;

    public GeosparqlServer(int port, String datasetName, boolean loopbackOnly, Dataset dataset, boolean allowUpdate) {

        this.port = port;
        this.datasetName = checkDatasetName(datasetName);
        this.localServiceURL = "http://localhost:" + port + "/" + datasetName;
        this.loopbackOnly = loopbackOnly;
        this.allowUpdate = allowUpdate;

        Builder builder = FusekiServer.create()
                .port(port)
                .loopback(loopbackOnly);
        builder.add(datasetName, dataset, allowUpdate);
        this.server = builder.build();

    }

    private String checkDatasetName(String datasetName) {

        if (datasetName.isEmpty()) {
            LOGGER.warn("Empty dataset name. Defaulting to '/ds'.");
            return "/ds";
        }

        if (datasetName.charAt(0) == '/') {
            return datasetName;
        } else {
            return "/" + datasetName;
        }
    }

    @Override
    public void run() {
        LOGGER.info("GeoSPARQL Server: Running - Port: {}, Dataset: {}, Loopback Only: {},  Allow Update: {}", port, datasetName, loopbackOnly, allowUpdate);
        addShutdownHook();
        this.server.start();
    }

    private void addShutdownHook() {
        removeShutdownHook();

        Thread thread = new Thread() {
            @Override
            public void run() {
                server.stop();
                LOGGER.info("GeoSPARQL Server: Shutdown");
            }
        };
        Runtime.getRuntime().addShutdownHook(thread);
        shutdownThread = thread;
    }

    private void removeShutdownHook() {
        if (shutdownThread != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownThread);
            } catch (IllegalStateException ex) {
                LOGGER.info("Shutdown in progress.");
            } finally {
                shutdownThread = null;
            }
        }
    }

    public void shutdown() {
        server.stop();
        removeShutdownHook();
        LOGGER.info("GeoSPARQL Server: Shutdown");
    }

    public int getPort() {
        return port;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public String getLocalServiceURL() {
        return localServiceURL;
    }

    public boolean isLoopbackOnly() {
        return loopbackOnly;
    }

    public boolean isAllowUpdate() {
        return allowUpdate;
    }

    @Override
    public String toString() {
        return "GeosparqlServer{" + "port=" + port + ", datasetName=" + datasetName + ", localServiceURL=" + localServiceURL + ", loopbackOnly=" + loopbackOnly + ", allowUpdate=" + allowUpdate + '}';
    }

}
