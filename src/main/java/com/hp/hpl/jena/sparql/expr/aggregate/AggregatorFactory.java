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

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.atlas.logging.Log ;

public class AggregatorFactory
{
    public static Aggregator createCount(boolean distinct)
    { 
        return distinct ? new AggCountDistinct() : new AggCount() ;
    }

    public static Aggregator createCountExpr(boolean distinct, Expr expr)
    { 
        return distinct ? new AggCountVarDistinct(expr) : new AggCountVar(expr) ;
    }
    
    public static Aggregator createSum(boolean distinct, Expr expr)
    { 
        return distinct ? new AggSumDistinct(expr) : new AggSum(expr) ;
    }
    
    public static Aggregator createMin(boolean distinct, Expr expr)
    { 
        // Only remember it's DISTINCT for getting the printing right.
        return distinct ? new AggMinDistinct(expr) : new AggMin(expr) ;
    }
    
    public static Aggregator createMax(boolean distinct, Expr expr)
    { 
        return distinct ? new AggMaxDistinct(expr)  : new AggMax(expr) ;
    }
    
    public static Aggregator createAvg(boolean distinct, Expr expr)
    { 
        return distinct ? new AggAvgDistinct(expr) : new AggAvg(expr) ;
    }
        
    public static Aggregator createSample(boolean distinct, Expr expr)
    { 
        return distinct ? new AggSampleDistinct(expr) : new AggSample(expr) ;
    }
    
    public static Aggregator createGroupConcat(boolean distinct, Expr expr, String separator, ExprList orderedBy)
    { 
        if ( orderedBy != null && ! orderedBy.isEmpty())
            throw new NotImplemented("GROUP_CONCAT / ORDER BY not implemented yet") ;        
        return distinct ? new AggGroupConcatDistinct(expr, separator) : new AggGroupConcat(expr, separator) ;
    }
    
    public static Aggregator createAggNull() { return new AggNull() ; }
    
    private static Aggregator err(String label)
    {
        Log.fatal(AggregatorFactory.class, "Not implemented: "+label) ;
        return null ;
    }
}
