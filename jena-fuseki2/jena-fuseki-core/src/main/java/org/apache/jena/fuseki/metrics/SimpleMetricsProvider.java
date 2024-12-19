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

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.HttpSC;

public class SimpleMetricsProvider implements MetricsProvider {

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    public SimpleMetricsProvider() {}

    @Override
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    @Override
    public void scrape(HttpAction action) {

        HttpServletResponse response = action.getResponse();
        StringBuilder sbuff = new StringBuilder(1000);

        // Text-based dump format. Unstable.
        try {
            meterRegistry.forEachMeter(meter->{
                Id id = meter.getId();
                output(sbuff, "Meter %s", id);
                output(sbuff, "  Name     %s", id.getName());
                output(sbuff, "  BaseUnit %s",id.getBaseUnit());
                id.getTags().forEach(tag->{
                    output(sbuff, "    Tag %s %s", tag.getKey(), tag.getValue());
                });

                meter.measure().forEach(measurement->{
                    output(sbuff, "    Measure %s %s", measurement.getStatistic(), measurement.getValue());
                });
            });

            String txt = sbuff.toString();
            action.setResponseContentType(WebContent.contentTypeTextPlain);
            try ( ServletOutputStream x = action.getResponseOutputStream() ) {
                x.print(txt);
            }
        } catch (Throwable th) {
            ServletOps.error(HttpSC.INTERNAL_SERVER_ERROR_500);
        }
    }

    private void output(StringBuilder sbuff, String fmt, Object...args) {
        String str = fmt.formatted(args);
        sbuff.append(str);
        if ( ! str.endsWith("\n") )
            sbuff.append("\n");
    }
}
