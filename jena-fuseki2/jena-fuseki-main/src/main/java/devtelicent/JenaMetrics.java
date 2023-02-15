/*
 * Copyright (C) 2023 Telicent Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package devtelicent;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Entrypoint for Jena related metrics
 */
public class JenaMetrics {

    private static OpenTelemetry OTEL = null;
    private static final Object lock = new Object();
    private static Map<String, Meter> METER_CACHE = new HashMap<>();
    private JenaMetrics() {
    }

    /**
     * Gets the configured Open Telemetry instance
     * <p>
     * This will either be the instance configured explicitly via {@link #set(OpenTelemetry)} or the global instance
     * from {@link GlobalOpenTelemetry#get()}.
     * </p>
     *
     * @return Open Telemetry instance
     */
    public static OpenTelemetry get() {
        synchronized (lock) {
            if (OTEL == null) {
                return GlobalOpenTelemetry.get();
            }
            return OTEL;
        }
    }

    /**
     * Sets a specific Open Telemetry instance, primarily intended for testing
     *
     * @param otel Open Telemetry instance
     */
    public static void set(OpenTelemetry otel) {
        synchronized (lock) {
            boolean needCacheReset = otel != OTEL;
            OTEL = otel;
            if (needCacheReset) {
                resetCaches();
            }
        }
    }

    /**
     * Resets to using the global Open Telemetry instance
     */
    public static void reset() {
        set(null);
    }

    private static void resetCaches() {
        METER_CACHE.clear();
    }

    /**
     * Gets or create the Meter instance for the given library
     * <p>
     * Library version detection will be attempted by calling {@link LibraryVersion#get(String)}, see
     * {@link LibraryVersion} for more details.
     * </p>
     *
     * @param library Library
     * @return Meter instance
     */
    public static Meter getMeter(String library) {
        return getMeter(library, LibraryVersion.get(library));
    }

    /**
     * Gets or creates the Meter instance for the given library
     *
     * @param library Library name
     * @param version Library version
     * @return Meter instance
     */
    public static Meter getMeter(String library, String version) {
        synchronized (lock) {
            Meter m = METER_CACHE.computeIfAbsent(String.format("%s-%s", library, version),
                                                  k -> get().meterBuilder(library)
                                                            .setInstrumentationVersion(version)
                                                            .build());
            return m;
        }
    }

    private static final double NANOSECONDS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);

    /**
     * Times a task (in seconds) recording its timing
     *
     * @param histogram        Histogram
     * @param metricAttributes Metric attributes
     * @param task             Task to run
     */
    public static void time(DoubleHistogram histogram, Attributes metricAttributes, Runnable task) {
        long start = System.nanoTime();
        try {
            task.run();
        } finally {
            long elapsed = System.nanoTime() - start;
            histogram.record(elapsed / NANOSECONDS_PER_SECOND, metricAttributes);
        }
    }

    /**
     * Times a task (in seconds) recording its timing
     *
     * @param histogram        Histogram
     * @param metricAttributes Metric attributes
     * @param task             Task to run
     * @param <T>              Task return type
     * @return Task return value
     * @throws Exception Thrown if there's a problem running the task
     */
    public static <T> T time(DoubleHistogram histogram, Attributes metricAttributes, Callable<T> task) throws
            Exception {
        long start = System.nanoTime();
        try {
            return task.call();
        } finally {
            long elapsed = System.nanoTime() - start;
            histogram.record(elapsed / NANOSECONDS_PER_SECOND, metricAttributes);
        }
    }
}
