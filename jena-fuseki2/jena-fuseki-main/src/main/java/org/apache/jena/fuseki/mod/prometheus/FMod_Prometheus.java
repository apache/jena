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

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.ctl.ActionMetrics;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.metrics.MetricsProvider;
import org.apache.jena.rdf.model.Model;

/**
 * Prometheus Metrics.
 *
 * PrometheusMetricsProvider
 */
public class FMod_Prometheus implements FusekiModule {

    public static FusekiModule create() {
        return new FMod_Prometheus();
    }

    private MetricsProvider metricsProvider = null;

    public FMod_Prometheus() {}

//    @Override
//    public int level() {
//        return 5000;
//    }

    @Override
    public String name() { return "FMod Prometheus Metrics"; }

    @Override public void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) {
        metricsProvider = new PrometheusMetricsProvider();
        serverBuilder.addServletAttribute(Fuseki.attrMetricsProvider, metricsProvider);
        serverBuilder.addServlet("/$/metrics", new ActionMetrics());
    }

    @Override public void server(FusekiServer server) {
        MetricsProvider metricsProvider = MetricsProvider.getMetricsProvider(server.getServletContext());
        metricsProvider.dataAccessPointMetrics(metricsProvider, server.getDataAccessPointRegistry());
    }
}
