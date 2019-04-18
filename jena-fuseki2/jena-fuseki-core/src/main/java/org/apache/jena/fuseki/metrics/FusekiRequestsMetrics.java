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
package org.apache.jena.fuseki.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.List;
import org.apache.jena.fuseki.server.Counter;
import org.apache.jena.fuseki.server.CounterName;
import org.apache.jena.fuseki.server.CounterSet;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;

public class FusekiRequestsMetrics implements MeterBinder {

    private DataAccessPoint dataAccessPoint;

    public FusekiRequestsMetrics(DataAccessPoint dataAccessPoint) {
        this.dataAccessPoint= dataAccessPoint;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        DataService dataService = dataAccessPoint.getDataService();
        for (Operation operation : dataService.getOperations()) {
            List<Endpoint> endpoints = dataService.getEndpoints( operation );
            for (Endpoint endpoint : endpoints) {
                CounterSet counters = endpoint.getCounters();
                for (CounterName counterName : counters.counters()) {
                    Counter counter = counters.get( counterName );

                    Gauge.builder(
                            "fuseki_" + counterName.getFullName(), counter, Counter::value )
                            .tags( new String[] {
                                    "dataset", dataAccessPoint.getName(),
                                    "endpoint", endpoint.getName(),
                                    "operation", operation.getName(),
                                    "description", operation.getDescription()
                            } )
                            .register( registry );
                }
            }
        }
    }

}
