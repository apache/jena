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

package org.apache.jena.fuseki.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.fuseki.build.FusekiBuilder;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.sparql.core.DatasetGraph;

/** Actions on a {@link FusekiServer} */
public class FusekiLib {

    /** Choose a free port */
    public static int choosePort() {
        return FusekiNetLib.choosePort();
    }

    /**
     * Return a collection of the names registered. This collection does not change as the
     * server changes.
     */
    public static Collection<String> names(FusekiServer server) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        int N = dataAccessPoints.size();
        Stream<String> stream = DataAccessPointRegistry.get(server.getServletContext()).keys().stream();
        // Correct size, no reallocate.
        List<String> names = stream.collect(Collectors.toCollection(() -> new ArrayList<>(N)));
        return names;
    }

    /** Add a dataset to a server */
    public static FusekiServer addDataset(FusekiServer server, String name, DatasetGraph dsg, boolean withUpdate) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        FusekiBuilder.addDataset(dataAccessPoints, name, dsg, withUpdate);
        return server;
    }

    /** Add a {@link DataService} to a server */
    public static FusekiServer addDataset(FusekiServer server, String name, DataService dataService) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        FusekiBuilder.addDataService(dataAccessPoints, name, dataService);
        return server;
    }

    /** Remove dataset from a server */
    public static FusekiServer removeDataset(FusekiServer server, String name) {
        DataAccessPointRegistry dataAccessPoints = DataAccessPointRegistry.get(server.getServletContext());
        FusekiBuilder.removeDataset(dataAccessPoints, name);
        return server;
    }
}
