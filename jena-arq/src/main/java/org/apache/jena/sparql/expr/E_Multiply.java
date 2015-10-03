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

import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.expr.nodevalue.NodeValueOps ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.sse.Tags ;

public class E_Multiply extends ExprFunction2
{
    private static final String functionName = Tags.tagMultiply ;
    private static final String symbol = Tags.symMult ;
    
    public E_Multiply(Expr left, Expr right)
    {
        super(left, right, functionName, symbol) ;
    }
    
    @Override
    public NodeValue eval(NodeValue x, NodeValue y)    {
        if ( ARQ.isStrictMode() )
            return XSDFuncOp.numMultiply(x, y) ;

        return NodeValueOps.multiplicationNV(x, y) ;
    }
    
    @Override
    public Expr copy(Expr e1, Expr e2) {  return new E_Multiply(e1 , e2 ) ; }
}
