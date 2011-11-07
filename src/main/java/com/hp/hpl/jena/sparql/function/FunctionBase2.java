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

import java.util.List ;

import com.hp.hpl.jena.query.QueryBuildException ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Support for a function of one argument. */

public abstract class FunctionBase2 extends FunctionBase
{
    @Override
    public void checkBuild(String uri, ExprList args)
    { 
        if ( args.size() != 2 )
            throw new QueryBuildException("Function '"+Utils.className(this)+"' takes two arguments") ;
    }

    
    @Override
    public final NodeValue exec(List<NodeValue> args)
    {
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException(Utils.className(this)+": Null args list") ;
        
        if ( args.size() != 2 )
            throw new ExprEvalException(Utils.className(this)+": Wrong number of arguments: Wanted 2, got "+args.size()) ;
        
        NodeValue v1 = args.get(0) ;
        NodeValue v2 = args.get(1) ;
        
        return exec(v1, v2) ;
    }
    
    public abstract NodeValue exec(NodeValue v1, NodeValue v2) ;
}
