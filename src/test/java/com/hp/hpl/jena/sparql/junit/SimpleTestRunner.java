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

package com.hp.hpl.jena.sparql.junit;

import java.io.PrintStream ;
import java.util.Enumeration ;

import junit.framework.AssertionFailedError ;
import junit.framework.Test ;
import junit.framework.TestCase ;
import junit.framework.TestFailure ;
import junit.framework.TestListener ;
import junit.framework.TestResult ;
import junit.runner.BaseTestRunner ;


/** Simple, text output, test runner */

public class SimpleTestRunner extends BaseTestRunner
{
    public SimpleTestRunner()
    {
        super();
    }

    @Override
    public void testStarted(String arg0) { }
    @Override
    public void testEnded(String arg0)   { }
    @Override
    public void testFailed(int arg0, Test test, Throwable t) {}
    @Override
    protected void runFailed(String arg0) { }


    static public TestResult runSilent(Test ts)
    {
        TestResult result = new TestResult() ;
        result.addListener(new SilentListener()) ;
        ts.run(result) ;
        return result ;
    }
    
    static public TestResult runNoReport(Test ts)
    {
        TestResult result = new TestResult() ;
        //result.addListener(new SilentListener()) ;
        result.addListener(new Listener()) ;
        ts.run(result) ;
        return result ;
    }
    
    static public void runAndReport(Test ts) { runAndReport(ts, null) ; }

    static public void runAndReport(Test ts, PrintStream out)
    {
        if ( out == null )
            out = System.out ;
        TestResult result = runNoReport(ts) ;
        if ( result.errorCount() > 0 || result.failureCount() > 0 )
        {
            out.println() ;
            out.println("===========================================") ;
        }
        int goodCount = result.runCount() - result.errorCount() - result.failureCount() ;
        out.println("Tests = "+result.runCount()+
                           " : Successes = "+goodCount+
                           " : Errors = "+result.errorCount()+
                           " : Failures = "+result.failureCount()) ;
        
        for ( Enumeration<?> e = result.errors() ; e.hasMoreElements() ; )
        {
            out.println() ;
            TestFailure failure = (TestFailure)e.nextElement() ;
            out.println("Error:    "+failure.toString()) ;
        }
        for ( Enumeration<?> e = result.failures() ; e.hasMoreElements() ; )
        {
            out.println() ;
            TestFailure failure = (TestFailure)e.nextElement() ;
            out.println("Failure:  "+failure.toString()) ;
        }
    }
    
    static class Listener implements TestListener
    {
        Listener() {}
        @Override
        public void addError(Test test, Throwable arg1)
        {
            System.out.println("** Error:    "+test) ;
            if ( arg1 != null )
            {
                if ( arg1.getMessage() != null )
                    System.out.println("  "+arg1.getMessage()) ;
                StackTraceElement st = arg1.getStackTrace()[0] ;
                System.out.println(st) ;
            }
        }
        
        @Override
        public void addFailure(Test test, AssertionFailedError arg1)
        { 
            System.out.println("** Failure:  "+test);
            if ( arg1 != null )
            {
                if ( arg1.getMessage() != null )
                    System.out.println("  "+arg1.getMessage()) ;
                StackTraceElement st = arg1.getStackTrace()[0] ;
                System.out.println(st) ;
            }
        }
        
        @Override
        public void endTest(Test test) { }
        
        @Override
        public void startTest(Test test)
        { 
            // Compensate for TestCase.toString() adding "(class)" to the end of the name
            String name = "" ;
            if ( test instanceof TestCase )
                name = ((TestCase)test).getName() ;
            else
                name = test.toString() ;
            System.out.println("Test: "+name) ; }
    }
    
    static class SilentListener implements TestListener
    {
        SilentListener() {}
        @Override
        public void addError(Test test, Throwable arg1) {}
        @Override
        public void addFailure(Test test, AssertionFailedError arg1) {}
        @Override
        public void endTest(Test test) { }
        @Override
        public void startTest(Test test) { }
    }

}
