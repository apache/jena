/**
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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.servlet.ServletOutputStream;
import org.apache.jena.fuseki.metrics.FusekiMetrics;
import org.apache.jena.fuseki.metrics.MetricsProvider;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.WebContent;

/**
 */
public class PrometheusMetricsProvider implements MetricsProvider {

    private PrometheusMeterRegistry meterRegistry;

    public PrometheusMetricsProvider() {
        meterRegistry = new PrometheusMeterRegistry( PrometheusConfig.DEFAULT );
        meterRegistry.config().commonTags( "application", "fuseki" );
        FusekiMetrics.registerMetrics(meterRegistry);
    }

    @Override
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    @Override
    public void scrape(HttpAction action) {
        try (ServletOutputStream out = action.getResponseOutputStream()) {
            ServletOps.success(action);
            action.setResponseContentType( WebContent.contentTypeTextPlain );
            action.setResponseCharacterEncoding( WebContent.charsetUTF8 );

            out.write( meterRegistry.scrape().getBytes() );
        } catch (Throwable t) {
            ServletOps.errorOccurred( t );
        }
    }
}
