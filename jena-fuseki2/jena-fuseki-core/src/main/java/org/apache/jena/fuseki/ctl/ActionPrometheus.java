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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.fuseki.server.Counter;
import org.apache.jena.fuseki.server.CounterName;
import org.apache.jena.fuseki.server.CounterSet;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;

import static org.apache.jena.riot.WebContent.charsetUTF8;
import static org.apache.jena.riot.WebContent.contentTypeJSON;

public class ActionPrometheus extends ActionCtl {

    @Override
    final protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void perform(HttpAction action) {
        try {
            DataAccessPointRegistry dataAccessPointRegistry = action.getDataAccessPointRegistry();

            try (ServletOutputStream out = action.response.getOutputStream()) {
                action.response.setContentType( contentTypeJSON );
                action.response.setCharacterEncoding( charsetUTF8 );

                dataAccessPointRegistry
                        .forEach( (name, access) -> writeMetricForDataAccessPoint( access, out ) );
            }
        } catch (IOException e) {
            ServletOps.errorOccurred(e);
        }
        finally {
            action.endRead();
        }
    }

    private void writeMetricForDataAccessPoint(DataAccessPoint access, OutputStream out) {
        DataService dataService = access.getDataService();
        for (Operation operation : dataService.getOperations()) {
            List<Endpoint> endpoints = dataService.getEndpoints( operation );
            for (Endpoint endpoint : endpoints) {
                CounterSet counters = endpoint.getCounters();
                for (CounterName counterName : counters.counters()) {
                    Properties labels = new Properties();
                    labels.setProperty( "dataset", access.getName() );
                    labels.setProperty( "endpoint", endpoint.getName() );
                    labels.setProperty( "operation", operation.getName());
                    labels.setProperty( "description", operation.getDescription());

                    Counter counter = counters.get( counterName );

                    writeMetric( "fuseki." + counterName.getFullName(),
                            labels, out, counter.value() );
                }
            }
        }
    }

    private void writeMetric(String metricName, Properties labels, OutputStream out, long value) {
        try {
            out.write(
                    String.format("%s{%s} %d%n",
                            StringUtils.replaceChars(metricName, '.', '_'),
                            labels.entrySet().stream()
                                    .map( e -> e.getKey() + "=\"" + e.getValue() + "\"" )
                                    .collect( Collectors.joining( ", " ) ),
                            value
                    ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
}
