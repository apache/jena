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

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry;

/** Register custom aggregates in addition to the SPARQL defined ones.
 * This includes the statistics aggregate functions as custom aggregates, 
 * as {@code <http://jena.apache.org/ARQ/function#stdev} etc.
 * This is commonly abbreviated {@code afn:stdev}.
 */ 
public class StandardAggregates {
    
    public static String BASE = ARQConstants.ARQAggregateLibraryURI;
    public static String BASE2 = ARQConstants.ARQFunctionLibraryURI;   

    public static void register() {
        // PGSQL names:
//        stddev(expression)
//        stddev_pop(expression)
//        stddev_samp(expression)
//        variance(expression)
//        var_pop(expression)
//        var_samp(expression)
        
//        |  < STDEV:       "stdev" >
//        |  < STDEV_SAMP:  "stdev_samp" >
//        |  < STDEV_POP:   "stdev_pop" >
//        |  < VARIANCE:    "variance" >
//        |  < VAR_SAMP:    "var_samp" >
//        |  < VAR_POP:     "var_pop" >
       
        // The statistics aggregates
        // = stddev-samp except one element -> 0. ?????????????
        AccumulatorFactory f_Stdev      =  (agg, distinct) -> new AccStatStdDevSample(agg.getExpr(), distinct);
        AccumulatorFactory f_StdevSamp  =  (agg, distinct) -> new AccStatStdDevSample(agg.getExpr(), distinct);
        AccumulatorFactory f_StdevPop   =  (agg, distinct) -> new AccStatStdDevPopulation(agg.getExpr(), distinct);
        AccumulatorFactory f_VarPop     =  (agg, distinct) -> new AccStatVarPopulation(agg.getExpr(), distinct);
        AccumulatorFactory f_VarSamp    =  (agg, distinct) -> new AccStatVarSample(agg.getExpr(), distinct);
        
        r(AggURI.stdev,        f_Stdev);
        r(AggURI.stdev_samp,   f_StdevSamp);
        r(AggURI.stdev_pop,    f_StdevPop);
        r(AggURI.variance,     f_VarSamp);
        r(AggURI.var_samp,     f_VarSamp);
        r(AggURI.var_pop,      f_VarPop);
    }
    
    private static void r(String uri, AccumulatorFactory f) {
        AggregateRegistry.register(uri, f, null);
        // Again in the afn@ (not encouraged but easy to do)
        AggregateRegistry.register(uri.replace("/aggregate", ""), f, null);
    }
}
