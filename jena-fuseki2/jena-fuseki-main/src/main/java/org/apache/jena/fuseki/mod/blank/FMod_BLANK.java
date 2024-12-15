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

package org.apache.jena.fuseki.mod.blank;

import org.apache.jena.fuseki.main.sys.FusekiAutoModule;

/**
 * Template.
 *
 * src/main/resources/META-INF/services/org.apache.jena.fuseki.main.sys.FusekiAutoModule
 */
public class FMod_BLANK implements FusekiAutoModule {
    @Override
    public String name() { return "BLANK"; }

//    @Override public void start() { }
//    @Override public void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) { }
//    @Override public void configured(DataAccessPointRegistry dapRegistry, Model configModel) {
//        dapRegistry.accessPoints().forEach(accessPoint->configDataAccessPoint(accessPoint, configModel));
//    }
//    @Override public void configDataAccessPoint(DataAccessPoint dap, Model configModel) {}
//    @Override public void server(FusekiServer server) { }
//    @Override public void serverBeforeStarting(FusekiServer server) { }
//    @Override public void serverAfterStarting(FusekiServer server) { }
//    @Override public void serverStopped(FusekiServer server) { }
//    @Override public void stop() {}
}
