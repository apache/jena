/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import junit.framework.*;
import junit.runner.*;
import java.io.*;
import java.util.*;


/** apps.SimpleRunnerAlt
 * 
 * @author Andy Seaborne
 * @version $Id: SimpleTestRunner.java,v 1.4 2007/01/02 11:20:20 andy_seaborne Exp $
 */

public class SimpleTestRunner extends BaseTestRunner
{
    public SimpleTestRunner()
    {
        super();
    }

    public void testStarted(String arg0) { }
    public void testEnded(String arg0)   { }
    public void testFailed(int arg0, Test test, Throwable t) {}
    protected void runFailed(String arg0) { }

    
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
        
        for ( Enumeration e = result.errors() ; e.hasMoreElements() ; )
        {
            out.println() ;
            TestFailure failure = (TestFailure)e.nextElement() ;
            out.println("Error:    "+failure.toString()) ;
        }
        for ( Enumeration e = result.failures() ; e.hasMoreElements() ; )
        {
            out.println() ;
            TestFailure failure = (TestFailure)e.nextElement() ;
            out.println("Failure:  "+failure.toString()) ;
        }
    }
    
    static class Listener implements TestListener
    {
        Listener() {}
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
        
        public void endTest(Test test) { }
        
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
        public void addError(Test test, Throwable arg1) {}
        public void addFailure(Test test, AssertionFailedError arg1) {}
        public void endTest(Test test) { }
        public void startTest(Test test) { }
    }

}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */