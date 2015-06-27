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

import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.sparql.sse.Tags ;

public class E_SameTerm extends ExprFunction2
{
    private static final String functionName = Tags.tagSameTerm ;

    public E_SameTerm(Expr left, Expr right)
    {
        super(left, right, functionName) ;
    }
    
    @Override
    public NodeValue eval(NodeValue x, NodeValue y)
    {
        return NodeFunctions.sameTerm(x, y) ;
    }
    
    @Override
    public Expr copy(Expr e1, Expr e2) {  return new E_SameTerm(e1 , e2 ) ; }
}
