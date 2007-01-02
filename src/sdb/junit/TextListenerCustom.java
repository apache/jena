/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.junit;
import java.io.PrintStream;

import org.junit.internal.runners.TextListener;
import org.junit.runner.notification.Failure;

// Enable tweaking of the output format.
// If getWriter() were not private ...

public class TextListenerCustom extends TextListener
{
    private final PrintStream fWriter;
    static final int maxStackTrace = 10 ;
    
    public TextListenerCustom() {
        this(System.out);
    }

    public TextListenerCustom(PrintStream writer) {
        super(writer) ;
        this.fWriter= writer;
    }
    @Override
    protected void printFailureTrace(Failure failure) {
        //getWriter().print(failure.getTrace());
        fWriter.println(failure.getMessage()) ;

        Throwable th = failure.getException() ;
        if ( ! ( th instanceof java.lang.AssertionError ) )
        {            
            printFailureTrace(failure, 0, maxStackTrace) ;
            return ;
        }
         
        // It was an assert failure.
        // Rather than a full stacktrace, we print the first non-JUnit point.
        StackTraceElement[] stackTrace = th.getStackTrace() ;
        StackTraceElement el = null ;
        int start = 0 ;
        for ( start = 0 ; start < stackTrace.length ; start++ )
        {
            el = stackTrace[start] ;
            if ( ! el.getClassName().equals("org.junit.Assert") )
                break ;
        }
        printFailureTrace(failure, start, start+maxStackTrace) ;
    }

    private void printFailureTrace(Failure failure, int start, int finish)
    {
        StackTraceElement[] stackTrace = failure.getException().getStackTrace() ;
        if ( finish > stackTrace.length )
            finish = stackTrace.length ;
        
        for ( int i = start ; i < finish  ; i++ )
        {
            StackTraceElement el = stackTrace[i] ;
            fWriter.println("    "+el.toString()) ;
        }
    }
}


/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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
