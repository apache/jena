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

package org.apache.jena.sparql.function;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;

/** Implementation root for custom function evaluation. */  
public abstract class FunctionBase implements Function {

    @Override
    public final void build(String uri, ExprList args) {
        // Rename for legacy reasons.
        checkBuild(uri, args) ;
    }

    // Valid during execution.
    // Only specialised uses need these values 
    // e.g. fn:apply which is a meta-function - it looks up a URI to get a function to call.
    protected FunctionEnv functionEnv = null;
    // Not needed so hide but keep for debugging.
    private Binding binding = null;
    
    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("FunctionBase: Null args list") ;
        
        List<NodeValue> evalArgs = new ArrayList<>() ;
        for ( Expr e : args )
        {
            NodeValue x = e.eval( binding, env );
            evalArgs.add( x );
        }
        
        // Cature
        try {
            this.functionEnv = env ;
            this.binding = binding;
            NodeValue nv = exec(evalArgs) ;
            return nv ;
        } finally {
            this.functionEnv = null ;
            this.binding = null;
        }
        
    }
    
    /** Function call to a list of evaluated argument values */ 
    public abstract NodeValue exec(List<NodeValue> args) ;

    public abstract void checkBuild(String uri, ExprList args) ;
}
