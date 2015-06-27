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
package org.apache.jena.arq.querybuilder.handlers;

import java.util.List ;
import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.util.ExprUtils ;

/**
 * A Select clause handler.
 *
 */
public class SelectHandler implements Handler {

	// the query to handle
	private final Query query;

	/**
	 * Constructor.
	 * @param query The query to manage.
	 */
	public SelectHandler(Query query) {
		this.query = query;
		setDistinct(query.isDistinct());
		setReduced(query.isReduced());
	}

	/**
	 * Set the distinct flag.
	 * Set or unset the distinct flag.
	 * Will set the reduced flag if it was previously set.
	 * @param state the state to set the distinct flag to.
	 */
	public void setDistinct(boolean state) {
		query.setDistinct(state);
		if (state) {
			query.setReduced(false);
		}
	}

	/**
	 * Set the reduced flag.
	 * Set or unset the reduced flag.
	 * Will set the reduced flag if it was previously set.
	 * @param state the state to set the reduced flag to.
	 */
	public void setReduced(boolean state) {
		query.setReduced(state);
		if (state) {
			query.setDistinct(false);
		}
	}

	/**
	 * Add a variable to the select.
	 * If the variable is <code>null</code> the variables are set to star.
	 * @param var The variable to add.
	 */
	public void addVar(Var var) {
		if (var == null) {
			query.setQueryResultStar(true);
		} else {
			query.setQueryResultStar(false);
			query.addResultVar(var);
		}
	}

	/** Add an Expression as variable to the select.
	 * If the variable is the variables are set to star.
	 * @param expression The expression as a string.
	 * @param var The variable to add.
	 */
	public void addVar(String expression, Var var)  {
		addVar( ExprUtils.parse( query, expression, true ), var );
	}
	
	/**
	 * Add an Expression as variable to the select.
	 * @param expr The expresson to add.
	 * @param var The variable to add.
	 */
	public void addVar(Expr expr, Var var) {
		if (expr ==null)
		{
			throw new IllegalArgumentException( "expr may not be null");
		}
		if (var == null)
		{
			throw new IllegalArgumentException( "var may not be null");
		}
			query.setQueryResultStar(false);
			query.addResultVar(var, expr);
	}
	
	/**
	 * Get the list of variables from the query.
	 * @return The list of variables in the query.
	 */
	public List<Var> getVars() {
		return query.getProjectVars();
	}

	/**
	 * Add all the variables from the select handler variable.
	 * @param selectHandler The select handler to copy the variables from.
	 */
	public void addAll(SelectHandler selectHandler) {

		setReduced(selectHandler.query.isReduced());
		setDistinct(selectHandler.query.isDistinct());
		query.setQueryResultStar(selectHandler.query.isQueryResultStar());
		VarExprList shProjectVars = selectHandler.query.getProject();
		VarExprList qProjectVars = query.getProject();
		for (Var var : shProjectVars.getVars())
		{
			qProjectVars.add( var, shProjectVars.getExpr(var));
		}
	}

	@Override
	public void setVars(Map<Var, Node> values) {
		// nothing to do
	}

	@Override
	public void build() {
		if (query.getProject().getVars().isEmpty()) {
			query.setQueryResultStar(true);
		}
		// handle the SELECT * case
		query.getProjectVars();
	}
}
