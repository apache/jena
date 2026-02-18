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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecException;
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
 * Supports structured filter arguments for faceted navigation:
 * <pre>
 * (?s ?score) luc:query (property? "query" '{"field":["val"]}'? limit?)
 * </pre>
 * <p>
 * When filters are present, uses {@link SearchExecution} to share state
 * with {@code luc:facet} in the same query.
 */
public class ShaclTextQueryPF extends PropertyFunctionBase {
    private static final Logger log = LoggerFactory.getLogger(ShaclTextQueryPF.class);

    private TextIndexLucene textIndex = null;
    private boolean warningIssued = false;

    public ShaclTextQueryPF() {}

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt);
        DatasetGraph dsg = execCxt.getDataset();
        textIndex = chooseTextIndex(execCxt, dsg);

        if (argSubject.isList()) {
            int size = argSubject.getArgListSize();
            if (size == 0 || size > 5) {
                throw new QueryBuildException("Subject has " + size + " elements, must be 1-5: " + argSubject);
            }
        }

        if (argObject.isList()) {
            List<Node> list = argObject.getArgList();
            if (list.isEmpty()) {
                throw new QueryBuildException("Zero-length argument list");
            }
        }
    }

    private static TextIndexLucene chooseTextIndex(ExecutionContext execCxt, DatasetGraph dsg) {
        Object obj = execCxt.getContext().get(TextQuery.textIndex);
        if (obj instanceof TextIndexLucene) {
            return (TextIndexLucene) obj;
        }
        if (obj != null) {
            Log.warn(ShaclTextQueryPF.class, "Context setting '" + TextQuery.textIndex + "' is not a TextIndexLucene");
        }
        if (dsg instanceof DatasetGraphText) {
            TextIndex ti = ((DatasetGraphText) dsg).getTextIndex();
            if (ti instanceof TextIndexLucene) {
                return (TextIndexLucene) ti;
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

        if (textIndex == null) {
            if (!warningIssued) {
                Log.warn(getClass(), "No text index - no text search performed");
                warningIssued = true;
            }
            return IterLib.result(binding, execCxt);
        }

        argSubject = Substitute.substitute(argSubject, binding);
        argObject = Substitute.substitute(argObject, binding);

        Node s = null, score = null, literal = null, graph = null, prop = null;

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
                graph = argSubject.getArg(3);
                if (!graph.isVariable())
                    throw new QueryExecException("Hit graph is not a variable: " + argSubject);
            }
            if (argSubject.getArgListSize() > 4) {
                prop = argSubject.getArg(4);
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

        // Use SearchExecution for shared state with luc:facet
        SearchExecution se = SearchExecution.getOrCreate(
            execCxt, args.props, args.queryString,
            args.filters, textIndex, null, null);

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

        QueryIterator qIter = resultsToQueryIterator(binding, s, score, literal, graph, prop, hits, execCxt);
        if (args.limit >= 0)
            qIter = new QueryIterSlice(qIter, 0, args.limit, execCxt);
        return qIter;
    }

    private QueryIterator resultsToQueryIterator(Binding binding, Node subj, Node score, Node literal,
                                                  Node graph, Node prop, Collection<TextHit> results,
                                                  ExecutionContext execCxt) {
        Var sVar = Var.isVar(subj) ? Var.alloc(subj) : null;
        Var scoreVar = (score == null) ? null : Var.alloc(score);
        Var literalVar = (literal == null) ? null : Var.alloc(literal);
        Var graphVar = (graph == null) ? null : Var.alloc(graph);
        Var propVar = (prop == null) ? null : Var.alloc(prop);

        Function<TextHit, Binding> converter = (TextHit hit) -> {
            if (score == null && literal == null)
                return sVar != null ? BindingFactory.binding(binding, sVar, hit.getNode()) : BindingFactory.binding(binding);
            BindingBuilder bmap = Binding.builder(binding);
            if (sVar != null) bmap.add(sVar, hit.getNode());
            if (scoreVar != null) bmap.add(scoreVar, NodeFactoryExtra.floatToNode(hit.getScore()));
            if (literalVar != null && hit.getLiteral() != null) bmap.add(literalVar, hit.getLiteral());
            if (graphVar != null && hit.getGraph() != null) bmap.add(graphVar, hit.getGraph());
            if (propVar != null && hit.getProp() != null) bmap.add(propVar, hit.getProp());
            return bmap.build();
        };

        Iterator<Binding> bIter = Iter.map(results.iterator(), converter);
        return QueryIterPlainWrapper.create(bIter, execCxt);
    }

    private QueryArgs parseArgs(PropFuncArg argObject) {
        List<Resource> props = new ArrayList<>();
        String queryString = null;
        Map<String, List<String>> filters = null;
        int limit = -1;
        String highlight = null;

        if (argObject.isNode()) {
            Node o = argObject.getArg();
            if (!o.isLiteral()) {
                log.warn("Object to luc:query is not a literal: " + argObject);
                return null;
            }
            queryString = o.getLiteralLexicalForm();
            return new QueryArgs(props, queryString, filters, limit, highlight);
        }

        List<Node> list = argObject.getArgList();
        if (list.isEmpty())
            throw new TextIndexException("luc:query object list can not be empty");

        int idx = 0;

        // Collect property URIs
        while (idx < list.size() && list.get(idx).isURI()) {
            Property prop = ResourceFactory.createProperty(list.get(idx).getURI());
            props.add(prop);
            idx++;
        }

        // Query string
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

        // Remaining args: JSON filters, limit, highlight
        while (idx < list.size()) {
            Node n = list.get(idx);
            if (n.isLiteral()) {
                String lex = n.getLiteralLexicalForm();
                if (lex.startsWith("{")) {
                    filters = parseJsonFilters(lex);
                } else if (lex.startsWith("highlight:")) {
                    highlight = lex.substring("highlight:".length());
                } else {
                    try {
                        int v = Integer.parseInt(lex);
                        limit = (v < 0) ? -1 : v;
                    } catch (NumberFormatException e) {
                        // Try as NodeFactoryExtra for typed literals
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

        return new QueryArgs(props, queryString, filters, limit, highlight);
    }

    /**
     * Parse a JSON object string into a filter map.
     * Expected format: {"field": ["value1", "value2"], "field2": ["value3"]}
     */
    static Map<String, List<String>> parseJsonFilters(String jsonStr) {
        Map<String, List<String>> filters = new LinkedHashMap<>();
        JsonObject json = JSON.parse(jsonStr);
        for (String key : json.keys()) {
            JsonArray vals = json.get(key).getAsArray();
            List<String> values = new ArrayList<>();
            for (int i = 0; i < vals.size(); i++) {
                values.add(vals.get(i).getAsString().value());
            }
            filters.put(key, values);
        }
        return filters;
    }

    private static class QueryArgs {
        final List<Resource> props;
        final String queryString;
        final Map<String, List<String>> filters;
        final int limit;
        final String highlight;

        QueryArgs(List<Resource> props, String queryString,
                  Map<String, List<String>> filters, int limit, String highlight) {
            this.props = props;
            this.queryString = queryString;
            this.filters = filters;
            this.limit = limit;
            this.highlight = highlight;
        }
    }
}
