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
import java.util.ServiceLoader;
import javax.servlet.ServletContext;

public class MetricRegistryProvider {

    private static MeterRegistry meterRegistry;

    /**
     * Prefer the one stored in the servlet context if there is one
     * @param servletContext
     * @return
     */
    public static MeterRegistry get(ServletContext servletContext) {
        MeterRegistry result = (MeterRegistry)servletContext.getAttribute( MeterRegistry.class.getName() );
        if (result == null) {
            result = load();
            servletContext.setAttribute( MeterRegistry.class.getName(), result );
        }
        return result;
    }

    public static MeterRegistry load() {
        if (meterRegistry == null) {
            ServiceLoader<MetricRegistryLoader> serviceLoader = ServiceLoader.load( MetricRegistryLoader.class );
            MetricRegistryLoader chosenLoader = null;
            for (MetricRegistryLoader loader : serviceLoader) {
                if (chosenLoader == null ||
                        loader.getPriority() < chosenLoader.getPriority()) {
                    chosenLoader = loader;
                }
            }
            meterRegistry = chosenLoader.load();
        }
        return meterRegistry;
    }

}
