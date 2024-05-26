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
package org.apache.jena.fuseki.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;

public class MetricsProviderRegistry {

    private static int priority = Integer.MAX_VALUE;
    private static MetricsProvider metricsProvider = new SimpleMetricsProvider();

    public static MetricsProvider get() {
        return metricsProvider;
    }

    public static void put(MetricsProvider metricsProvider, int priority) {
        if (priority < MetricsProviderRegistry.priority) {
            MetricsProviderRegistry.priority = priority;
            MetricsProviderRegistry.metricsProvider = metricsProvider;
        }
    }

    /** Bind each data access point in a DataAccessPointRegistry to Prometheus. */
    public static void bindPrometheus(DataAccessPointRegistry dapRegistry) {
        try {
            MeterRegistry meterRegistry = MetricsProviderRegistry.get().getMeterRegistry();
            if (meterRegistry != null) {
                dapRegistry.accessPoints().forEach(dap->{
                    new FusekiRequestsMetrics( dap ).bindTo( meterRegistry );
                });
            }
        } catch (Throwable th) {
            Fuseki.configLog.error("Failed to bind all data access points to Prometheus", th);
        }
    }
}
