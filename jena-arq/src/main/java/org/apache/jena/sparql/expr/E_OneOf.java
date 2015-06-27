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

import java.util.List ;

import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.sse.Tags ;

public class E_OneOf extends E_OneOfBase
{

    private static final String functionName = Tags.tagIn ;
    
    public E_OneOf(Expr expr, ExprList args)
    {
        super(functionName, expr, args) ;
    }
    
    protected E_OneOf(ExprList args)
    {
        super(functionName, args) ;
    }

    @Override
    public NodeValue evalSpecial(Binding binding, FunctionEnv env)
    {
        boolean b = super.evalOneOf(binding, env) ;
        return NodeValue.booleanReturn(b) ;
    }
    
    @Override
    public NodeValue eval(List<NodeValue> args)
    { throw new ARQInternalErrorException() ; }
    
    @Override
    public Expr copy(ExprList newArgs)
    {
        return new E_OneOf(newArgs) ;
    }
}
