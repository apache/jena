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

package org.apache.jena.sparql.expr.aggregate;

import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;

public class AggMinDistinct extends AggMinBase
{
    // ---- MIN(DISTINCT expr)
    public AggMinDistinct(Expr expr) { super(expr, true) ; } 
    @Override
    public Aggregator copy(ExprList exprs) { return new AggMinDistinct(exprs.get(0)) ; }
    @Override
    public int hashCode()   { return HC_AggMinDistinct ^ getExpr().hashCode() ; }
    
    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ; 
        if ( ! ( other instanceof AggMinDistinct ) )
            return false ;
        AggMinDistinct agg = (AggMinDistinct)other ;
        return exprList.equals(agg.exprList, bySyntax) ;
    } 
}
