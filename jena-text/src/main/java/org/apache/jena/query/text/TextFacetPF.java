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
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.query.text.cql.CqlExpression;
import org.apache.jena.query.text.cql.CqlParser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL property function for facet counts ({@code luc:facet}).
 * <p>
 * <b>Syntax:</b>
 * <pre>
 * (?field ?value ?count) luc:facet (fieldSpec queryString '["field1","field2"]' cqlFilter? maxValues? minCount?)
 * </pre>
 * <p>
 * The first string literal is the field specification for text query scoping
 * (same as luc:query). CQL filters are JSON objects with an {@code "op"} key.
 */
public class TextFacetPF extends PropertyFunctionBase {
    private static final Logger log = LoggerFactory.getLogger(TextFacetPF.class);

    private ShaclTextIndexLucene textIndex = null;
    private boolean warningIssued = false;

    public TextFacetPF() {}

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt);

        if (argSubject.isList()) {
            int size = argSubject.getArgListSize();
            if (size < 1 || size > 3) {
                throw new QueryBuildException("Subject must have 1-3 elements (field, value, count): " + argSubject);
            }
        }

        if (argObject.isList()) {
            List<Node> list = argObject.getArgList();
            if (list.isEmpty()) {
                throw new QueryBuildException("Object list must contain at least a query string and facet fields");
            }
        }
    }

    private static ShaclTextIndexLucene chooseTextIndex(ExecutionContext execCxt, DatasetGraph dsg) {
        // Try registry first
        Object regObj = execCxt.getContext().get(TextQuery.textIndexRegistry);
        if (regObj instanceof TextIndexRegistry registry) {
            TextIndexLucene idx = registry.getDefault();
            if (idx instanceof ShaclTextIndexLucene shaclIdx) {
                return shaclIdx;
            }
            Log.warn(TextFacetPF.class, "Text index is not a ShaclTextIndexLucene - faceting not supported");
            return null;
        }

        // Fall back to single index
        Object obj = execCxt.getContext().get(TextQuery.textIndex);
        if (obj instanceof ShaclTextIndexLucene shaclIdx) {
            return shaclIdx;
        }
        if (obj != null) {
            Log.warn(TextFacetPF.class, "Context setting '" + TextQuery.textIndex + "' is not a ShaclTextIndexLucene");
        }
        if (dsg instanceof DatasetGraphText) {
            TextIndex ti = ((DatasetGraphText) dsg).getTextIndex();
            if (ti instanceof ShaclTextIndexLucene shaclIdx) {
                return shaclIdx;
            }
            Log.warn(TextFacetPF.class, "TextIndex is not a ShaclTextIndexLucene - faceting not supported");
        }
        Log.warn(TextFacetPF.class, "Failed to find the text index");
        return null;
    }

    @Override
    public QueryIterator exec(Binding binding,
                              PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                              ExecutionContext execCxt) {

        argSubject = Substitute.substitute(argSubject, binding);
        argObject = Substitute.substitute(argObject, binding);

        // Parse subject variables: (?field ?value ?count)
        Node fieldNode = null;
        Node valueNode = null;
        Node countNode = null;

        if (argSubject.isList()) {
            List<Node> subjList = argSubject.getArgList();
            fieldNode = subjList.get(0);
            if (!fieldNode.isVariable()) {
                throw new QueryExecException("Field must be a variable: " + argSubject);
            }
            if (subjList.size() > 1) {
                valueNode = subjList.get(1);
                if (!valueNode.isVariable()) {
                    throw new QueryExecException("Value must be a variable: " + argSubject);
                }
            }
            if (subjList.size() > 2) {
                countNode = subjList.get(2);
                if (!countNode.isVariable()) {
                    throw new QueryExecException("Count must be a variable: " + argSubject);
                }
            }
        } else {
            fieldNode = argSubject.getArg();
            if (!fieldNode.isVariable()) {
                throw new QueryExecException("Subject must be a variable: " + argSubject);
            }
        }

        // Parse object arguments
        FacetArgs args = parseObjectArgs(argObject);
        if (args == null || args.facetFields.isEmpty()) {
            return IterLib.noResults(execCxt);
        }

        // Resolve text index (always uses default)
        textIndex = chooseTextIndex(execCxt, execCxt.getDataset());
        if (textIndex == null) {
            if (!warningIssued) {
                Log.warn(getClass(), "No text index - no facet counts available");
                warningIssued = true;
            }
            return IterLib.noResults(execCxt);
        }

        if (!textIndex.isFacetingEnabled()) {
            Log.warn(getClass(), "Faceting is not enabled on this text index. Configure facet fields in the index definition.");
            return IterLib.noResults(execCxt);
        }

        // Get facet counts via SearchExecution for shared state
        Map<String, List<FacetValue>> facetCounts;
        try {
            log.debug("TextFacetPF: searchFields={} cqlFilter={} queryString='{}' facetFields={}",
                args.searchFields, args.cqlFilter, args.queryString, args.facetFields);

            SearchExecution se = SearchExecution.getOrCreate(
                execCxt, args.searchFields, args.queryString,
                args.cqlFilter, null, textIndex, null, null);
            facetCounts = se.getFacetCounts(args.facetFields, args.maxValues, args.minCount);
        } catch (Exception e) {
            log.error("Error getting facet counts: {}", e.getMessage());
            return IterLib.noResults(execCxt);
        }

        return generateBindings(binding, fieldNode, valueNode, countNode, facetCounts, execCxt);
    }

    private QueryIterator generateBindings(Binding binding, Node fieldNode, Node valueNode, Node countNode,
            Map<String, List<FacetValue>> facetCounts, ExecutionContext execCxt) {

        Var fieldVar = Var.isVar(fieldNode) ? Var.alloc(fieldNode) : null;
        Var valueVar = valueNode != null ? Var.alloc(valueNode) : null;
        Var countVar = countNode != null ? Var.alloc(countNode) : null;

        List<Binding> bindings = new ArrayList<>();

        for (Map.Entry<String, List<FacetValue>> entry : facetCounts.entrySet()) {
            String field = entry.getKey();
            for (FacetValue fv : entry.getValue()) {
                BindingBuilder builder = Binding.builder(binding);
                if (fieldVar != null) {
                    builder.add(fieldVar, NodeFactory.createLiteralString(field));
                }
                if (valueVar != null) {
                    builder.add(valueVar, NodeFactory.createLiteralString(fv.getValue()));
                }
                if (countVar != null) {
                    builder.add(countVar, NodeFactory.createLiteralDT(
                        String.valueOf(fv.getCount()), XSDDatatype.XSDlong));
                }
                bindings.add(builder.build());
            }
        }

        return QueryIterPlainWrapper.create(bindings.iterator(), execCxt);
    }

    /**
     * Parse the object argument list.
     * <p>
     * Arg order: (fieldSpec queryString facetFields cqlFilter? maxValues? minCount?)
     */
    private FacetArgs parseObjectArgs(PropFuncArg argObject) {
        List<String> searchFields = new ArrayList<>();
        String queryString = null;
        List<String> facetFields = new ArrayList<>();
        CqlExpression cqlFilter = null;
        int maxValues = 10;
        int minCount = 0;
        boolean maxValuesSet = false;

        if (argObject.isNode()) {
            log.warn("luc:facet requires at least a query string and facet fields");
            return null;
        }

        List<Node> list = argObject.getArgList();
        int idx = 0;

        // 1. First literal = field spec
        if (idx < list.size() && list.get(idx).isLiteral()) {
            String lex = list.get(idx).getLiteralLexicalForm();
            if (lex.startsWith("[")) {
                // Could be field spec array or facet fields array — check context
                // If no query string has been seen, treat first array-like literal as field spec
                // only if it's followed by another literal (the query string)
                // For simplicity: first non-JSON, non-integer literal = field spec
                // JSON arrays are handled below as facet fields
            } else if (!lex.startsWith("{") && !isInteger(lex)) {
                searchFields.add(lex);
                idx++;
            }
        }

        // 2. Query string (first non-JSON, non-integer literal)
        if (idx < list.size() && list.get(idx).isLiteral()) {
            String lex = list.get(idx).getLiteralLexicalForm();
            if (!lex.startsWith("[") && !lex.startsWith("{") && !isInteger(lex)) {
                queryString = lex;
                idx++;
            }
        }

        if (searchFields.isEmpty()) {
            searchFields.add("default");
        }

        // 3. Parse remaining: JSON arrays (facet fields), JSON objects (CQL), and integers
        while (idx < list.size()) {
            Node n = list.get(idx);
            if (n.isLiteral()) {
                String lex = n.getLiteralLexicalForm();
                if (lex.startsWith("[")) {
                    // JSON array: facet field names
                    JsonArray arr = JSON.parseAny(lex).getAsArray();
                    for (int i = 0; i < arr.size(); i++) {
                        facetFields.add(arr.get(i).getAsString().value());
                    }
                } else if (lex.startsWith("{")) {
                    // JSON object: CQL filter (has "op" key)
                    if (lex.contains("\"op\"")) {
                        cqlFilter = CqlParser.parse(lex);
                    }
                } else if (isInteger(lex)) {
                    if (!maxValuesSet) {
                        maxValues = Integer.parseInt(lex);
                        maxValuesSet = true;
                    } else {
                        minCount = Integer.parseInt(lex);
                    }
                } else {
                    if (queryString == null) {
                        queryString = lex;
                    } else {
                        log.warn("Unexpected argument in luc:facet: {}", lex);
                    }
                }
            }
            idx++;
        }

        return new FacetArgs(searchFields, queryString, facetFields, cqlFilter, maxValues, minCount);
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static class FacetArgs {
        final List<String> searchFields;
        final String queryString;
        final List<String> facetFields;
        final CqlExpression cqlFilter;
        final int maxValues;
        final int minCount;

        FacetArgs(List<String> searchFields, String queryString, List<String> facetFields,
                  CqlExpression cqlFilter, int maxValues, int minCount) {
            this.searchFields = searchFields;
            this.queryString = queryString;
            this.facetFields = facetFields;
            this.cqlFilter = cqlFilter;
            this.maxValues = maxValues;
            this.minCount = minCount;
        }
    }
}
