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

package org.apache.jena.sparql.expr.aggregate ;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;

public class AggregatorFactory {
    public static Aggregator createCount(boolean distinct) {
        return distinct ? new AggCountDistinct() : new AggCount() ;
    }

    public static Aggregator createCountExpr(boolean distinct, Expr expr) {
        return distinct ? new AggCountVarDistinct(expr) : new AggCountVar(expr) ;
    }

    public static Aggregator createSum(boolean distinct, Expr expr) {
        return distinct ? new AggSumDistinct(expr) : new AggSum(expr) ;
    }

    public static Aggregator createMin(boolean distinct, Expr expr) {
        // Only remember it's DISTINCT for getting the printing right.
        return distinct ? new AggMinDistinct(expr) : new AggMin(expr) ;
    }

    public static Aggregator createMax(boolean distinct, Expr expr) {
        return distinct ? new AggMaxDistinct(expr) : new AggMax(expr) ;
    }

    public static Aggregator createAvg(boolean distinct, Expr expr) {
        return distinct ? new AggAvgDistinct(expr) : new AggAvg(expr) ;
    }

    public static Aggregator createMedian(boolean distinct, Expr expr) {
        return distinct ? new AggMedianDistinct(expr) : new AggMedian(expr) ;
    }

    public static Aggregator createMode(boolean distinct, Expr expr) {
        return distinct ? new AggModeDistinct(expr) : new AggMode(expr) ;
    }

    public static Aggregator createSample(boolean distinct, Expr expr) {
        return distinct ? new AggSampleDistinct(expr) : new AggSample(expr) ;
    }

    public static Aggregator createGroupConcat(boolean distinct, Expr expr, String separator, ExprList orderedBy) {
        if ( orderedBy != null && !orderedBy.isEmpty() )
            throw new NotImplemented("GROUP_CONCAT / ORDER BY not implemented yet") ;
        return distinct ? new AggGroupConcatDistinct(expr, separator) : new AggGroupConcat(expr, separator) ;
    }

    public static Aggregator createAggNull() {
        return new AggNull() ;
    }

    public static Aggregator createFold(Expr expr1, String typeIRI1, Expr expr2, String typeIRI2) {
        return new AggFold(expr1, typeIRI1, expr2, typeIRI2) ;
    }

    public static Aggregator createCustom(String iri, Args a) {
        return createCustom(iri, a.distinct, ExprList.copy(a)) ;
    }
    
    public static Aggregator createCustom(String iri, boolean distinct, Expr expr) {
        return createCustom(iri, distinct, new ExprList(expr)) ;
    }
    
    public static Aggregator createCustom(String iri, boolean distinct, ExprList exprs) {
        if ( ! AggregateRegistry.isRegistered(iri) )
            Log.warn(AggregatorFactory.class, "Not registered: custom aggregate <"+iri+">") ;
        return new AggCustom(iri, distinct, exprs) ;
    }
}
