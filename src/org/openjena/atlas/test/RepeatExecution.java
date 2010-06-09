/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.test;


public class RepeatExecution
{
    public static void repeatExecutions(ExecGenerator execGenerator, int iterations, boolean showProgress)
    {
        // Output control
        int dots = 10 ;
        int lines = 500 ;
        if ( iterations > 10000 )
        {
            dots = 10*dots ;
            lines = 10*lines ;
        }
        
        long start = System.currentTimeMillis() ;
        int successes = 0 ;
        int failures = 0 ;
        
        boolean eol = true;
        for ( int i = 0 ; i < iterations ; i++ )
        {
            if ( showProgress && i%lines == 0 )
            {
                eol = true ;
                if ( i != 0 ) 
                    System.out.println() ;
                System.out.printf("%-6d: ", i) ;
            }
            else
                eol = false ;
            
            if ( showProgress && i%dots == 0 )
                System.out.print(".") ;
            try {
                execGenerator.executeOneTest();
                successes ++ ;
            } catch (Exception ex)
            { 
                ex.printStackTrace(System.err) ;
                failures ++ ;
                if ( failures >= 1 )
                    break ; 
            }
            
        }
        long finish = System.currentTimeMillis() ;
        
        if ( showProgress && ! eol )
            System.out.println() ;
        
        //System.out.println() ;
        System.out.printf("Successes = %d : Failures = %d\n", successes, failures) ;
        double x = (finish-start)/1000.0 ;
        System.out.printf("Time = %.2fs; avg = %.4fs\n", x, x/iterations) ;
    }

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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