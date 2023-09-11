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

import java.util.IdentityHashMap;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.lang.LabelToNodeMap;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.util.Symbol;

public class E_BNode implements Unstable
{
    private static final String symbol = Tags.tagBNode;

    private static final Symbol keyMap = Symbol.create("arq:internal:bNodeMappings");

    public static Expr create() {
        return new E_BNode0();
    }

    public static Expr create(Expr expr) {
        return new E_BNode1(expr);
    }

    // --- The zero argument case.
    private static class E_BNode0 extends ExprFunction0  implements Unstable {

        protected E_BNode0() {
            super(symbol);
        }

        @Override
        public NodeValue eval(FunctionEnv env) {
            return NodeValue.makeNode(NodeFactory.createBlankNode());
        }

        @Override
        public Expr copy() {
            return new E_BNode0();
        }
    }

    // --- The one argument case.
    private static class E_BNode1 extends ExprFunction1  implements Unstable {
        protected E_BNode1(Expr expr) {
            super(expr, symbol);
        }

        @Override
        public NodeValue eval(NodeValue nv)
        { throw new ARQInternalErrorException(); }

        @Override
        public NodeValue evalSpecial(Binding binding, FunctionEnv env) {
            NodeValue x = expr.eval(binding, env);
            if ( !x.isString() )
                throw new ExprEvalException("Not a string: " + x);

            Integer key = System.identityHashCode(binding);

            // IdentityHashMap
            // Normally bindings have value equality (e.g. DISTINCT)
            @SuppressWarnings("unchecked")
            IdentityHashMap<Binding, LabelToNodeMap> mapping = (IdentityHashMap<Binding, LabelToNodeMap>)env.getContext().get(keyMap);

            if ( mapping == null ) {
                mapping = new IdentityHashMap<>();
                env.getContext().set(keyMap, mapping);
            }
            LabelToNodeMap mapper = mapping.get(binding);
            if ( mapper == null ) {
                @SuppressWarnings("deprecation")
                LabelToNodeMap mapper_ = LabelToNodeMap.createBNodeMap();
                mapper = mapper_;
                mapping.put(binding, mapper);
            }

            Node bnode = mapper.asNode(x.getString());
            return NodeValue.makeNode(bnode);
        }

        @Override
        public Expr copy(Expr expr) {
            return new E_BNode1(expr);
        }
    }

}
