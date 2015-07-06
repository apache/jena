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
import org.apache.jena.arq.querybuilder.handlers.SolutionModifierHandler;
import org.apache.jena.sparql.lang.sparql_11.ParseException ;

/**
 * Interface that defines the SolutionClause as per
 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rSolutionModifier
 * 
 * @param <T>
 *            The Builder type that the clause is part of.
 */
public interface SolutionModifierClause<T extends AbstractQueryBuilder<T>> {

	/**
	 * Add an order by
	 * 
	 * @param orderBy
	 *            The variable name to order by.
	 * @return The builder for chaining.
	 */
	public T addOrderBy(String orderBy);

	/**
	 * Add a group by
	 * 
	 * @param groupBy
	 *            The variable name to group by.
	 * @return The builder for chaining.
	 */
	public T addGroupBy(String groupBy);

	/**
	 * Add a having expression.
	 * 
	 * @param expression
	 *            Expression to evaluate for the having.
	 * @return The builder for chaining.
	 */
	public T addHaving(String expression) throws ParseException;

	/**
	 * Set the limit.
	 * 
	 * Setting the limit to 0 (zero) sets no limit.
	 * 
	 * @param limit
	 *            the maximum number of results to return.
	 * @return The builder for chaining.
	 */
	public T setLimit(int limit);

	/**
	 * Set the offset.
	 * 
	 * Setting the offset to 0 (zero) sets no offset.
	 * 
	 * @param offset
	 *            the number of results to skip before returning results..
	 * @return The builder for chaining.
	 */
	public T setOffset(int offset);

	/**
	 * Get the Solution modifier for this clause.
	 * @return The SolutionModifierHandler the clause is using.
	 */
	public SolutionModifierHandler getSolutionModifierHandler();

}
