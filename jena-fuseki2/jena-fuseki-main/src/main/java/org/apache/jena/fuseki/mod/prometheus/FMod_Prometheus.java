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

package org.apache.jena.fuseki.mod.prometheus;

import java.util.Set;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.metrics.MetricsProviderRegistry;
import org.apache.jena.rdf.model.Model;

/**
 * Prometheus Metrics.
 *
 * PrometheusMetricsProvider
 */
public class FMod_Prometheus implements FusekiModule {

    private static FusekiModule singleton = new FMod_Prometheus();
    public static FusekiModule get() {
        return singleton;
    }

    public FMod_Prometheus() {}

//    @Override
//    public int level() {
//        return 5000;
//    }
//
//    @Override public void start() {
//        Fuseki.configLog.info("FMod Prometheus Metrics");
//        MetricsProviderRegistry.set(new PrometheusMetricsProvider());
//    }

    @Override
    public String name() { return "FMod Prometheus Metrics"; }

    @Override public void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) {
        //MetricsProviderRegistry.set(new PrometheusMetricsProvider());
        serverBuilder.addServlet("/$/metrics", new ActionMetrics());
    }

    @Override public void server(FusekiServer server) {
        MetricsProviderRegistry.dataAccessPointMetrics(server.getDataAccessPointRegistry());
    }
}
