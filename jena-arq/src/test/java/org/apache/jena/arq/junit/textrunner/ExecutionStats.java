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

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

class ExecutionStats implements TestExecutionListener {

    // Skips outer containers:
    private int skip = 3;
    private int depth = 0 ;

    private int testCount = 0 ;
    private int containerCount = 0 ;

    private int errors = 0 ;
    private int successful = 0 ;

    public ExecutionStats() { }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        // Not in walk order
        if ( testIdentifier.isContainer() ) {
            depth++;
            if ( depth > skip )
                containerCount++;
        } else {
            testCount++;
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
        if ( testIdentifier.isContainer() ) {
            depth--;
        } else {
            // Summary
            switch(result.getStatus()) {
                case ABORTED->{}
                case FAILED ->  errors++;
                case SUCCESSFUL -> successful++;
            }
        }
    }

    //@formatter:off
    public int getContainerCount()  { return containerCount; }
    public int getTestCount()       { return testCount; }
    public int getTestPasses()      { return successful; }
    public int getTestFailures()    { return errors; }
    //@formatter:on
}