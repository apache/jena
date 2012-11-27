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

package org.apache.jena.atlas.test;

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
