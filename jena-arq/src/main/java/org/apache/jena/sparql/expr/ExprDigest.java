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

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDigest;
import org.apache.jena.sparql.serializer.SerializationContext;

public abstract class ExprDigest extends ExprFunction1
{
    private final String digestName;
    // Historically, MD5 and SH* have been printed upper case.
    private final String printName;

    public ExprDigest(Expr expr, String symbol, String printName, String digestName) {
        super(expr, symbol);
        this.digestName = digestName;
        this.printName = printName;
    }

    private final Cache<NodeValue, NodeValue> cache = CacheFactory.createOneSlotCache();

    @Override
    public NodeValue eval(NodeValue v) {
        return cache.get(v, x -> calculate(x));
    }

    private NodeValue calculate(NodeValue v) {
        return NodeValueDigest.calculateDigest(v, digestName);
    }

    @Override
    public String getFunctionPrintName(SerializationContext cxt) {
        return printName;
    }
}
