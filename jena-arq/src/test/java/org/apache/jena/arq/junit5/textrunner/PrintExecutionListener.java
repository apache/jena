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

package org.apache.jena.arq.junit5.textrunner;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import org.apache.jena.atlas.io.IndentedWriter;

class PrintExecutionListener implements TestExecutionListener {

        // Skips outer containers:
        private int skip = 3;
        private int depth = 0 ;

        private int testCount = 0 ;
        private int containerCount = 0 ;

        private int errors = 0 ;
        private int successful = 0 ;
        private final IndentedWriter out;

        public PrintExecutionListener(IndentedWriter out) {
            this.out = out;
        }

        @Override
        public void testPlanExecutionStarted(TestPlan testPlan) {
            out.flush();
        }

        @Override
        public void testPlanExecutionFinished(TestPlan testPlan) {
//            out.println();
//            out.println("Containers: "+containerCount);
//            out.println("Successes:  "+successful);
//            out.println("Errors:     "+errors);
            out.flush();
        }

        @Override
        public void executionStarted(TestIdentifier testIdentifier) {
            // Container vs Test?
            // Not in walk order

            if ( testIdentifier.isContainer() ) {
                depth++;
                if ( depth > skip ) {
                    containerCount++;
                    out.println(testIdentifier.getDisplayName());
                    out.incIndent();
                }
            } else {
                testCount++;
            }
        }

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {

            if ( testIdentifier.isContainer() ) {
                if ( depth > skip )
                    out.decIndent();
                depth--;
            } else {
                out.printf("%d [%s] %s\n", testCount, result.getStatus().name().substring(0, 1), testIdentifier.getDisplayName());
                // Or summary
                switch(result.getStatus()) {
                    case ABORTED->{}
                    case FAILED ->  errors++;
                    case SUCCESSFUL -> successful++;
                }
            }
        }
    }