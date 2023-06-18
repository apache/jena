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

package org.apache.jena.sparql.function.library;

import java.util.List ;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.*;
import org.apache.jena.sparql.util.Context ;

/** XPath and XQuery Functions and Operators 3.1
 * <p>
 * {@code fn:apply(function, args)}
 */
public class FN_Apply extends FunctionBase {
    // Assumes one object per use site.
    private Cache<String, Function> cache1 = CacheFactory.createOneSlotCache();

    @Override
    public void checkBuild(String uri, ExprList args) {
        if ( args.isEmpty() )
            throw new ExprException("fn:apply: no function to call (minimum number of args is one)");
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        throw new InternalErrorException("fn:apply: exec(args) Should not have been called");
    }

    @Override
    public NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        if ( args.isEmpty() )
            throw new ExprException("fn:apply: no function to call (minimum number of args is one)");
        NodeValue functionId = args.get(0);
        List<NodeValue> argExprs = args.subList(1,args.size()) ;
        ExprList exprs = new ExprList();
        argExprs.forEach(exprs::add);
        Node fnNode = functionId.asNode();

        if ( fnNode.isBlank() )
            throw new ExprEvalException("fn:apply: function id is a blank node (must be a URI)");
        if ( fnNode.isLiteral() )
            throw new ExprEvalException("fn:apply: function id is a literal (must be a URI)");
        if ( fnNode.isVariable() )
            // Should not happen ... but ...
            throw new ExprEvalException("fn:apply: function id is an unbound variable (must be a URI)");
        if ( fnNode.isURI() ) {
            String functionIRI = fnNode.getURI();
            Function function = cache1.get(functionIRI, x->buildFunction(x, env));
            if ( function == null )
                throw new ExprEvalException("fn:apply: Unknown function: <"+functionId+">");
            if ( function instanceof FunctionBase ) {
                // Fast track.
                return ((FunctionBase)function).exec(argExprs);
            }
            function.build(functionIRI, exprs, env.getContext());
            // Eval'ed arguments.
            return function.exec(null, exprs, functionIRI, env);
        }

        throw new ExprEvalException("fn:apply: Weird function argument (arg 1): "+functionId);
    }

    private Function buildFunction(String functionIRI, FunctionEnv functionEnv) {
        FunctionRegistry registry = chooseRegistry(functionEnv.getContext()) ;
        FunctionFactory ff = registry.get(functionIRI) ;
        if ( ff == null )
            return null;
        return ff.create(functionIRI) ;
    }

    private FunctionRegistry chooseRegistry(Context context) {
        FunctionRegistry registry = FunctionRegistry.get(context) ;
        if ( registry == null )
            registry = FunctionRegistry.get() ;
        return registry ;
    }
}
