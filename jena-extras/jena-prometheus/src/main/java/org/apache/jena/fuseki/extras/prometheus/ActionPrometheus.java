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
package org.apache.jena.fuseki.extras.prometheus;

import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import org.apache.jena.fuseki.ctl.ExtraAction;
import org.apache.jena.fuseki.metrics.MetricRegistryProvider;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.WebContent;

/**
 * Maps to $/extras/prometheus
 */
public class ActionPrometheus extends ExtraAction {

    @Override
    public String getPath() {
        return "prometheus";
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    }

    @Override
    protected void perform(HttpAction action) {
        try (ServletOutputStream out = action.response.getOutputStream()) {
            action.response.setContentType( WebContent.contentTypeJSON );
            action.response.setCharacterEncoding( WebContent.charsetUTF8 );

            out.write( ((PrometheusMeterRegistry)MetricRegistryProvider.get(action.request.getServletContext())).scrape().getBytes() );
        } catch (Throwable t) {
            ServletOps.errorOccurred( t );
        }
    }
}
