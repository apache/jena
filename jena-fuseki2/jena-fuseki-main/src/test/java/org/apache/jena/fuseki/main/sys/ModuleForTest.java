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

package org.apache.jena.fuseki.main.sys;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

public class ModuleForTest implements FusekiModule {

    public AtomicInteger countPrepared = new AtomicInteger(0);
    public AtomicInteger countConfiguration = new AtomicInteger(0);
    public AtomicInteger countServer = new AtomicInteger(0);
    public AtomicInteger countServerBeforeStarting = new AtomicInteger(0);
    public AtomicInteger countServerAfterStarting = new AtomicInteger(0);

    public ModuleForTest() {}

    private String modName = "ModuleForTest-"+UUID.randomUUID().toString();

    @Override
    public String name() {
        return modName;
    }

    public void clearLifecycle() {
        // Not countStart.
        countConfiguration.set(0);
        countPrepared.set(0);
        countServer.set(0);
        countServerBeforeStarting.set(0);
        countServerAfterStarting.set(0);
    }

    @Override
    public void prepare(FusekiServer.Builder builder, Set<String> datasetNames, Model configModel) {
        countPrepared.incrementAndGet();
    }

    @Override
    public void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        countConfiguration.getAndIncrement();
    }


    // Built, not started, about to be returned to the builder caller
    @Override public void server(FusekiServer server) {
        countServer.getAndIncrement();
    }

    // Server starting
    @Override public void serverBeforeStarting(FusekiServer server) {
        countServerBeforeStarting.getAndIncrement();
    }

    // Server starting
    @Override public void serverAfterStarting(FusekiServer server) {
        countServerAfterStarting.getAndIncrement();
    }

}
