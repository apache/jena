/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.test;

import static org.openjena.atlas.lib.RandomLib.random ;

import java.util.Arrays;


/** Support for testing BTrees */

public class Gen
{
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