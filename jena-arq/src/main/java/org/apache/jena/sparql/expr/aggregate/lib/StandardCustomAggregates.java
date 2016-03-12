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

import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory ;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry ;

/** Register custom aggregates.
 * This includes the statistics aggregate functions as custom aggregates, 
 * as {@code <http://jena.apache.org/ARQ/function#stdev} etc.
 * This is commonly abbreviated {@code afn:stdev}.
 */ 
public class StandardCustomAggregates {

    public static String BASE = ARQConstants.ARQAggregateLibraryURI ;
    public static String BASE2 = ARQConstants.ARQFunctionLibraryURI ;   

    public static void register() {
        // The statistics aggregates
        AccumulatorFactory f_VarP =     (agg, distinct) -> new AccStatVarPopulation(agg.getExpr(), distinct) ;
        AccumulatorFactory f_Var =      (agg, distinct) -> new AccStatVarSample(agg.getExpr(), distinct) ;
        AccumulatorFactory f_StdevP =   (agg, distinct) -> new AccStatStdDevPopulation(agg.getExpr(), distinct) ;
        AccumulatorFactory f_Stdev =    (agg, distinct) -> new AccStatStdDevSample(agg.getExpr(), distinct) ;
        
        AggregateRegistry.register(BASE+"stdevp",   f_StdevP, null) ;
        AggregateRegistry.register(BASE+"stdev",    f_Stdev, null) ;
        AggregateRegistry.register(BASE+"varp",     f_VarP, null) ;
        AggregateRegistry.register(BASE+"var",      f_Var, null) ;
        
        // Again in the afn@ (not encouraged but easy to do)
        AggregateRegistry.register(BASE2+"stdevp",  f_StdevP, null) ;
        AggregateRegistry.register(BASE2+"stdev",   f_Stdev, null) ;
        AggregateRegistry.register(BASE2+"varp",    f_VarP, null) ;
        AggregateRegistry.register(BASE2+"var",     f_Var, null) ;

        // DISTINCT versions as URIs.
        AccumulatorFactory f_VarP_d =     (agg, distinct) -> new AccStatVarPopulation(agg.getExpr(), true) ;
        AccumulatorFactory f_Var_d =      (agg, distinct) -> new AccStatVarSample(agg.getExpr(), true) ;
        AccumulatorFactory f_StdevP_d =   (agg, distinct) -> new AccStatStdDevPopulation(agg.getExpr(), true) ;
        AccumulatorFactory f_Stdev_d =    (agg, distinct) -> new AccStatStdDevSample(agg.getExpr(), true) ;

        AggregateRegistry.register(BASE+"varpd",    f_VarP_d, null) ;
        AggregateRegistry.register(BASE+"vard",     f_Var_d, null) ;
        AggregateRegistry.register(BASE+"stdevpd",  f_StdevP_d, null) ;
        AggregateRegistry.register(BASE+"stdevd",   f_Stdev_d, null) ;

    }
}
