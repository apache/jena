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

package com.hp.hpl.jena.sparql.function.library;


//import org.apache.commons.logging.*;
import com.hp.hpl.jena.query.QueryBuildException ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.Function ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** Function that evaluates an expression - catches evaluation failures
 *  and returns false.
 *  Mainly used in extensions.
 *  Would be better if that were eval and this were "safe" or somesuch */

public class eval implements Function
{
    @Override
    public void build(String uri, ExprList args)
    {
        if ( args.size() != 1 )
            throw new QueryBuildException("'eval' takes one argument") ;
    }

    
    /** Processes unevaluated arguments */
    
    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env)
    {
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("function eval: Null args list") ;
        
        if ( args.size() != 1 )
            throw new ARQInternalErrorException("function eval: Arg list not of size 1") ;
        
        Expr ex = args.get(0) ;
        try {
            NodeValue v = ex.eval(binding, env) ;
            return v ;
        } catch (ExprEvalException evalEx)
        {
            return NodeValue.FALSE ;
        }
    }  

    
}
