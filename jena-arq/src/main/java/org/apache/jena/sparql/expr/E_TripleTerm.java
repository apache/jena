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

import org.apache.jena.sparql.function.library.triple.EmbeddedTripleFunctions;
import org.apache.jena.sparql.sse.Tags;

public class E_TripleTerm extends ExprFunction3 {

    private static final String symbol = Tags.tagFnTriple;

    public E_TripleTerm(Expr expr1, Expr expr2, Expr expr3) {
        // SPARQL keyword, symbol for SSE.
        super(expr1, expr2, expr3, symbol);
    }

    @Override
    public NodeValue eval(NodeValue s, NodeValue p, NodeValue o) {
        return EmbeddedTripleFunctions.fnTriple(s, p, o);
    }

    @Override
    public Expr copy(Expr arg1, Expr arg2, Expr arg3) {
        return new E_TripleTerm(arg1, arg2, arg3);
    }
}
