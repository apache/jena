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
package org.apache.jena.arq.querybuilder;

import java.util.Collection;
import java.util.List;

import org.apache.jena.arq.querybuilder.clauses.DatasetClause;
import org.apache.jena.arq.querybuilder.clauses.SelectClause;
import org.apache.jena.arq.querybuilder.clauses.SolutionModifierClause;
import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.arq.querybuilder.handlers.DatasetHandler;
import org.apache.jena.arq.querybuilder.handlers.SelectHandler;
import org.apache.jena.arq.querybuilder.handlers.SolutionModifierHandler;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.FrontsNode ;
import org.apache.jena.graph.FrontsTriple ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException ;

/**
 * Build a select query.
 *
 */
public class SelectBuilder extends AbstractQueryBuilder<SelectBuilder>
		implements DatasetClause<SelectBuilder>, WhereClause<SelectBuilder>,
		SolutionModifierClause<SelectBuilder>, SelectClause<SelectBuilder> {

	// the handlers.
	private DatasetHandler datasetHandler;
	private WhereHandler whereHandler;
	private SolutionModifierHandler solutionModifier;
	private SelectHandler selectHandler;

	/**
	 * Constructor.
	 */
	public SelectBuilder() {
		super();
		query.setQuerySelectType();
		datasetHandler = new DatasetHandler(query);
		whereHandler = new WhereHandler(query);
		solutionModifier = new SolutionModifierHandler(query);
		selectHandler = new SelectHandler(query);
	}

	@Override
	public DatasetHandler getDatasetHandler() {
		return datasetHandler;
	}

	@Override
	public WhereHandler getWhereHandler() {
		return whereHandler;
	}

	@Override
	public SelectBuilder clone() {
		SelectBuilder qb = new SelectBuilder();
		qb.prologHandler.addAll(prologHandler);
		qb.datasetHandler.addAll(datasetHandler);
		qb.whereHandler.addAll(whereHandler);
		qb.solutionModifier.addAll(solutionModifier);
		qb.selectHandler.addAll(selectHandler);

		return qb;
	}

	@Override
	public SelectBuilder setDistinct(boolean state) {
		selectHandler.setDistinct(state);
		return this;
	}

	@Override
	public SelectBuilder setReduced(boolean state) {
		selectHandler.setReduced(state);
		return this;
	}

	@Override
	public SelectBuilder addVar(Object var) {
		selectHandler.addVar(makeVar(var));
		return this;
	}

	/**
	 * Add an expression string as a filter.
	 * @param expression The expression string to add.
	 * @throws ParseException If the expression can not be parsed.
	 */
	@Override
	public SelectBuilder addVar(String expression, Object var) throws ParseException {
		selectHandler.addVar( expression, makeVar(var) );
		return this;
	}

	@Override
	public SelectBuilder addVar(Expr expr, Object var) {
		selectHandler.addVar(expr, makeVar(var));
		return this;
	}
	
	@Override
	public List<Var> getVars() {
		return selectHandler.getVars();
	}

	@Override
	public SelectBuilder fromNamed(String graphName) {
		datasetHandler.fromNamed(graphName);
		return this;
	}

	@Override
	public SelectBuilder fromNamed(Collection<String> graphNames) {
		datasetHandler.fromNamed(graphNames);
		return this;
	}

	@Override
	public SelectBuilder from(String graphName) {
		datasetHandler.from(graphName);
		return this;
	}

	@Override
	public SelectBuilder from(Collection<String> graphName) {
		datasetHandler.from(graphName);
		return this;
	}

	@Override
	public SelectBuilder addOrderBy(String orderBy) {
		solutionModifier.addOrderBy(orderBy);
		return this;
	}

	@Override
	public SelectBuilder addGroupBy(String groupBy) {
		solutionModifier.addGroupBy(groupBy);
		return this;
	}

	@Override
	public SolutionModifierHandler getSolutionModifierHandler() {
		return solutionModifier;
	}

	@Override
	public SelectBuilder addHaving(String having) throws ParseException {
		solutionModifier.addHaving(having);
		return this;
	}

	@Override
	public SelectBuilder setLimit(int limit) {
		solutionModifier.setLimit(limit);
		return this;
	}

	@Override
	public SelectBuilder setOffset(int offset) {
		solutionModifier.setOffset(offset);
		return this;
	}

	/**
	 * Convert a node to a string.
	 * If the node is a literal return the literal value.
	 * If the node is a URI return the URI enclosed with &lt; and &gt;
	 * If the node is a variable return the name preceeded by '?'
	 * @param node The node to convert.
	 * @return A string representation of the node.
	 */
	private static String toString(Node node) {
		if (node.isBlank()) {
			return node.getBlankNodeLabel();
		}
		if (node.isLiteral()) {
			return node.toString();
		}
		if (node.isURI()) {
			return String.format("<%s>", node.getURI());
		}
		if (node.isVariable()) {
			return String.format("?%s", node.getName());
		}
		return node.toString();
	}

	/**
	 * Convert the object to a string.
	 * If the object is a node or fronts a node then 
	 * <ul>
	 * <li>If the node is a literal return the literal value.</li>
	 * <li>If the node is a URI return the URI enclosed with &lt; and &gt;</li>
	 * <li>If the node is a variable return the name preceeded by '?'</li>
	 * </ul>
	 * otherwise return the toString() method of the object.
	 * @param o the Object to convert.
	 * @return The string representation of the object.
	 */
	public static String makeString(Object o) {
		if (o instanceof FrontsNode) {
			return toString(((FrontsNode) o).asNode());
		}
		if (o instanceof Node) {
			return toString((Node) o);
		}
		return o.toString();
	}

	@Override
	public SelectBuilder addWhere(Triple t) {
		whereHandler.addWhere(t);
		return this;
	}

	@Override
	public SelectBuilder addWhere(FrontsTriple t) {
		whereHandler.addWhere(t.asTriple());
		return this;
	}

	@Override
	public SelectBuilder addWhere(Object s, Object p, Object o) {
		addWhere(new Triple(makeNode(s), makeNode(p), makeNode(o)));
		return this;
	}

	@Override
	public SelectBuilder addOptional(Triple t) {
		whereHandler.addOptional(t);
		return this;
	}

	@Override
	public SelectBuilder addOptional(FrontsTriple t) {
		whereHandler.addOptional(t.asTriple());
		return this;
	}

	@Override
	public SelectBuilder addOptional(Object s, Object p, Object o) {
		addOptional(new Triple(makeNode(s), makeNode(p), makeNode(o)));
		return this;
	}

	@Override
	public SelectBuilder addFilter(String s) throws ParseException {
		whereHandler.addFilter(s);
		return this;
	}

	@Override
	public SelectBuilder addSubQuery(SelectBuilder subQuery) {
		prologHandler.addAll(subQuery.prologHandler);
		whereHandler.addSubQuery(subQuery);
		return this;
	}

	@Override
	public SelectBuilder addUnion(SelectBuilder subQuery) {
		whereHandler.addUnion(subQuery);
		return this;
	}

	@Override
	public SelectBuilder addGraph(Object graph, SelectBuilder subQuery) {
		prologHandler.addAll(subQuery.prologHandler);
		whereHandler.addGraph(makeNode(graph), subQuery.whereHandler);
		return this;
	}

	@Override
	public SelectBuilder addBind(Expr expression, Object var) {
		whereHandler.addBind( expression, makeVar(var) );
		return this;
	}

	@Override
	public SelectBuilder addBind(String expression, Object var) throws ParseException {
		whereHandler.addBind( expression, makeVar(var) );
		return this;
	}

	@Override
	public SelectHandler getSelectHandler() {
		return selectHandler;
	}
}