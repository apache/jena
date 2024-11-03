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

import java.io.File;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;

/**
 * The choice of metrics for Fuseki.
 */
public class FusekiMetrics {

    public static void registerMetrics(MeterRegistry meterRegistry) {
        new FileDescriptorMetrics().bindTo( meterRegistry );
        new ProcessorMetrics().bindTo( meterRegistry );
        new ClassLoaderMetrics().bindTo( meterRegistry );
        new UptimeMetrics().bindTo( meterRegistry );
        for (File root : File.listRoots()) {
            new DiskSpaceMetrics(root).bindTo( meterRegistry );
        }
        // Has a warning about resource closing.
        @SuppressWarnings("resource")
        JvmGcMetrics x = new JvmGcMetrics();
        x.bindTo( meterRegistry );
        new JvmMemoryMetrics().bindTo( meterRegistry );
        new JvmThreadMetrics().bindTo( meterRegistry );
    }
}
