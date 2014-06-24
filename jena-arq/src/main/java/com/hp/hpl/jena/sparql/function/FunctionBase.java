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

package com.hp.hpl.jena.sparql.function;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Interface to value-testing extensions to the expression evaluator. */

public abstract class FunctionBase implements Function
{
    String uri = null ;
    protected ExprList arguments = null ;
    private FunctionEnv env ;
    
    @Override
    public final void build(String uri, ExprList args)
    {
        this.uri = uri ;
        arguments = args ;
        checkBuild(uri, args) ;
    }

    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env)
    {
        // This is merely to allow functions to be 
        // It duplicates code in E_Function/ExprFunctionN.
        
        this.env = env ;
        
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("FunctionBase: Null args list") ;
        
        List<NodeValue> evalArgs = new ArrayList<>() ;
        for ( Expr e : args )
        {
            NodeValue x = e.eval( binding, env );
            evalArgs.add( x );
        }
        
        NodeValue nv =  exec(evalArgs) ;
        arguments = null ;
        return nv ;
    }
    
    /** Return the Context object for this execution */
    public Context getContext() { return env.getContext() ; }
    
    /** Function call to a list of evaluated argument values */ 
    public abstract NodeValue exec(List<NodeValue> args) ;

    public abstract void checkBuild(String uri, ExprList args) ;
    
//    /** Get argument, indexing from 1 **/
//    public NodeValue getArg(int i)
//    {
//        i = i-1 ;
//        if ( i < 0 || i >= arguments.size()  )
//            return null ;
//        return (NodeValue)arguments.get(i) ;
//    }
}
