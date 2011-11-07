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
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

public class AggMaxDistinct extends AggMaxBase
{
    // ---- MAX( DISTINCT expr)
    public AggMaxDistinct(Expr expr) { super(expr) ; } 
    @Override
    public Aggregator copy(Expr expr) { return new AggMaxDistinct(expr) ; }

    @Override
    public String toString() { return "max(distinct "+ExprUtils.fmtSPARQL(getExpr())+")" ; }
    @Override
    public String toPrefixString() { return "(max distinct "+WriterExpr.asString(getExpr())+")" ; }

    @Override
    public int hashCode()   { return HC_AggMaxDistinct ^ expr.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ; 
        if ( ! ( other instanceof AggMaxDistinct ) )
            return false ;
        AggMaxDistinct agg = (AggMaxDistinct)other ;
        return expr.equals(agg.expr) ;
    }

}
