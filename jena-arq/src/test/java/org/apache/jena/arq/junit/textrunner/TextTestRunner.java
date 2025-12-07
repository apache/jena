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

package org.apache.jena.arq.junit.textrunner;

import java.util.List;
import java.util.Objects;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import org.apache.jena.arq.junit.EarlReport;
import org.apache.jena.arq.junit.manifest.EarlReporter;
import org.apache.jena.atlas.io.IndentedWriter;

public class TextTestRunner {

    public static void run(List<String> manifestFiles) {
        run(null, manifestFiles);
    }

    public static void run(EarlReport report, List<String> manifestFiles) {
        runInternal(report, manifestFiles);
    }

    static void runInternal(EarlReport earlReport, List<String> filenames) {
        try ( IndentedWriter out = IndentedWriter.stdout.clone().setFlushOnNewline(true) ; ) {
            // Statics used for configuration so multiple calls would
            // interfere for both input configuration and output printing.
            synchronized(TextTestRunner.class) {
                runInternal0(out, earlReport, filenames);
            }
        }
    }

    // Either an EARL report xor text output.
    static void runInternal0(IndentedWriter out, EarlReport earlReport, List<String> manifestFiles) {
        Objects.requireNonNull(manifestFiles);

        if ( manifestFiles.isEmpty() )
            throw new IllegalArgumentException("No manifests");

        boolean produceEarlReport = earlReport!=null ;
        if ( produceEarlReport ) {
            EarlReporter.setEarlReport(earlReport);
        }

        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        LoggingListener loggerListener = LoggingListener.forBiConsumer((throwable,string)->{
            out.println(string.get());
        });

        ManifestConfiguration config = ManifestConfiguration.get();
        manifestFiles.forEach(config::add);

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(ManifestHolder.class))
                .build();

        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);

        ExecutionStats executionStats = new ExecutionStats();
        PrintExecutionListener printExecListener = new PrintExecutionListener(out);

        if ( produceEarlReport ) {
            // Build report, no output.
            launcher.registerTestExecutionListeners(executionStats);
        } else {
            launcher.registerTestExecutionListeners(executionStats, printExecListener, summaryListener);
        }

        // Run, which calls the TestFactory which generates the tests from the manifest.
        launcher.execute(request);

        // For skips tests.
        TestExecutionSummary summary = summaryListener.getSummary();

        if ( produceEarlReport ) {
            //RDFWriter.source(earlReport.getModel()).format(RDFFormat.TURTLE).output(System.out);
            EarlReporter.clearEarlReport();
        } else {
            out.println();
            if ( executionStats.getTestFailures() == 0 ) {
                out.println("** Success");
                out.println();
            } else {
                out.printf("** Failures: %s\n", executionStats.getTestFailures());
                out.println();
            }
            // summary should be null only when producing EARL reports.
            //summary.printTo
            if ( summary.getTestsSkippedCount() > 0 ) {
                out.println("Manifests:     "+executionStats.getContainerCount());
                out.println("Tests pass:    "+executionStats.getTestPasses());
                out.println("Tests fail:    "+executionStats.getTestFailures());
                out.println("Tests skipped: "+summary.getTestsSkippedCount());
            } else {
                out.println("Manifests:  "+executionStats.getContainerCount());
                out.println("Tests pass: "+executionStats.getTestPasses());
                out.println("Tests fail: "+executionStats.getTestFailures());
            }
        }
    }
}
