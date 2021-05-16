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

package org.apache.jena.arq.junit.runners;

import org.apache.jena.sparql.junit.EarlReport;
import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * Run a single test.
 */
public class RunnerOneTest extends Runner
{
    private static int count = 1;
    private final Description description;
    private final EarlReport report;    // Optional.
    private final Runnable testCase;
    private final String testURI;
    private final String name;

    public RunnerOneTest(String name, Runnable test) {
        this(name, test, null, null);
    }

    public RunnerOneTest(String name, Runnable test, String testURI, EarlReport report) {
        this.name = name;
        int count$ = count;
        testCase = test;
        // Names must be unique else Eclipse will not report them.
        description = Description.createSuiteDescription("T-"+count$+": "+name);
        count++;

        // Optional.
        this.testURI = testURI;
        this.report = report;
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.fireTestStarted(description);
        try {
            testCase.run();
            if ( report != null )
                report.success(testURI);
        } catch (AssumptionViolatedException e) {
            notifier.fireTestAssumptionFailed(new Failure(description, e));
//        } catch (AssertionFailedError ex) {
//            // JUnit assertion or fail()
//            if ( report != null )
//                report.failure(testURI);
//            notifier.fireTestFailure(new Failure(description, ex));
        } catch (Throwable ex) {
            if ( report != null )
                report.failure(testURI);
            notifier.fireTestFailure(new Failure(description, ex));
        } finally {
            notifier.fireTestFinished(description);
        }
    }
}
