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

import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.sparql.sse.Tags ;

/** Create a literal from lexical form and language tag */
public class E_StrLang extends ExprFunction2
{
    private static final String symbol = Tags.tagStrLang;

    public E_StrLang(Expr expr1, Expr expr2) {
        super(expr1, expr2, symbol);
    }

    @Override
    public NodeValue eval(NodeValue v1, NodeValue v2) {
        return NodeFunctions.strLang(v1, v2);
    }

    @Override
    public Expr copy(Expr e1, Expr e2) { return new E_StrLang(e1, e2); }
}
