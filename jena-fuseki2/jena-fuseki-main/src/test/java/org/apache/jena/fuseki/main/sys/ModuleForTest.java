/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main.sys;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiServer.Builder;
import org.apache.jena.fuseki.main.runner.ServerArgs;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

// Not autoloaded
public class ModuleForTest implements FusekiModule {


    // ---- FusekiServerArgsHandler
    public AtomicInteger countArgsModify = new AtomicInteger(0);
    public AtomicInteger countArgsPrepare = new AtomicInteger(0);
    public AtomicInteger countArgsBuilder = new AtomicInteger(0);

    // ---- FusekiBuildCycle
    public AtomicInteger countPrepared = new AtomicInteger(0);
    public AtomicInteger countConfigured = new AtomicInteger(0);
    public AtomicInteger countConfigDataAccessPoint = new AtomicInteger(0);
    public AtomicInteger countServer = new AtomicInteger(0);
    public AtomicInteger countServerConfirmReload = new AtomicInteger(0);
    public AtomicInteger countServerReload = new AtomicInteger(0);

    // ---- FusekiStartStop
    public AtomicInteger countServerBeforeStarting = new AtomicInteger(0);
    public AtomicInteger countServerAfterStarting = new AtomicInteger(0);
    public AtomicInteger countServerStopped = new AtomicInteger(0);

    public ModuleForTest() {}

    private String modName = "ModuleForTest-"+UUID.randomUUID().toString();

    @Override
    public String name() {
        return modName;
    }

    public void clearLifecycle() {
        countArgsModify.set(0);
        countArgsPrepare.set(0);
        countArgsBuilder.set(0);

        countPrepared.set(0);
        countConfigured.set(0);
        countConfigDataAccessPoint.set(0);
        countServer.set(0);
        countServerConfirmReload.set(0);
        countServerReload.set(0);

        countServerBeforeStarting.set(0);
        countServerAfterStarting.set(0);
        countServerStopped.set(0);
    }

    // ---- FusekiServerArgsHandler

    @Override public void serverArgsModify(CmdGeneral cmdGeneral, ServerArgs serverArgs) {
        countArgsModify.incrementAndGet();
    }

    @Override public void serverArgsPrepare(CmdGeneral cmdGeneral, ServerArgs serverArgs) {
        countArgsPrepare.incrementAndGet();
    }

    @Override public void serverArgsBuilder(Builder build, Model confModel) {
        countArgsBuilder.incrementAndGet();
    }

    // ---- FusekiBuildCycle

    @Override public void prepare(FusekiServer.Builder builder, Set<String> datasetNames, Model configModel) {
        countPrepared.incrementAndGet();
    }

    @Override public void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        countConfigured.getAndIncrement();
    }

    @Override public void configDataAccessPoint(DataAccessPoint dataAccessPoint, Model confModel) {
        countConfigDataAccessPoint.getAndIncrement();
    }

    // Built, not started, about to be returned to the builder caller
    @Override public void server(FusekiServer server) {
        countServer.getAndIncrement();
    }

    @Override public boolean serverConfirmReload(FusekiServer server) {
        countServerConfirmReload.incrementAndGet();
        return true;
    }

    @Override public void serverReload(FusekiServer server) {
        countServerReload.incrementAndGet();
    }

    // ---- FusekiStartStop

    @Override public void serverBeforeStarting(FusekiServer server) {
        countServerBeforeStarting.getAndIncrement();
    }

    @Override public void serverAfterStarting(FusekiServer server) {
        countServerAfterStarting.getAndIncrement();
    }

    @Override public void serverStopped(FusekiServer server) {
        countServerStopped.getAndIncrement();
    }

    // Debugging!
    @Override
    public String toString() {
        return String.format("ModuleForTest[prepared=%s configuration=%s server=%s]",
                             countPrepared.get(),
                             countConfigured.get(),
                             countServer.get());
    }
}
