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

package org.apache.jena.atlas.junit;

import java.io.PrintStream ;

import org.junit.internal.TextListener ;
import org.junit.runner.Description ;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure ;

/** JUnit4 test listener that prints one line per test */
public class TextListenerLong extends TextListener
{
    private PrintStream out ;
    private int manifestCount;

    public TextListenerLong(PrintStream writer, int manifestCount) {
        super(writer) ;
        this.out = writer ;
        this.manifestCount = manifestCount;
    }

    @Override
    public void testRunStarted(Description description) {}

    @Override
    public void testStarted(Description description) {
        out.println(description.getDisplayName());
    }

    @Override
    public void testFailure(Failure failure) {
        out.println("Error: "+ failure.getMessage());
    }

    @Override
    public void testIgnored(Description description) {
        out.println("  Ignored");
    }

    @Override
    protected void printFailure(Failure each, String prefix) {
        out.println(prefix + ") " + each.getTestHeader());
        // Don't print stack trace.
        // Stack traces are printed when the test runs.
        //out.print(each.getTrimmedTrace());
    }

    // TextListener does not expose getWriter()
    private PrintStream getWriter() {
        return this.out;
    }

    @Override
    protected void printFooter(Result result) {
        //super.printFooter(result);

        int testCount = result.getRunCount();
        // Adjust for manifests.
        if ( manifestCount > 0 )
            testCount = testCount - manifestCount;

        // Copied from super, modified to count tests, not manifests.
        if (result.wasSuccessful()) {
            getWriter().println();
            getWriter().print("OK");
            getWriter().println(" (" + testCount + " test" + (testCount == 1 ? "" : "s") + ")");

        } else {
            getWriter().println();
            getWriter().println("FAILURES!!!");
            getWriter().println("Tests run: " + testCount + ",  Failures: " + result.getFailureCount());
        }
        getWriter().println();
    }
}
