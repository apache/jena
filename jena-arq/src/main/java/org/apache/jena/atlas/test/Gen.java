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

import java.util.* ;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

/** Support for testing.  May be  generally useful */
public class Gen
{
    /** Generate a random sequence between low (inclusive) and high (exclusive): may include duplicates. */ 
    public static int[] rand(int numRand, int low, int high) {
        return rand(numRand, low, high, false) ; 
    }
    
    /**
     * Generate a random sequence between low (inclusive) and high (exclusive) - with duplicates or not
     */
    public static int[] rand(int numRand, int low, int high, boolean allDifferent) {
        Set<Integer> used = new HashSet<>();
        IntSupplier supplier = allDifferent ? () -> {
            int x = oneRandomInt(low, high);
            while (!used.add(x)) x = oneRandomInt(low, high);
            return x;
        } : () -> oneRandomInt(low, high);

        return IntStream.generate(supplier).limit(numRand).toArray();
    }

    private static int oneRandomInt(int low, int high) {
        return random.nextInt(high - low) + low;
    }

    /** Pull items out of the list in a random order */ 
    public static int[] permute(int[] x) {
        int[] x2 = Arrays.copyOf(x, x.length);
        Collections.shuffle(Arrays.asList(x2), random);
        return x2; 
    }
    
    /** Do a number of random pair-wise swaps */
    public static int[] shuffle(int[] x, int num) {
        // Collections.shuffle.
        int[] x2 = Arrays.copyOf(x, x.length) ;
        for ( int i = 0 ; i < num ; i++ ) {
            int a = random.nextInt(x2.length) ;
            int b = random.nextInt(x2.length) ;
            int t = x2[a] ;
            x2[a] = x2[b] ;
            x2[b] = t ;
        }
        return x2 ;
    }

    public static String strings(int[] keys) {
        StringJoiner joiner = new StringJoiner(",");
        for ( int k : keys ) joiner.add(Integer.toString(k));
        return joiner.toString();
    }
 }
