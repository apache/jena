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
package org.apache.jena.obfuscate;

import org.apache.jena.graph.Triple;
import org.apache.jena.obfuscate.transform.ExprTransformObfuscate;
import org.apache.jena.obfuscate.transform.TransformObfuscate;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;

/**
 * Utilities for obfuscation
 */
public class Obfuscator {

    public static Triple obfuscate(ObfuscationProvider provider, Triple t) {
        return new Triple(provider.obfuscateNode(t.getSubject()), provider.obfuscateNode(t.getPredicate()),
                provider.obfuscateNode(t.getObject()));
    }

    public static Quad obfuscate(ObfuscationProvider provider, Quad q) {
        return new Quad(provider.obfuscateNode(q.getGraph()), provider.obfuscateNode(q.getSubject()),
                provider.obfuscateNode(q.getPredicate()), provider.obfuscateNode(q.getObject()));
    }

    public static Op obfuscate(ObfuscationProvider provider, Op op) {
        // TODO Currently we take a full copy because somewhere something
        // modifies the original expressions and I never tracked down
        // where that happens to address it
        op = Transformer.transform(new TransformCopy(), new ExprTransformCopy(), op);
        return Transformer.transform(new TransformObfuscate(provider), new ExprTransformObfuscate(provider), op);
    }

    public static Query obfuscate(ObfuscationProvider provider, Query query) {
        Query q = query.cloneQuery();

        // TODO Obfuscate the query

        return q;
    }

    /**
     * @param provider
     * @param e
     * @return
     */
    public static Expr obfuscate(ObfuscationProvider provider, Expr e) {
        e = e.deepCopy();
        return ExprTransformer.transform(new ExprTransformObfuscate(provider), e);
    }
}
