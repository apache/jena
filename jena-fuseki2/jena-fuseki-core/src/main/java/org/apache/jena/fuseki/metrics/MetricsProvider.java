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

import java.util.Objects;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.ServletContext;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.servlets.HttpAction;

/** Micrometer registry and output generator. */
public interface MetricsProvider {
    public MeterRegistry getMeterRegistry();
    public void scrape(HttpAction action);

    /** Bind each data access point in a DataAccessPointRegistry to the system Micrometer {@link MeterRegistry}. */
    public default void dataAccessPointMetrics(MetricsProvider metricsProvider, DataAccessPointRegistry dapRegistry) {
        try {
            dapRegistry.accessPoints().forEach(dap->addDataAccessPointMetrics(dap));
        } catch (Throwable th) {
            Fuseki.configLog.error("Failed to bind all data access points to netrics provider", th);
        }
    }

    public default void addDataAccessPointMetrics(DataAccessPoint dataAccessPoint) {
        MeterRegistry meterRegistry = this.getMeterRegistry();
        if (meterRegistry != null )
            addDataAccessPointMetrics(meterRegistry, dataAccessPoint);
    }

    private static void addDataAccessPointMetrics(MeterRegistry meterRegistry, DataAccessPoint dataAccessPoint) {
        if ( dataAccessPoint == null )
            Log.warn(MetricsProvider.class, "addDataAccessPointMetrics: Null DataAccessPoint");
        new FusekiRequestsMetrics(dataAccessPoint).bindTo(meterRegistry);
    }

    public static void setMetricsProvider(ServletContext servletContext, MetricsProvider provider) {
        Objects.requireNonNull(servletContext);
        if ( provider == null )
            servletContext.removeAttribute(Fuseki.attrMetricsProvider);
        else
            servletContext.setAttribute(Fuseki.attrMetricsProvider, provider);
    }

    public static MetricsProvider getMetricsProvider(ServletContext servletContext) {
        Objects.requireNonNull(servletContext);
        return (MetricsProvider)servletContext.getAttribute(Fuseki.attrMetricsProvider);
    }
}
