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

package org.apache.jena.query.hybrid;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextQuery;
import org.apache.jena.query.vector.VectorIndex;
import org.apache.jena.query.vector.VectorQuery;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class HybridQueryPF extends PropertyFunctionBase {
    private TextIndex textIndex;
    private VectorIndex vectorIndex;

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt);
        Object textObj = execCxt.getContext().get(TextQuery.textIndex);
        Object vectorObj = execCxt.getContext().get(VectorQuery.vectorIndex);
        if (textObj instanceof TextIndex)
            textIndex = (TextIndex)textObj;
        if (vectorObj instanceof VectorIndex)
            vectorIndex = (VectorIndex)vectorObj;

        if (!argSubject.isList())
            throw new QueryBuildException("Subject must be a list: " + argSubject);
        int subjectSize = argSubject.getArgListSize();
        if (subjectSize != 2 && subjectSize != 4 && subjectSize != 6)
            throw new QueryBuildException("Subject must be (?uri ?score), (?uri ?score ?textRank ?vectorRank), or (?uri ?score ?textRank ?vectorRank ?textScore ?vectorScore): " + argSubject);
        if (!argObject.isList())
            throw new QueryBuildException("Object must be a list: " + argObject);
        int objectSize = argObject.getArgListSize();
        if (objectSize < 2 || objectSize > 7)
            throw new QueryBuildException("Object must be (property queryText limit? candidateLimit? rrfK? textWeight? vectorWeight?): " + argObject);
    }

    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        if (textIndex == null)
            throw new QueryExecException("No jena-text index found for hybrid:query");
        if (vectorIndex == null)
            throw new QueryExecException("No jena-vector index found for hybrid:query");

        argSubject = Substitute.substitute(argSubject, binding);
        argObject = Substitute.substitute(argObject, binding);

        SubjectVars vars = parseSubject(argSubject);
        QuerySpec spec = parseObject(argObject);
        List<HybridHit> hits = HybridSearch.search(textIndex, vectorIndex, spec.property, spec.queryText, spec.limit,
                spec.candidateLimit, spec.rrfK, spec.textWeight, spec.vectorWeight);

        Iterator<Binding> iterator = hits.stream().map(hit -> {
            BindingBuilder builder = Binding.builder(binding);
            add(builder, vars.subject, hit.getNode());
            add(builder, vars.score, NodeFactoryExtra.floatToNode(hit.getScore()));
            add(builder, vars.textRank, rankNode(hit.getTextRank()));
            add(builder, vars.vectorRank, rankNode(hit.getVectorRank()));
            add(builder, vars.textScore, scoreNode(hit.getTextScore()));
            add(builder, vars.vectorScore, scoreNode(hit.getVectorScore()));
            return builder.build();
        }).iterator();
        return QueryIterPlainWrapper.create(iterator, execCxt);
    }

    private static SubjectVars parseSubject(PropFuncArg argSubject) {
        return new SubjectVars(var(argSubject.getArg(0), "hit subject"),
                var(argSubject.getArg(1), "hybrid score"),
                argSubject.getArgListSize() > 2 ? var(argSubject.getArg(2), "text rank") : null,
                argSubject.getArgListSize() > 3 ? var(argSubject.getArg(3), "vector rank") : null,
                argSubject.getArgListSize() > 4 ? var(argSubject.getArg(4), "text score") : null,
                argSubject.getArgListSize() > 5 ? var(argSubject.getArg(5), "vector score") : null);
    }

    private static QuerySpec parseObject(PropFuncArg argObject) {
        Node property = argObject.getArg(0);
        Node queryText = argObject.getArg(1);
        if (!property.isURI())
            throw new QueryExecException("Hybrid query property must be an IRI: " + property);
        if (!queryText.isLiteral())
            throw new QueryExecException("Hybrid query text must be a literal: " + queryText);
        int limit = intArg(argObject, 2, HybridSearch.DEFAULT_LIMIT);
        int candidateLimit = intArg(argObject, 3, Math.max(HybridSearch.DEFAULT_CANDIDATE_LIMIT, limit));
        int rrfK = intArg(argObject, 4, HybridSearch.DEFAULT_RRF_K);
        float textWeight = floatArg(argObject, 5, HybridSearch.DEFAULT_TEXT_WEIGHT);
        float vectorWeight = floatArg(argObject, 6, HybridSearch.DEFAULT_VECTOR_WEIGHT);
        return new QuerySpec(property, queryText.getLiteralLexicalForm(), limit, candidateLimit, rrfK, textWeight, vectorWeight);
    }

    private static Var var(Node node, String label) {
        if (!node.isVariable())
            throw new QueryExecException("Hybrid " + label + " must be a variable: " + node);
        return Var.alloc(node);
    }

    private static int intArg(PropFuncArg args, int index, int dft) {
        if (args.getArgListSize() <= index)
            return dft;
        return NodeFactoryExtra.nodeToInt(args.getArg(index));
    }

    private static float floatArg(PropFuncArg args, int index, float dft) {
        if (args.getArgListSize() <= index)
            return dft;
        Node node = args.getArg(index);
        if (!node.isLiteral() || !(node.getLiteralValue() instanceof Number))
            throw new QueryExecException("Hybrid numeric argument must be a number: " + node);
        return ((Number)node.getLiteralValue()).floatValue();
    }

    private static void add(BindingBuilder builder, Var var, Node value) {
        if (var != null && value != null)
            builder.add(var, value);
    }

    private static Node rankNode(int rank) {
        return rank > 0 ? NodeFactoryExtra.intToNode(rank) : NodeFactoryExtra.intToNode(0);
    }

    private static Node scoreNode(float score) {
        return Float.isNaN(score) ? null : NodeFactoryExtra.floatToNode(score);
    }

    private record SubjectVars(Var subject, Var score, Var textRank, Var vectorRank, Var textScore, Var vectorScore) {}

    private record QuerySpec(Node property, String queryText, int limit, int candidateLimit, int rrfK, float textWeight, float vectorWeight) {}
}
