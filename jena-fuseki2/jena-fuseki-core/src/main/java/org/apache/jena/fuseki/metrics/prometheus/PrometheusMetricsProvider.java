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
package org.apache.jena.fuseki.metrics.prometheus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.File;
import javax.servlet.ServletOutputStream;
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

        new FileDescriptorMetrics().bindTo( meterRegistry );
        new ProcessorMetrics().bindTo( meterRegistry );
        new ClassLoaderMetrics().bindTo( meterRegistry );
        new UptimeMetrics().bindTo( meterRegistry );
        for (File root : File.listRoots()) {
            new DiskSpaceMetrics(root).bindTo( meterRegistry );
        }
        new JvmGcMetrics().bindTo( meterRegistry );
        new JvmMemoryMetrics().bindTo( meterRegistry );
        new JvmThreadMetrics().bindTo( meterRegistry );
    }

    @Override
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    @Override
    public void scrape(HttpAction action) {
        try (ServletOutputStream out = action.response.getOutputStream()) {
            action.response.setContentType( WebContent.contentTypeTextPlain );
            action.response.setCharacterEncoding( WebContent.charsetUTF8 );

            out.write( meterRegistry.scrape().getBytes() );
        } catch (Throwable t) {
            ServletOps.errorOccurred( t );
        }
    }

}
