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
import org.apache.jena.sparql.util.Context;

/** Implementation root for custom function evaluation. */
public abstract class FunctionBase implements Function {

    @Override
    public void build(String uri, ExprList args, Context context) {
        // Rename for legacy reasons.
        checkBuild(uri, args) ;
    }

    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("FunctionBase: Null args list") ;

        List<NodeValue> evalArgs = evalArgs(binding, args, env);

        return exec(evalArgs, env) ;
    }

    public static List<NodeValue> evalArgs(Binding binding, ExprList args, FunctionEnv env) {
        List<NodeValue> evalArgs = new ArrayList<>();
        for ( Expr e : args ) {
            NodeValue x = e.eval(binding, env);
            evalArgs.add(x);
        }
        return evalArgs;
    }

    /** Evaluation with access to the environment.
     * Special and careful use only!
     * Arity will have been checked if using a fixed-count FunctionBase subclass.
     */
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        return exec(args);
    }

    /** Function call to a list of evaluated argument values */
    public abstract NodeValue exec(List<NodeValue> args) ;

    public abstract void checkBuild(String uri, ExprList args) ;
}
