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

package com.hp.hpl.jena.sparql.expr;

import java.util.List ;

import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.sse.Tags;

/** SPARQL CONCATs */

public class E_StrConcat extends ExprFunctionN
{
    private static final String name = Tags.tagConcat ;
    
    public E_StrConcat(ExprList args)
    {
        super(name, args) ;
    }

    @Override
    public Expr copy(ExprList newArgs)
    {
        return new E_StrConcat(newArgs) ;
    }

    @Override
    public NodeValue eval(List<NodeValue> args)
    { 
        return XSDFuncOp.strConcat(args) ;
    }
}
