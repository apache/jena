/*
 * Copyright (C) 2023 Telicent Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.fuseki.main.fmods;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.runtimemetrics.*;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sys.JenaSystem;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

import devtelicent.JenaMetrics;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import java.util.List;
import org.apache.jena.fuseki.server.Counter;
import org.apache.jena.fuseki.server.CounterName;
import org.apache.jena.fuseki.server.CounterSet;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class FusekiOpenTelemetryModule {

    public static void main(String...a) throws Exception {
        JenaSystem.init();
        FusekiLogging.setLogging();

        // Normally done with ServiceLoader
        // A file /META-INF/services/org.apache.jena.fuseki.main.sys.FusekiModule
        // in the jar file with contents:
        //    org.apache.jena.fuseki.main.examples.ExampleModule
        //
        // The file is typically put into the jar by having
        //   src/main/resources/META-INF/services/org.apache.jena.fuseki.main.sys.FusekiModule
        FusekiModule module = new FMod_OpenTelemetry();
        FusekiModules.add(module);

        // Create server.
        FusekiServer server =
            FusekiServer.create()
                .port(0)
                .add("ds", DatasetGraphFactory.empty())
                .build()
                .start();
        int port = server.getPort();

        // Client HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:"+port+"/ds"))
                .GET()
                .build();
        HttpResponse<Void> response = HttpEnv.getDftHttpClient().send(request, BodyHandlers.discarding());
        server.stop();
    }

    public static class FMod_OpenTelemetry implements FusekiModule {

        private String modName = UUID.randomUUID().toString();
        @Override
        public String name() {
            return modName;
        }

        /** Module loaded */
        @Override
        public void start() {
            OpenTelemetry otel = JenaMetrics.get();

            BufferPools.registerObservers(otel);
            Classes.registerObservers(otel);
            Cpu.registerObservers(otel);
            MemoryPools.registerObservers(otel);
            Threads.registerObservers(otel);
            GarbageCollector.registerObservers(otel);
        }

        public static void buildMetrics(DataAccessPoint dap) {
            Meter meter = JenaMetrics.getMeter("Jena");
            DataService dataService = dap.getDataService();

            for (Operation operation : dataService.getOperations()) {
                List<Endpoint> endpoints = dataService.getEndpoints( operation );
                for (Endpoint endpoint : endpoints) {
                    CounterSet counters = endpoint.getCounters();
                    for (CounterName counterName : counters.counters()) {
                        Counter counter = counters.get( counterName );

                        Attributes metricAttributes = Attributes.of(
                                SemanticAttributes.MESSAGING_OPERATION, operation.getName(),
                                SemanticAttributes.MESSAGING_DESTINATION_KIND, operation.getDescription(),
                                SemanticAttributes.MESSAGING_DESTINATION, endpoint.getName(),
                                SemanticAttributes.MESSAGING_SYSTEM, dap.getName());

                        meter.gaugeBuilder("fuseki_" + dap.getName().replace("/", ".") + "_" +
                                        endpoint.getName() + "_" + endpoint.getOperation().getName() + "_" + counterName.getFullName())
                                .setDescription(operation.getDescription())
                                .ofLongs()
                                .buildWithCallback(
                                        measure -> {
                                            measure.record(counter.value(), metricAttributes);
                                        });
                    }
                }
            }
        }

        @Override
        public void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
            dapRegistry.accessPoints().forEach(accessPoint->buildMetrics(accessPoint));
        }

        @Override public void serverAfterStarting(FusekiServer server) {
            System.out.println("Customized server start on port "+server.getHttpPort());
        }
    }
}
