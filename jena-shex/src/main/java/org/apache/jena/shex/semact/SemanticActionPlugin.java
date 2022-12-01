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

package org.apache.jena.shex.semact;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shex.Plugin;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.expressions.SemAct;
import org.apache.jena.shex.expressions.ShapeExpression;
import org.apache.jena.shex.expressions.TripleExpression;
import org.apache.jena.shex.sys.SysShex;

import java.util.Collection;
import java.util.List;

public interface SemanticActionPlugin extends Plugin {

    @Override
    default void register() {
        List<String> uris = getUris();
        if(uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> SysShex.registerSemActPlugin(uri, this));
        }
    }

    List<String> getUris();

    boolean evaluateStart(SemAct semAct, ShexSchema schema);

    boolean evaluateTripleExpr(SemAct semAct, TripleExpression tripleExpression, Collection<Triple> triples);

    boolean evaluateShapeExpr(SemAct semAct, ShapeExpression shapeExpression, Node focus);
}
