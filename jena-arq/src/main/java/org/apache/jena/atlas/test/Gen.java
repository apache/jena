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

import static org.apache.jena.atlas.lib.RandomLib.random ;

import java.util.Arrays ;


/** Support for testing B+Trees/BTrees.  May be  generally useful*/

public class Gen
{
    static boolean DIFFERENT = false ;
    /** Generate a random sequence between low (inclusive) and high (exclusive) */ 
    public static int[] rand(int numRand, int low, int high)
    {
        int[] k = new int[numRand] ;
        Arrays.fill(k, -1) ;
        for ( int i = 0 ; i < numRand ; i++ )
        {
            if ( DIFFERENT )
            {
                loop1: while(true)
                {
                    // All different.
                    int x = random.nextInt(high-low)+low ;
                    for ( int j = 0 ; j < i ; j++ )
                    {
                        if ( k[j] == x )
                            continue loop1 ;
                    }
                    k[i] = x ;
                    break ;
                }
            }
            else
            {
                int x = random.nextInt(high-low)+low ;
                k[i] = x ;
            }
            
        }
        
        return k ;
    }

    /** Sort-of jumble a sequence */
    public static int[] permute(int[] x, int num)
    {
        // Collections.shuffle.
        int[] x2 = new int[x.length] ;
        System.arraycopy(x, 0, x2, 0, x.length) ;
        
        for (int i = 0 ; i < num ; i++ )
        {
            int a = random.nextInt(x2.length) ;
            int b = random.nextInt(x2.length) ;
            int t = x2[a] ;
            x2[a] = x2[b] ;
            x2[b] = t ;
        }
        // Checking.
        for ( int k : x )
        {
            boolean found = false ;
            for ( int k2 : x2 )
                if ( k == k2 )
                {
                    found = true ;
                    break ;
                }
            if ( ! found )
                System.err.printf("Corrupted permute: [%s] [%s]\n", strings(x) , strings(x2)) ;
        }
        return x2 ;
    }
    
    public static String strings(int[] keys)
    {
        StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for ( int k : keys)
        {
            if ( ! first )
                sb.append(", ") ;
            first = false ;
            sb.append(k) ;
        }
        return sb.toString() ;
    }
 }
