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
import java.text.NumberFormat ;
import java.util.List ;

import org.junit.runner.Description ;
import org.junit.runner.Result ;
import org.junit.runner.notification.Failure ;
import org.junit.runner.notification.RunListener ;

class TextListenerOneLine extends RunListener 
{
    // See also org.junit.internal.TextListener
    // TextListener does not provide access to the writer (!!)
    
    protected Description current = null ;
    protected PrintStream fWriter ;

    public TextListenerOneLine(PrintStream writer) { this.fWriter = writer ; }
    
    @Override
    public void testStarted(Description description) {
        current = description ;
        fWriter.println(description.getMethodName()) ;
    }
    
    @Override
    public void testFinished(Description description)
    {
        current = null ;
    }

    @Override
    public void testFailure(Failure failure)
    {
        if ( failure.getMessage() != null && failure.getMessage().length() > 0 )
            fWriter.println("**** Failure: "+failure.getMessage());
        else 
            fWriter.println("**** Failure") ;
    }

    @Override
    public void testIgnored(Description description) {
        fWriter.println("** Ignored");
    }
    
    @Override
    public void testRunFinished(Result result) {
        printHeader(result.getRunTime());
        printFailures(result);
        printFooter(result);
    }
    protected void printHeader(long runTime) {
        fWriter.println();
        fWriter.println("Time: " + elapsedTimeAsString(runTime));
        
    }

    protected void printFailures(Result result) {
        List<Failure> failures= result.getFailures();
        if (failures.size() == 0)
            return;
        if ( result.getFailureCount() > 0 )
        {
            fWriter.println() ;
            fWriter.println("===========================================") ;
        }
        printSummary(result) ;
        
        int i = 1;
        for (Failure each : failures)
            printFailure(each, "" + i++);
    }
    
    protected void printSummary(Result result)
    {
        int badCount =  result.getFailureCount() ;
        int ignoredCount = result.getIgnoreCount() ;
        int goodCount = result.getRunCount() - badCount - ignoredCount ;
        
        fWriter.print("Tests = "+result.getRunCount()) ;
        fWriter.print(" : Successes = "+goodCount) ;
        if ( ignoredCount > 0 )
            fWriter.print(" : Ignored = "+ignoredCount) ;
        fWriter.print(" : Failures = "+badCount) ;
        fWriter.println() ;
        fWriter.println() ;
    }
    
    protected void printFailure(Failure f, String prefix) {
        System.out.print("Failure: ") ;
        System.out.print(prefix) ;
        System.out.print(" : ") ;
        //System.out.println(f.getDescription().getMethodName()) ;
        System.out.println(f.getDescription().getDisplayName()) ;
        System.out.println(f.getException()) ;
        // Truncate at : java.lang.reflect.Method.invoke
        f.getException().printStackTrace(System.out) ;
    }
    
    protected void printFooter(Result result) 
    {
        if ( result.getFailureCount() > 0 )
            printSummary(result) ;
    }
    
    protected String elapsedTimeAsString(long runTime) {
        return NumberFormat.getInstance().format((double) runTime / 1000);
    }
}
