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
package org.apache.jena.fuseki.ctl;

import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.metrics.FusekiRequestsMetrics;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;

import static org.apache.jena.riot.WebContent.charsetUTF8;
import static org.apache.jena.riot.WebContent.contentTypeJSON;

public class ActionPrometheus extends ActionCtl {

    private PrometheusMeterRegistry meterRegistry;

    @Override
    public void init(ServletConfig config) throws ServletException {
        DataAccessPointRegistry dataAccessPointRegistry = (DataAccessPointRegistry)config.getServletContext().getAttribute(Fuseki.attrNameRegistry) ;

        meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        new JvmMemoryMetrics().bindTo( meterRegistry );
        new FusekiRequestsMetrics(dataAccessPointRegistry).bindTo( meterRegistry );
    }

    @Override
    final protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void perform(HttpAction action) {
        try (ServletOutputStream out = action.response.getOutputStream()) {
            action.response.setContentType( contentTypeJSON );
            action.response.setCharacterEncoding( charsetUTF8 );

            out.write( meterRegistry.scrape().getBytes() );
        } catch (IOException e) {
            ServletOps.errorOccurred( e );
        }
    }

}
