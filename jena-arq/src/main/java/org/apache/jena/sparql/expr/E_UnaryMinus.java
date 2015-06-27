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

import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.sse.Tags ;

public class E_UnaryMinus extends ExprFunction1
{
    private static final String functionName = Tags.tagUnaryMinus ;
    private static final String symbol = Tags.symMinus ;

    public E_UnaryMinus(Expr expr)
    {
        super(expr, functionName, symbol) ;
    }
    
    @Override
    public NodeValue eval(NodeValue v) { return XSDFuncOp.unaryMinus(v) ; }
    
    @Override
    public Expr copy(Expr expr) { return new E_UnaryMinus(expr) ; } 
}
