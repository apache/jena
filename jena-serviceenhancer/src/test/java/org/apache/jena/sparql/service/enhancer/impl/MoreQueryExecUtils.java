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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class MoreQueryExecUtils {
    public static Node evalToNode(QueryExec qeTmp, Consumer<Context> cxtMutator) {
        Query query = qeTmp.getQuery();
        Var resultVar = Iterables.getOnlyElement(query.getProjectVars());
        Binding binding = evalToBinding(qeTmp, cxtMutator);
        Node result = binding == null ? null : binding.get(resultVar);
        return result;
    }

    public static Binding evalToBinding(QueryExec qeTmp, Consumer<Context> cxtMutator) {
        Binding result;
        try (QueryExec qe = qeTmp) {
            if (cxtMutator != null) {
                cxtMutator.accept(qe.getContext());
            }
            RowSet rs = qe.select();
            result = Iterators.getOnlyElement(rs, null);
        }
        return result;
    }

    // Project a certain column into a list of nodes
    public static List<Node> evalToNodes(QueryExec qeTmp, Consumer<Context> cxtMutator) {
        Query query = qeTmp.getQuery();
        Var resultVar = Iterables.getOnlyElement(query.getProjectVars());
        List<Node> result = new ArrayList<>();
        try (QueryExec qe = qeTmp) {
            if (cxtMutator != null) {
                cxtMutator.accept(qe.getContext());
            }
            qe.select().forEachRemaining(b -> result.add(b.get(resultVar)));
        }
        return result;
    }

    public static String evalToLexicalForm(QueryExec qe, Consumer<Context> cxtMutator) {
        Node node = evalToNode(qe, cxtMutator);
        String result = node == null ? null :
            node.isLiteral() ? node.getLiteralLexicalForm() : node.toString();
        return result;
    }
}
