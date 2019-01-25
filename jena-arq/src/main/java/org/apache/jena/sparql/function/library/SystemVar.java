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

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase0;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Symbol;

/**
 * Function that returns the value of a system variable.
 */
public class SystemVar extends FunctionBase0 {
    private Symbol systemSymbol;

    protected SystemVar(Symbol systemSymbol) {
        if ( systemSymbol == null )
            throw new ExprException("System symbol is null ptr");
        this.systemSymbol = systemSymbol;
    }

    // Need to intercept exec so we can get to the FunctionEnv
    @Override
    public NodeValue exec() {
        return get(systemSymbol, super.functionEnv);
    }

    public static NodeValue get(Symbol symbol, FunctionEnv env) {
        Object obj = env.getContext().get(symbol);
        if ( obj == null )
            throw new ExprEvalException("null for symbol: " + symbol);
        if ( !(obj instanceof Node) )
            throw new ExprEvalException("Not a Node: " + Lib.className(obj));
        Node n = (Node)obj;
        NodeValue nv = NodeValue.makeNode(n);
        return nv;
    }
}
