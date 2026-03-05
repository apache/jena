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

package org.apache.jena.query.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.query.text.cql.CqlExpression;
import org.apache.jena.query.text.cql.CqlParser;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterSlice;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.IterLib;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL property function for SHACL-mode text queries ({@code luc:query}).
 * <p>
 * Argument format:
 * <pre>
 * (?s ?score ?literal ?totalHits) luc:query ("indexId" property* "query" cqlFilter? sortSpec? limit? highlight?)
 * </pre>
 * <p>
 * The first string literal is the index ID (required). CQL filters are JSON objects
 * with an {@code "op"} key. Sort specs are JSON with a {@code "field"} key.
 */
public class ShaclTextQueryPF extends PropertyFunctionBase {
    private static final Logger log = LoggerFactory.getLogger(ShaclTextQueryPF.class);

    private TextIndexLucene textIndex = null;
    private boolean warningIssued = false;

    public ShaclTextQueryPF() {}

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt);

        if (argSubject.isList()) {
            int size = argSubject.getArgListSize();
            if (size == 0 || size > 6) {
                throw new QueryBuildException("Subject has " + size + " elements, must be 1-6: " + argSubject);
            }
        }

        if (argObject.isList()) {
            List<Node> list = argObject.getArgList();
            if (list.isEmpty()) {
                throw new QueryBuildException("Zero-length argument list");
            }
        }
    }

    private static TextIndexLucene chooseTextIndex(ExecutionContext execCxt, DatasetGraph dsg, String indexId) {
        // Try registry first
        Object regObj = execCxt.getContext().get(TextQuery.textIndexRegistry);
        if (regObj instanceof TextIndexRegistry registry) {
            if (indexId != null) {
                return registry.get(indexId);
            }
            return registry.getDefault();
        }

        // Fall back to single index
        Object obj = execCxt.getContext().get(TextQuery.textIndex);
        if (obj instanceof TextIndexLucene tl) {
            return tl;
        }
        if (obj != null) {
            Log.warn(ShaclTextQueryPF.class, "Context setting '" + TextQuery.textIndex + "' is not a TextIndexLucene");
        }
        if (dsg instanceof DatasetGraphText) {
            TextIndex ti = ((DatasetGraphText) dsg).getTextIndex();
            if (ti instanceof TextIndexLucene tl) {
                return tl;
            }
            Log.warn(ShaclTextQueryPF.class, "TextIndex is not a TextIndexLucene");
        }
        Log.warn(ShaclTextQueryPF.class, "Failed to find the text index");
        return null;
    }

    @Override
    public QueryIterator exec(Binding binding,
                              PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                              ExecutionContext execCxt) {
        if (log.isTraceEnabled()) {
            IndentedLineBuffer subjBuff = new IndentedLineBuffer();
            argSubject.output(subjBuff, null);
            IndentedLineBuffer objBuff = new IndentedLineBuffer();
            argObject.output(objBuff, null);
            log.trace("exec: {} luc:query {}", subjBuff, objBuff);
        }

        argSubject = Substitute.substitute(argSubject, binding);
        argObject = Substitute.substitute(argObject, binding);

        Node s = null, score = null, literal = null, totalHitsNode = null, graph = null, prop = null;

        if (argSubject.isList()) {
            s = argSubject.getArg(0);
            if (argSubject.getArgListSize() > 1) {
                score = argSubject.getArg(1);
                if (!score.isVariable())
                    throw new QueryExecException("Hit score is not a variable: " + argSubject);
            }
            if (argSubject.getArgListSize() > 2) {
                literal = argSubject.getArg(2);
                if (!literal.isVariable())
                    throw new QueryExecException("Hit literal is not a variable: " + argSubject);
            }
            if (argSubject.getArgListSize() > 3) {
                totalHitsNode = argSubject.getArg(3);
                if (!totalHitsNode.isVariable())
                    throw new QueryExecException("Total hits is not a variable: " + argSubject);
            }
            if (argSubject.getArgListSize() > 4) {
                graph = argSubject.getArg(4);
                if (!graph.isVariable())
                    throw new QueryExecException("Hit graph is not a variable: " + argSubject);
            }
            if (argSubject.getArgListSize() > 5) {
                prop = argSubject.getArg(5);
                if (!prop.isVariable())
                    throw new QueryExecException("Hit prop is not a variable: " + argSubject);
            }
        } else {
            s = argSubject.getArg();
        }

        if (s.isLiteral())
            return IterLib.noResults(execCxt);

        QueryArgs args = parseArgs(argObject);
        if (args == null)
            return IterLib.noResults(execCxt);

        // Resolve text index using indexId
        textIndex = chooseTextIndex(execCxt, execCxt.getDataset(), args.indexId);
        if (textIndex == null) {
            if (!warningIssued) {
                Log.warn(getClass(), "No text index - no text search performed");
                warningIssued = true;
            }
            return IterLib.result(binding, execCxt);
        }

        // Use SearchExecution for shared state with luc:facet
        SearchExecution se = SearchExecution.getOrCreate(
            execCxt, args.indexId, args.props, args.queryString,
            args.cqlFilter, args.sortSpecs, textIndex, null, null);

        int limit = args.limit > 0 ? args.limit : 10000;
        List<TextHit> allHits = se.getHits(limit, args.highlight);

        Collection<TextHit> hits;
        if (Var.isVar(s)) {
            hits = allHits;
        } else {
            String subjStr = TextQueryFuncs.subjectToString(s);
            hits = new ArrayList<>();
            for (TextHit hit : allHits) {
                if (subjStr.equals(TextQueryFuncs.subjectToString(hit.getNode()))) {
                    hits.add(hit);
                }
            }
        }

        long totalHits = totalHitsNode != null ? se.getTotalHits() : -1;
        QueryIterator qIter = resultsToQueryIterator(binding, s, score, literal, totalHitsNode, totalHits, graph, prop, hits, execCxt);
        if (args.limit >= 0)
            qIter = new QueryIterSlice(qIter, 0, args.limit, execCxt);
        return qIter;
    }

    private QueryIterator resultsToQueryIterator(Binding binding, Node subj, Node score, Node literal,
                                                  Node totalHitsNode, long totalHits,
                                                  Node graph, Node prop, Collection<TextHit> results,
                                                  ExecutionContext execCxt) {
        Var sVar = Var.isVar(subj) ? Var.alloc(subj) : null;
        Var scoreVar = (score == null) ? null : Var.alloc(score);
        Var literalVar = (literal == null) ? null : Var.alloc(literal);
        Var totalHitsVar = (totalHitsNode == null) ? null : Var.alloc(totalHitsNode);
        Node totalHitsValue = totalHitsVar != null
            ? NodeFactory.createLiteralDT(String.valueOf(totalHits), XSDDatatype.XSDlong) : null;
        Var graphVar = (graph == null) ? null : Var.alloc(graph);
        Var propVar = (prop == null) ? null : Var.alloc(prop);

        Function<TextHit, Binding> converter = (TextHit hit) -> {
            if (score == null && literal == null && totalHitsVar == null)
                return sVar != null ? BindingFactory.binding(binding, sVar, hit.getNode()) : BindingFactory.binding(binding);
            BindingBuilder bmap = Binding.builder(binding);
            if (sVar != null) bmap.add(sVar, hit.getNode());
            if (scoreVar != null) bmap.add(scoreVar, NodeFactoryExtra.floatToNode(hit.getScore()));
            if (literalVar != null && hit.getLiteral() != null) bmap.add(literalVar, hit.getLiteral());
            if (totalHitsVar != null) bmap.add(totalHitsVar, totalHitsValue);
            if (graphVar != null && hit.getGraph() != null) bmap.add(graphVar, hit.getGraph());
            if (propVar != null && hit.getProp() != null) bmap.add(propVar, hit.getProp());
            return bmap.build();
        };

        Iterator<Binding> bIter = Iter.map(results.iterator(), converter);
        return QueryIterPlainWrapper.create(bIter, execCxt);
    }

    /**
     * Parse object arguments.
     * <p>
     * Arg order: (indexId property* queryString cqlFilter? sortSpec? limit? highlight?)
     * <ul>
     *   <li>First literal = index ID (required)</li>
     *   <li>URIs = properties to search</li>
     *   <li>Next plain literal = query string</li>
     *   <li>JSON with "op" key = CQL filter</li>
     *   <li>JSON with "field" key = sort spec</li>
     *   <li>Integer = limit</li>
     *   <li>"highlight:..." = highlight options</li>
     * </ul>
     */
    private QueryArgs parseArgs(PropFuncArg argObject) {
        List<Resource> props = new ArrayList<>();
        String indexId = null;
        String queryString = null;
        CqlExpression cqlFilter = null;
        List<SortSpec> sortSpecs = null;
        int limit = -1;
        String highlight = null;

        if (argObject.isNode()) {
            Node o = argObject.getArg();
            if (!o.isLiteral()) {
                log.warn("Object to luc:query is not a literal: " + argObject);
                return null;
            }
            queryString = o.getLiteralLexicalForm();
            return new QueryArgs(TextIndexRegistry.DEFAULT_ID, props, queryString, cqlFilter, sortSpecs, limit, highlight);
        }

        List<Node> list = argObject.getArgList();
        if (list.isEmpty())
            throw new TextIndexException("luc:query object list can not be empty");

        int idx = 0;

        // First literal = index ID
        if (idx < list.size() && list.get(idx).isLiteral()) {
            String lex = list.get(idx).getLiteralLexicalForm();
            if (!lex.startsWith("{") && !lex.startsWith("[") && !isJsonLike(lex)) {
                indexId = lex;
                idx++;
            }
        }

        // Collect property URIs
        while (idx < list.size() && list.get(idx).isURI()) {
            Property prop = ResourceFactory.createProperty(list.get(idx).getURI());
            props.add(prop);
            idx++;
        }

        // Query string (next non-JSON literal)
        if (idx < list.size() && list.get(idx).isLiteral()) {
            String lex = list.get(idx).getLiteralLexicalForm();
            if (!lex.startsWith("{") && !lex.startsWith("[")) {
                queryString = lex;
                idx++;
            }
        }

        if (queryString == null) {
            log.warn("No query string in luc:query arguments: " + list);
            return null;
        }

        if (indexId == null) {
            indexId = TextIndexRegistry.DEFAULT_ID;
        }

        // Remaining args: CQL filter, sort spec, limit, highlight
        while (idx < list.size()) {
            Node n = list.get(idx);
            if (n.isLiteral()) {
                String lex = n.getLiteralLexicalForm();
                if (lex.startsWith("{")) {
                    if (isCqlFilter(lex)) {
                        cqlFilter = CqlParser.parse(lex);
                    } else if (SortSpecParser.isSortSpec(lex)) {
                        sortSpecs = SortSpecParser.parse(lex);
                    }
                } else if (lex.startsWith("[")) {
                    // Array sort spec
                    if (SortSpecParser.isSortSpec(lex)) {
                        sortSpecs = SortSpecParser.parse(lex);
                    }
                } else if (lex.startsWith("highlight:")) {
                    highlight = lex.substring("highlight:".length());
                } else {
                    try {
                        int v = Integer.parseInt(lex);
                        limit = (v < 0) ? -1 : v;
                    } catch (NumberFormatException e) {
                        try {
                            int v = NodeFactoryExtra.nodeToInt(n);
                            limit = (v < 0) ? -1 : v;
                        } catch (Exception ex) {
                            log.warn("Unexpected argument in luc:query: {}", lex);
                        }
                    }
                }
            }
            idx++;
        }

        return new QueryArgs(indexId, props, queryString, cqlFilter, sortSpecs, limit, highlight);
    }

    /**
     * Check if a JSON string is a CQL filter (has an "op" key).
     */
    private static boolean isCqlFilter(String json) {
        return json.contains("\"op\"");
    }

    private static boolean isJsonLike(String s) {
        return s.startsWith("{") || s.startsWith("[");
    }

    private static class QueryArgs {
        final String indexId;
        final List<Resource> props;
        final String queryString;
        final CqlExpression cqlFilter;
        final List<SortSpec> sortSpecs;
        final int limit;
        final String highlight;

        QueryArgs(String indexId, List<Resource> props, String queryString,
                  CqlExpression cqlFilter, List<SortSpec> sortSpecs,
                  int limit, String highlight) {
            this.indexId = indexId;
            this.props = props;
            this.queryString = queryString;
            this.cqlFilter = cqlFilter;
            this.sortSpecs = sortSpecs;
            this.limit = limit;
            this.highlight = highlight;
        }
    }
}
