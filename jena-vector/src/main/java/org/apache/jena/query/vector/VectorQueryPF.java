/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.vector;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.IterLib;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class VectorQueryPF extends PropertyFunctionBase {
    private VectorIndex vectorIndex;

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt);
        vectorIndex = chooseVectorIndex(execCxt, execCxt.getDataset());
        if (!argSubject.isList() || argSubject.getArgListSize() < 1 || argSubject.getArgListSize() > 2)
            throw new QueryBuildException("Subject must be (?uri) or (?uri ?score): " + argSubject);
        if (argObject.isList()) {
            int size = argObject.getArgListSize();
            if (size < 1 || size > 2)
                throw new QueryBuildException("Object must be query text or (query text limit): " + argObject);
        }
    }

    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        if (vectorIndex == null)
            return IterLib.result(binding, execCxt);

        argSubject = Substitute.substitute(argSubject, binding);
        argObject = Substitute.substitute(argObject, binding);

        Node subject = argSubject.getArg(0);
        Node score = argSubject.getArgListSize() > 1 ? argSubject.getArg(1) : null;
        if (!subject.isVariable())
            throw new QueryExecException("Vector hit subject must be a variable: " + subject);
        if (score != null && !score.isVariable())
            throw new QueryExecException("Vector hit score must be a variable: " + score);

        QuerySpec spec = parseObject(argObject);
        List<VectorHit> hits = vectorIndex.query(spec.queryText, spec.limit);

        Var subjectVar = Var.alloc(subject);
        Var scoreVar = score == null ? null : Var.alloc(score);
        Iterator<Binding> iterator = hits.stream().map(hit -> {
            BindingBuilder builder = Binding.builder(binding);
            builder.add(subjectVar, hit.getNode());
            if (scoreVar != null)
                builder.add(scoreVar, NodeFactoryExtra.floatToNode(hit.getScore()));
            return builder.build();
        }).iterator();
        return QueryIterPlainWrapper.create(iterator, execCxt);
    }

    private static VectorIndex chooseVectorIndex(ExecutionContext execCxt, DatasetGraph dsg) {
        Object obj = execCxt.getContext().get(VectorQuery.vectorIndex);
        if (obj instanceof VectorIndex)
            return (VectorIndex)obj;
        if (dsg instanceof DatasetGraphVector)
            return ((DatasetGraphVector)dsg).getVectorIndex();
        return null;
    }

    private static QuerySpec parseObject(PropFuncArg argObject) {
        Node queryNode;
        int limit = 10;
        if (argObject.isNode()) {
            queryNode = argObject.getArg();
        } else {
            queryNode = argObject.getArg(0);
            if (argObject.getArgListSize() > 1)
                limit = NodeFactoryExtra.nodeToInt(argObject.getArg(1));
        }
        if (!queryNode.isLiteral())
            throw new QueryExecException("Vector query text must be a literal: " + queryNode);
        return new QuerySpec(queryNode.getLiteralLexicalForm(), limit);
    }

    private record QuerySpec(String queryText, int limit) {}
}
