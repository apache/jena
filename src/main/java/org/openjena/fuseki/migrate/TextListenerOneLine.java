/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.migrate;

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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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