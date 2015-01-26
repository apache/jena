/*
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

package org.seaborne.dboe.sparql.test;

import java.util.Enumeration ;

import junit.framework.TestFailure ;
import junit.framework.TestResult ;
import org.junit.runner.Description ;
import org.junit.runner.Runner ;
import org.junit.runner.notification.Failure ;
import org.junit.runner.notification.RunNotifier ;

import com.hp.hpl.jena.sparql.junit.EarlTestCase ;

/** Run a single SPARQL test. */
public class RunnerOneSPARQLTest extends Runner
{
    static int count = 0 ;
    // We use JUnit3 TestCase so as to work with the existing QueryTest etc
    // If that is retired, then create a "TestSPARQL".
    private Description description ;
    private EarlTestCase testCase ;
    
    public RunnerOneSPARQLTest(EarlTestCase test) {
        int count$ = (++count) ;
        testCase = test ;
        // Names must be unique else Eclipse will not report them. 
        description = Description.createTestDescription(RunnerOneSPARQLTest.class, "T-"+count$+": "+test.getName()) ;
    }
    
    @Override
    public Description getDescription() {
        return description ;
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.fireTestStarted(description) ;
        
        try {
            // testCase.
            testCase.setUpTest() ;
            try {
                TestResult r = testCase.run() ;
                // Junit3 to Junit4.
                Enumeration<TestFailure> en = r.errors() ;
                for (; en.hasMoreElements();) {
                    throw en.nextElement().thrownException() ;
                }
                en = r.failures() ;
                for (; en.hasMoreElements();) {
                    throw en.nextElement().thrownException() ;
                }
            } finally {
                testCase.tearDownTest() ;
            }

        } catch (Exception e) {
            notifier.fireTestAssumptionFailed(new Failure(description, e)) ;
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(description, e)) ;
        } finally {
            notifier.fireTestFinished(description); 
        }        
    }

}
