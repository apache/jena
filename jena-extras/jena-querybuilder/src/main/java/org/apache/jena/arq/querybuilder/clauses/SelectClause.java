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

import java.util.List;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.handlers.SelectHandler;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

/**
 * Interface that defines the SelectClause as per
 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rSelectClause
 * 
 * @param <T>
 *            The Builder type that the clause is part of.
 */
public interface SelectClause<T extends AbstractQueryBuilder<T>> {
	/**
	 * Get the select handler for this clause
	 * 
	 * @return The SelectHandler that the clause is using
	 */
	public SelectHandler getSelectHandler();

	

	/**
	 * Add a variable to the select statement.
	 * 
	 * A variable may only be added once. Attempting to add the same variable
	 * multiple times will be silently ignored.
	 * 
	 * @param var
	 *            The variable to add.
	 * @return The builder for chaining.
	 */
	public T addVar(Object var);

	/**
	 * Add an expression as variable to the select statement.
	 * 
	 * creates an '(Expression as Var)' to the select statement.
	 * 
	 * A variable may only be added once. Attempting to add the same variable
	 * multiple times will be silently ignored.
	 * 
	 * @param expr
	 *            The expression to be added
	 * @param var
	 *            The variable to add.
	 * @return The builder for chaining.
	 */
	public T addVar(Expr expr, Object var);

	/**
	 * Add an expression as variable to the select statement.
	 * 
	 * creates an '(Expression as Var)' to the select statement.
	 * 
	 * A variable may only be added once. Attempting to add the same variable
	 * multiple times will be silently ignored.
	 * 
	 * @param expr
	 *            The expression to be added
	 * @param var
	 *            The variable to add.
	 * @return The builder for chaining.
	 * @throws ParseException
	 */
	public T addVar(String expr, Object var) throws ParseException;

	/**
	 * @return A list of all the variables that have been added.
	 */
	public List<Var> getVars();

}
