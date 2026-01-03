/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.clauses;

import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.handlers.PrologHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

/**
 * Interface that defines the PrologClause as per
 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rPrologue
 * 
 * @param <T> The Builder type that the clause is part of.
 */
public interface PrologClause<T extends AbstractQueryBuilder<T>> {
    /**
     * Get the prolog handler for this clause.
     * 
     * @return The PrologHandler this clause is using.
     */
    public PrologHandler getPrologHandler();

    /**
     * Get the expression factory that works with the prefixes for this builder.
     * 
     * @return an ExprFactory for this builder.
     */
    public ExprFactory getExprFactory();

    /**
     * Adds a prefix.
     * 
     * @param pfx The prefix.
     * @param uri The URI for the prefix
     * @return This builder for chaining.
     */
    public T addPrefix(String pfx, Resource uri);

    /**
     * Adds a prefix.
     * 
     * @param pfx The prefix.
     * @param uri The URI for the prefix
     * @return This builder for chaining.
     */
    public T addPrefix(String pfx, Node uri);

    /**
     * Adds a prefix.
     * 
     * @param pfx The prefix.
     * @param uri The URI for the prefix
     * @return This builder for chaining.
     */
    public T addPrefix(String pfx, String uri);

    /**
     * Adds prefixes.
     * 
     * @param prefixes A mapping of prefix to URI to add.
     * @return This builder for chaining.
     */
    public T addPrefixes(Map<String, String> prefixes);

    /**
     * Adds prefixes.
     * 
     * @param prefixes A PrefixMapping instance..
     * @return This builder for chaining.
     */
    public T addPrefixes(PrefixMapping prefixes);

    /**
     * Sets the base URI.
     * 
     * See {@link AbstractQueryBuilder#makeNode} for conversion of the uri param.
     * The resulting Node must be a URI.
     * 
     * @param uri The base URI to use.
     * @return This builder for chaining.
     */
    public T setBase(Object uri);

    /**
     * Sets the base URI.
     * 
     * @param uri The base URI to use.
     * @return This builder for chaining.
     */
    public T setBase(String uri);

}
