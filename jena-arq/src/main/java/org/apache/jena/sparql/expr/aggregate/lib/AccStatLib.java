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

package org.apache.jena.sparql.expr.aggregate.lib;

import org.apache.jena.sparql.expr.ExprEvalException;

public class AccStatLib {
    /** Calculate the variance (sample) */ 
    public static double calcVarianceSample(double sumSquared, double sum, long count) {
        // (N*sum(?x*?x) - sum(?x) ) / N*(N-1) 
        return calcVariance$(sumSquared, sum, count, count-1);
    }

    /** Calculate the variance (population) */ 
    public static double calcVariancePopulation(double sumSquared, double sum, long N) {
        return calcVariance$(sumSquared, sum, N, N);
    }
    
    // Engine.
    static private double calcVariance$(double sumSquared, double sum, long N, long N1) {
//        System.out.printf("sum = %f, sumSq = %f, N=%d\n", sum, sumSquared, N);
        if ( N <= 0 )
            throw new ExprEvalException("N= "+N);
        if ( N1 == 0 )
            throw new ExprEvalException("Sample size one");
        double x = sumSquared - (sum*sum)/N;
        x = x / N1;
        return x;
    }
}
