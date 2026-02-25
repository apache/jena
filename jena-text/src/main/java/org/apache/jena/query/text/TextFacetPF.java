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
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.IterLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL property function for facet counts with structured filter support.
 * <p>
 * <b>Syntax:</b>
 * <pre>
 * (?field ?value ?count) text:facet (property? "query" '["field1","field2"]' '{"field":"[val]"}'? maxValues?)
 * </pre>
 * <p>
 * <b>Arguments:</b>
 * <ol>
 *   <li>Optional: One or more property URIs to search</li>
 *   <li>Required: Query string (plain literal)</li>
 *   <li>Required: Facet fields as JSON array: '["category", "author"]'</li>
 *   <li>Optional: Filters as JSON object: '{"category": ["Technology"]}'</li>
 *   <li>Optional: Max facet values per field (integer, default 10)</li>
 * </ol>
 * <p>
 * When used together with text:query in the same BGP with matching parameters,
 * they share a SearchExecution instance to avoid duplicate Lucene queries.
 */
public class TextFacetPF extends PropertyFunctionBase {
    private static final Logger log = LoggerFactory.getLogger(TextFacetPF.class);

    private TextIndexLucene textIndex = null;
    private boolean warningIssued = false;

    public TextFacetPF() {}

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt);
        DatasetGraph dsg = execCxt.getDataset();
        textIndex = chooseTextIndex(execCxt, dsg);

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

    private static TextIndexLucene chooseTextIndex(ExecutionContext execCxt, DatasetGraph dsg) {
        Object obj = execCxt.getContext().get(TextQuery.textIndex);
        if (obj instanceof TextIndexLucene) {
            return (TextIndexLucene) obj;
        }
        if (obj != null) {
            Log.warn(TextFacetPF.class, "Context setting '" + TextQuery.textIndex + "' is not a TextIndexLucene");
        }
        if (dsg instanceof DatasetGraphText) {
            TextIndex ti = ((DatasetGraphText) dsg).getTextIndex();
            if (ti instanceof TextIndexLucene) {
                return (TextIndexLucene) ti;
            }
            Log.warn(TextFacetPF.class, "TextIndex is not a TextIndexLucene - faceting not supported");
        }
        Log.warn(TextFacetPF.class, "Failed to find the text index");
        return null;
    }

    @Override
    public QueryIterator exec(Binding binding,
                              PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                              ExecutionContext execCxt) {
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

        // Get facet counts (via SearchExecution if filters present, direct otherwise)
        Map<String, List<FacetValue>> facetCounts;
        try {
            log.debug("TextFacetPF: filters={} queryString='{}' facetFields={}", args.filters, args.queryString, args.facetFields);
            if (args.filters != null && !args.filters.isEmpty()) {
                SearchExecution se = SearchExecution.getOrCreate(
                    execCxt, args.props, args.queryString,
                    args.filters, textIndex, null, null);
                facetCounts = se.getFacetCounts(args.facetFields, args.maxValues, args.minCount);
            } else {
                facetCounts = textIndex.getFacetCounts(args.queryString, args.facetFields, args.maxValues, args.minCount);
            }
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
     * Parse the object argument list into structured facet parameters.
     * <p>
     * Argument order:
     * 1. While arg is URI: collect as properties
     * 2. First plain literal (not JSON, not integer): query string
     * 3. Remaining literals:
     *    - starts with '[': JSON array of facet field names
     *    - starts with '{': JSON object of filter map
     *    - parseable as integer: maxValues
     */
    private FacetArgs parseObjectArgs(PropFuncArg argObject) {
        List<Resource> props = new ArrayList<>();
        String queryString = null;
        List<String> facetFields = new ArrayList<>();
        Map<String, List<String>> filters = null;
        int maxValues = 10;
        int minCount = 0;
        boolean maxValuesSet = false;

        if (argObject.isNode()) {
            // Single arg - must be a query string, but we need facet fields too
            log.warn("text:facet requires at least a query string and facet fields");
            return null;
        }

        List<Node> list = argObject.getArgList();
        int idx = 0;

        // 1. Collect property URIs
        while (idx < list.size() && list.get(idx).isURI()) {
            Node n = list.get(idx);
            Property prop = ResourceFactory.createProperty(n.getURI());
            props.add(prop);
            idx++;
        }

        // 2. Query string (first plain literal that's not JSON and not an integer)
        if (idx < list.size() && list.get(idx).isLiteral()) {
            String lex = list.get(idx).getLiteralLexicalForm();
            if (!lex.startsWith("[") && !lex.startsWith("{") && !isInteger(lex)) {
                queryString = lex;
                idx++;
            }
        }

        // 3. Parse remaining: JSON arrays, JSON objects, and integers
        // First integer = maxValues, second integer = minCount
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
                    // JSON object: filter map
                    filters = parseJsonFilters(lex);
                } else if (isInteger(lex)) {
                    if (!maxValuesSet) {
                        maxValues = Integer.parseInt(lex);
                        maxValuesSet = true;
                    } else {
                        minCount = Integer.parseInt(lex);
                    }
                } else {
                    // Unrecognized literal - could be a query string if we haven't seen one
                    if (queryString == null) {
                        queryString = lex;
                    } else {
                        log.warn("Unexpected argument in text:facet: {}", lex);
                    }
                }
            }
            idx++;
        }

        return new FacetArgs(props, queryString, facetFields, filters, maxValues, minCount);
    }

    /**
     * Parse a JSON object string into a filter map.
     * Expected format: {"field": ["value1", "value2"], "field2": ["value3"]}
     */
    static Map<String, List<String>> parseJsonFilters(String jsonStr) {
        Map<String, List<String>> filters = new java.util.LinkedHashMap<>();
        org.apache.jena.atlas.json.JsonObject json = JSON.parse(jsonStr);
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

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static class FacetArgs {
        final List<Resource> props;
        final String queryString;
        final List<String> facetFields;
        final Map<String, List<String>> filters;
        final int maxValues;
        final int minCount;

        FacetArgs(List<Resource> props, String queryString, List<String> facetFields,
                  Map<String, List<String>> filters, int maxValues, int minCount) {
            this.props = props;
            this.queryString = queryString;
            this.facetFields = facetFields;
            this.filters = filters;
            this.maxValues = maxValues;
            this.minCount = minCount;
        }
    }
}
