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
package org.apache.jena.sparql.expr;

import java.util.List;

import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.sse.Tags;

/** Do any of FN_Adjust(date/time)ToTimezone */
public class E_AdjustToTimezone extends ExprFunctionN {

    public E_AdjustToTimezone(Expr expr1, Expr expr2){
        super(Tags.tagAdjust, expr1, expr2);
    }

    @Override
    public Expr copy(ExprList newArgs) {
        return new E_AdjustToTimezone(super.getArg(0), super.getArg(1));
    }

    @Override
    public NodeValue eval(List<NodeValue> args)
    {
        if ( args.size() != 1 && args.size() != 2 )
            throw new ExprEvalException("ADJUST: Wrong number of arguments: "+args.size()+" : [wanted 1 or 2]") ;

        NodeValue v1 = args.get(0) ;
        if ( !v1.isDateTime() && !v1.isDate() && !v1.isTime() )
            throw new ExprEvalException("ADJUST: Not an xsd:dateTime, xsd:date or xsd:time : " + v1);

        if ( args.size() == 2 ) {
            NodeValue v2 = args.get(1) ;
            return XSDFuncOp.adjustToTimezone(v1, v2) ;
        }

        return XSDFuncOp.adjustToTimezone(v1, null) ;
    }
}
