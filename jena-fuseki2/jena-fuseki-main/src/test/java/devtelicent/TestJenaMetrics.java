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

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.fmods.FusekiOpenTelemetryModule;
import org.apache.jena.fuseki.main.fmods.FusekiOpenTelemetryModule.FMod_OpenTelemetry;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestJenaMetrics {

    @Test
    public void test_no_configuration() {
        OpenTelemetry otel = JenaMetrics.get();
        Assert.assertNotNull(otel);
    }

    @Test
    public void test_explicit_configuration() {
        OpenTelemetry defOtel = JenaMetrics.get();
        OpenTelemetry explicit = OpenTelemetry.propagating(ContextPropagators.noop());
        Assert.assertNotEquals(defOtel, explicit);

        JenaMetrics.set(explicit);
        Assert.assertEquals(JenaMetrics.get(), explicit);
    }

    @Test
    public void test_get_meter_01() {
        Meter m;
        try {
            mockOpenTelemetry_01();

            // If we've explicitly configured a proper Open Telemetry instance then we should get the same meter each
            // time we use the same meter name
            m = JenaMetrics.getMeter("test", "0.1");
            Meter m2 = JenaMetrics.getMeter("test", "0.1");
            Assert.assertEquals(m, m2);

            // However a different meter name should produce a different instance
            Meter n = JenaMetrics.getMeter("other", "0.1");
            Assert.assertNotEquals(m, n);
            Assert.assertNotEquals(m2, n);
        } finally {
            JenaMetrics.reset();
        }

        // After a reset should get different meter instances again
        Meter m3 = JenaMetrics.getMeter("test", "0.1");
        Assert.assertNotEquals(m, m3);

        // Calling reset() again has no effect here
        JenaMetrics.reset();
    }

    @Test
    public void test_get_meter_02() {
        JenaMetrics.getMeter("test");
    }

    private static void mockOpenTelemetry_01() {
        MetricExporter exporter = mock(MetricExporter.class);
        when(exporter.getDefaultAggregation(any())).thenReturn(Aggregation.defaultAggregation());
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                                                         .registerMetricReader(PeriodicMetricReader.builder(exporter)
                                                                                                   .setInterval(
                                                                                                           Duration.ofSeconds(
                                                                                                                   5))
                                                                                                   .build())
                                                         .build();
        OpenTelemetry otel = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();
        JenaMetrics.set(otel);
    }

    @Test
    public void test_get_counter_01() {
        try {
            mockOpenTelemetry_01();

            Meter m = JenaMetrics.getMeter("test", "0.1");
            LongCounter counter = m.counterBuilder("test")
                                   .setDescription("Description")
                                   .build();
            LongCounter counter2 = m.counterBuilder("test")
                                    .setDescription("Description")
                                    .build();
            // Open Telemetry SDK caching should result in the same counter being returned here
            Assert.assertEquals(counter, counter2);


            // Even if we use a different meter a counter of the same name returns the same counter instance
            Meter m2 = JenaMetrics.getMeter("other", "0.2");
            Assert.assertNotEquals(m, m2);
            LongCounter counter2a = m2.counterBuilder("test")
                                     .setDescription("Description")
                                     .build();
            Assert.assertEquals(counter, counter2a);

            // However a different counter name will result in a different instance
            LongCounter counter3 = m2.counterBuilder("other")
                                     .setDescription("Description")
                                     .build();
            Assert.assertNotEquals(counter, counter3);
            Assert.assertNotEquals(counter2, counter3);
        } finally {
            JenaMetrics.reset();
        }
    }

    @Test
    public void time_01() {
        verifyTimingMetrics(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void verifyTimingMetrics(Runnable runnable) {
        try {
            MetricsCollector collector = new MetricsCollector();
            MetricReader reader = prepareTimingReader(collector);

            Meter m = JenaMetrics.getMeter("test", "0.1");
            DoubleHistogram histogram = m.histogramBuilder("timings").build();
            Attributes attributes =
                    Attributes.of(AttributeKey.stringKey(AttributeNames.INSTANCE_ID), UUID.randomUUID().toString());

            JenaMetrics.time(histogram, attributes, runnable);
            verifyTimingMetric(collector, reader);
        } finally {
            JenaMetrics.reset();
        }
    }

    private static <T> void verifyTimingMetrics(Callable<T> runnable, Class<?> expectedError) {
        try {
            MetricsCollector collector = new MetricsCollector();
            MetricReader reader = prepareTimingReader(collector);

            Meter m = JenaMetrics.getMeter("test", "0.1");
            DoubleHistogram histogram = m.histogramBuilder("timings").build();
            Attributes attributes =
                    Attributes.of(AttributeKey.stringKey(AttributeNames.INSTANCE_ID), UUID.randomUUID().toString());

            try {
                JenaMetrics.time(histogram, attributes, runnable);
            } catch (Exception e) {
                if (expectedError == null) {
                    Assert.fail("Error not expected");
                } else {
                    Assert.assertTrue(expectedError.isAssignableFrom(e.getClass()));
                }
            }
            verifyTimingMetric(collector, reader);
        } finally {
            JenaMetrics.reset();
        }
    }

    private static void verifyTimingMetric(MetricsCollector collector, MetricReader reader) {
        reader.forceFlush();

        Map<String, Map<Attributes, Double>> metrics = collector.getAllMetrics();
        Assert.assertFalse(metrics.isEmpty());
        Double count = collector.getMetric("timings.count", Attributes.empty());
        Assert.assertEquals(count, 1.0, 0.001);
    }

    private static MetricReader prepareTimingReader(MetricsCollector collector) {
        MetricReader reader = PeriodicMetricReader.builder(collector).setInterval(Duration.ofSeconds(5)).build();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                                                         .registerMetricReader(reader)
                                                         .build();
        OpenTelemetry otel = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();
        JenaMetrics.set(otel);
        return reader;
    }

    @Test
    public void time_02() {
        verifyTimingMetrics(() -> {
            return 12345;
        }, null);
    }

    @Test
    public void time_03() {
        verifyTimingMetrics(() -> {
            throw new Exception("test");
        }, Exception.class);
    }

    @Test
    public void time_04() {
        verifyTimingMetrics(() -> {
            throw new FileNotFoundException();
        }, IOException.class);
    }

    @Test
    public void server_metrics_reader() throws IOException, InterruptedException {
        InMemoryMetricReader reader = InMemoryMetricReader.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();
        JenaMetrics.set(sdk);
        Assert.assertEquals(reader.collectAllMetrics().size(), 0);

        JenaSystem.init();
        FusekiLogging.setLogging();
        FusekiModule module = new FusekiOpenTelemetryModule.FMod_OpenTelemetry();
        FusekiModules.add(module);

        FusekiServer server = FusekiServer.create()
                .port(0)
                .add("/ds", DatasetGraphFactory.empty())
                .build()
                .start();
        int port = server.getPort();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:"+port+"/ds"))
                    .GET()
                    .build();
            HttpResponse<Void> response = HttpEnv.getDftHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
            Assert.assertTrue(reader.collectAllMetrics().size() >= 44);

            Assert.assertTrue(String.valueOf(reader.collectAllMetrics()).contains("process.runtime.jvm.cpu.utilization"));
            Assert.assertTrue(String.valueOf(reader.collectAllMetrics()).contains(
                    "attributes={messaging.destination=\"\", messaging.destination_kind=\"Graph Store Protocol\", " +
                            "messaging.operation=\"gsp-rw\", messaging.system=\"/ds\"}, value=1"));
        } finally { server.stop(); }
    }

    @Test
    public void server_metrics_exporter() throws IOException, InterruptedException {
//        SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(PeriodicMetricReader
//                .builder(new LoggingMetricExporter()).setInterval(Duration.ofSeconds(30)).build()).build();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(PeriodicMetricReader
                .builder(OtlpGrpcMetricExporter.builder().build()).build()).build();
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();
        JenaMetrics.set(sdk);

        JenaSystem.init();
        FusekiLogging.setLogging();
        FusekiModule module = new FMod_OpenTelemetry();
        FusekiModules.add(module);

        FusekiServer server = FusekiServer.create()
                .port(0)
                .add("/ds", DatasetGraphFactory.empty())
                .build()
                .start();
        int port = server.getPort();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:"+port+"/ds"))
                    .GET()
                    .build();
            HttpResponse<Void> response = HttpEnv.getDftHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
//            server.join();
        } finally { server.stop(); }
    }
}
