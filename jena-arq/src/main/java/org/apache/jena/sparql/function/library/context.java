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

import java.util.List;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Symbol;

/**
 * Function that returns the value of a context setting.
 */
public class context extends FunctionBase {
    // This function exists more for testing context get setup correctly than anything else.
    public context() {}

    @Override
    public void checkBuild(String uri, ExprList args) {
        if ( args.size() != 1 )
            throw new ExprEvalException("Wrong number of arguments");
    }

    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        NodeValue v = args.get(0);
        if ( ! v.isString() )
            throw new ExprEvalException("Not a string: function afn:context("+v+")");
        Symbol symbol = Symbol.create(v.getString());
        return get(symbol, env);
    }

    public static NodeValue get(Symbol symbol, FunctionEnv env) {
        Object obj = env.getContext().get(symbol);
        if ( obj == null )
            return NodeValue.nvEmptyString;
        if ( obj instanceof String )
            return NodeValue.makeString((String)obj);

        if ( !(obj instanceof Node) )
            throw new ExprEvalException("Not a Node: " + Lib.className(obj));
        Node n = (Node)obj;
        NodeValue nv = NodeValue.makeNode(n);
        return nv;
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        throw new InternalErrorException("afn:context.exec(args) called");
    }
}
