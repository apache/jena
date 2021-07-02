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

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.handlers.ConstructHandler;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Triple;

/**
 * Interface that defines the ConstructClause as per
 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rConstructTemplate
 * 
 * @param <T> The Builder type that the clause is part of.
 */
public interface ConstructClause<T extends AbstractQueryBuilder<T>> {

    /**
     * Add a construct triple.
     * 
     * @param t The triple to add.
     * @return The builder for chaining.
     */
    public T addConstruct(Triple t);

    /**
     * Add a construct triple.
     * 
     * @param t The triple to add.
     * @return The builder for chaining.
     */
    public T addConstruct(FrontsTriple t);

    /**
     * Add a construct triple.
     * 
     * See {@link AbstractQueryBuilder#makeNode} for conversion of the param values.
     * 
     * @param s The subject of the triple,
     * @param p The predicate of the triple.
     * @param o The object of the triple.
     * @return the builder for chaining.
     */
    public T addConstruct(Object s, Object p, Object o);

    /**
     * Get the Construct handler for this construct clause.
     * 
     * @return the construct handler used by this builder.
     */
    public ConstructHandler getConstructHandler();

}
