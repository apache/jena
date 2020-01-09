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
import java.util.Map;

import org.apache.jena.arq.querybuilder.clauses.DatasetClause;
import org.apache.jena.arq.querybuilder.clauses.SelectClause;
import org.apache.jena.arq.querybuilder.clauses.SolutionModifierClause;
import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.arq.querybuilder.handlers.DatasetHandler;
import org.apache.jena.arq.querybuilder.handlers.HandlerBlock;
import org.apache.jena.arq.querybuilder.handlers.SelectHandler;
import org.apache.jena.arq.querybuilder.handlers.SolutionModifierHandler;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

/**
 * Builder for SPARQL Select Queries.
 * <p>
 * The SelectBuilder provides chainable methods to programmatically generate SPARQL Select Queries.
 * The application order of the methods is not relevant for the resulting query.
 * An {@link ExprFactory} is intended for use along with the SelectBuilder to generate needed {@link Expr} parameter values.
 * An {@link ExprFactory} that works with the same prefixes can be obtained with {@link SelectBuilder#getExprFactory()}.
 * <p>
 * The SelectBuilder can be used as <b>prepared query</b>.
 * Values for variables in the created query can be set with {@link SelectBuilder#setVar(Object, Object)} and {@link SelectBuilder#setVar(Var, Node)}.
 * The method {@link SelectBuilder#clearWhereValues()} allows to clear the set values. 
 * 
 * @see AskBuilder
 * @see ConstructBuilder
 * @see DescribeBuilder
 * @see UpdateBuilder
 */
public class SelectBuilder extends AbstractQueryBuilder<SelectBuilder> implements DatasetClause<SelectBuilder>,
		WhereClause<SelectBuilder>, SolutionModifierClause<SelectBuilder>, SelectClause<SelectBuilder> {

	private final HandlerBlock handlerBlock;

	public SelectBuilder() {
		super();
		query.setQuerySelectType();
		handlerBlock = new HandlerBlock(query);
	}

	@Override
	public DatasetHandler getDatasetHandler() {
		return handlerBlock.getDatasetHandler();
	}

	@Override
	public HandlerBlock getHandlerBlock() {
		return handlerBlock;
	}

	@Override
	public SelectBuilder clone() {
		SelectBuilder qb = new SelectBuilder();
		qb.handlerBlock.addAll(handlerBlock);
		return qb;
	}

	/**
	 * Sets the distinct flag.
	 * 
	 * Setting the select distinct will unset reduced if it was set.
	 * 
	 * @param state
	 *            if true results will be distinct.
	 * @return This builder for chaining.
	 */
	public SelectBuilder setDistinct(boolean state) {
		getSelectHandler().setDistinct(state);
		return this;
	}

	/**
	 * Sets the reduced flag.
	 * 
	 * Setting the select reduced will unset distinct if it was set.
	 * 
	 * @param state
	 *            if true results will be reduced.
	 * @return This builder for chaining.
	 */
	public SelectBuilder setReduced(boolean state) {
		getSelectHandler().setReduced(state);
		return this;
	}

	@Override
	public SelectBuilder addVar(Object var) {
		getSelectHandler().addVar(makeVar(var));
		return this;
	}

	@Override
	public SelectBuilder addVar(String expression, Object var) throws ParseException {
		getSelectHandler().addVar(expression, makeVar(var));
		return this;
	}

	@Override
	public SelectBuilder addVar(Expr expr, Object var) {
		getSelectHandler().addVar(expr, makeVar(var));
		return this;
	}

	@Override
	public List<Var> getVars() {
		return getSelectHandler().getVars();
	}

	@Override
	public SelectBuilder fromNamed(String graphName) {
		getDatasetHandler().fromNamed(graphName);
		return this;
	}

	@Override
	public SelectBuilder fromNamed(Collection<String> graphNames) {
		getDatasetHandler().fromNamed(graphNames);
		return this;
	}

	@Override
	public SelectBuilder from(String graphName) {
		getDatasetHandler().from(graphName);
		return this;
	}

	@Override
	public SelectBuilder from(Collection<String> graphName) {
		getDatasetHandler().from(graphName);
		return this;
	}

	@Override
	public SelectBuilder addOrderBy(Expr orderBy) {
		getSolutionModifierHandler().addOrderBy(orderBy);
		return this;
	}

	@Override
	public SelectBuilder addOrderBy(Object orderBy) {
		getSolutionModifierHandler().addOrderBy(makeVar(orderBy));
		return this;
	}

	@Override
	public SelectBuilder addOrderBy(SortCondition orderBy) {
		getSolutionModifierHandler().addOrderBy(orderBy);
		return this;
	}

	@Override
	public SelectBuilder addOrderBy(Expr orderBy, Order order) {
		getSolutionModifierHandler().addOrderBy(orderBy, order);
		return this;
	}

	@Override
	public SelectBuilder addOrderBy(Object orderBy, Order order) {
		getSolutionModifierHandler().addOrderBy(makeVar(orderBy), order);
		return this;
	}

	@Override
	public SelectBuilder addGroupBy(Object groupBy) {
		getSolutionModifierHandler().addGroupBy(makeVar(groupBy));
		return this;
	}

	@Override
	public SelectBuilder addGroupBy(Expr groupBy) {
		getSolutionModifierHandler().addGroupBy(groupBy);
		return this;
	}

	@Override
	public SelectBuilder addGroupBy(Object var, Expr expr) {
		getSolutionModifierHandler().addGroupBy(makeVar(var), expr);
		return this;
	}

	@Override
	public SelectBuilder addGroupBy(Object var, String expr) {
		getSolutionModifierHandler().addGroupBy(makeVar(var), makeExpr(expr));
		return this;
	}

	@Override
	public SolutionModifierHandler getSolutionModifierHandler() {
		return handlerBlock.getModifierHandler();
	}

	@Override
	public SelectBuilder addHaving(String having) throws ParseException {
		getSolutionModifierHandler().addHaving(having);
		return this;
	}

	@Override
	public SelectBuilder addHaving(Expr expression) throws ParseException {
		getSolutionModifierHandler().addHaving(expression);
		return this;
	}

	@Override
	public SelectBuilder addHaving(Object var) throws ParseException {
		getSolutionModifierHandler().addHaving(makeVar(var));
		return this;
	}

	@Override
	public SelectBuilder setLimit(int limit) {
		getSolutionModifierHandler().setLimit(limit);
		return this;
	}

	@Override
	public SelectBuilder setOffset(int offset) {
		getSolutionModifierHandler().setOffset(offset);
		return this;
	}

	/**
	 * Converts a node to a string. If the node is a literal return the literal
	 * value. If the node is a URI return the URI enclosed with &lt; and &gt; If
	 * the node is a variable return the name preceded by '?'
	 * 
	 * @param node
	 *            The node to convert.
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
	 * Converts the object to a string. If the object is a node or fronts a node
	 * then
	 * <ul>
	 * <li>If the node is a literal return the literal value.</li>
	 * <li>If the node is a URI return the URI enclosed with &lt; and &gt;</li>
	 * <li>If the node is a variable return the name preceded by '?'</li>
	 * </ul>
	 * otherwise return the toString() method of the object.
	 * 
	 * @param o
	 *            the Object to convert.
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
	public SelectBuilder addWhere(TriplePath t )
	{
		getWhereHandler().addWhere(t);
		return this;
	}
	
	@Override
	public SelectBuilder addWhere(Triple t) {
		getWhereHandler().addWhere(new TriplePath(t));
		return this;
	}

	@Override
	public SelectBuilder addWhere(FrontsTriple t) {
		getWhereHandler().addWhere(new TriplePath(t.asTriple()));
		return this;
	}

	@Override
	public SelectBuilder addWhere(Object s, Object p, Object o) {
		getWhereHandler().addWhere( makeTriplePath( s, p, o ));
		return this;
	}

	@Override
	public SelectBuilder addWhereValueVar(Object var) {
		getWhereHandler().addValueVar(getPrologHandler().getPrefixes(), var);
		return this;
	}
	
	@Override
	public SelectBuilder addWhereValueVar(Object var, Object... values)
	{
		getWhereHandler().addValueVar(getPrologHandler().getPrefixes(), var, values);
		return this;
	}
	
	
	@Override
	public <K extends Collection<?>> SelectBuilder addWhereValueVars(Map<?,K> dataTable)
	{
		getWhereHandler().addValueVars(getPrologHandler().getPrefixes(), dataTable);
		return this;
	}
	
	@Override
	public SelectBuilder addWhereValueRow(Object... values)
	{
		getWhereHandler().addValueRow(getPrologHandler().getPrefixes(), values);
		return this;
	}

	@Override
	public SelectBuilder addWhereValueRow(Collection<?> values) {
		getWhereHandler().addValueRow(getPrologHandler().getPrefixes(), values);
		return this;
	}

	@Override
	public List<Var> getWhereValuesVars() {
		return getWhereHandler().getValuesVars();
	}

	@Override
	public Map<Var, List<Node>> getWhereValuesMap() {
		return getWhereHandler().getValuesMap();
	}

	@Override
	public SelectBuilder clearWhereValues() {
		getWhereHandler().clearValues();
		return this;
	}
	
	@Override
	public SelectBuilder addOptional(TriplePath t)
	{
		getWhereHandler().addOptional( t );
		return this;
	}
	@Override
	public SelectBuilder addOptional(Triple t) {
		getWhereHandler().addOptional(new TriplePath(t));
		return this;
	}

	@Override
	public SelectBuilder addOptional(FrontsTriple t) {
		getWhereHandler().addOptional(new TriplePath(t.asTriple()));
		return this;
	}

	@Override
	public SelectBuilder addOptional(Object s, Object p, Object o) {
		getWhereHandler().addOptional( makeTriplePath( s, p, o ));
		return this;
	}

	@Override
	public SelectBuilder addOptional(AbstractQueryBuilder<?> t) {
		getWhereHandler().addOptional(t.getWhereHandler());
		return this;
	}

	@Override
	public SelectBuilder addFilter(Expr expr) {
		getWhereHandler().addFilter(expr);
		return this;
	}
	
	@Override
	public SelectBuilder addFilter(String s) throws ParseException {
		getWhereHandler().addFilter(s);
		return this;
	}

	@Override
	public SelectBuilder addSubQuery(AbstractQueryBuilder<?> subQuery) {
		getWhereHandler().addSubQuery(subQuery);
		return this;
	}

	@Override
	public SelectBuilder addUnion(AbstractQueryBuilder<?> subQuery) {
		getWhereHandler().addUnion(subQuery);
		return this;
	}

	@Override
	public SelectBuilder addGraph(Object graph, AbstractQueryBuilder<?> subQuery) {
		getPrologHandler().addAll(subQuery.getPrologHandler());
		getWhereHandler().addGraph(makeNode(graph), subQuery.getWhereHandler());
		return this;
	}
	@Override
	public SelectBuilder addGraph(Object graph, FrontsTriple triple) {
		getWhereHandler().addGraph(makeNode(graph), new TriplePath(triple.asTriple()));
		return this;
	}
	@Override
	public SelectBuilder addGraph(Object graph, Object subject, Object predicate, Object object)
	{
		getWhereHandler().addGraph(makeNode(graph), makeTriplePath( subject, predicate, object ));
		return this;
	}
	@Override
	public SelectBuilder addGraph(Object graph, Triple triple) {
		getWhereHandler().addGraph(makeNode(graph), new TriplePath(triple));
		return this;
	}
	@Override
	public SelectBuilder addGraph(Object graph, TriplePath triplePath) {
		getWhereHandler().addGraph(makeNode(graph), triplePath );
		return this;
	}

	@Override
	public SelectBuilder addBind(Expr expression, Object var) {
		getWhereHandler().addBind(expression, makeVar(var));
		return this;
	}

	@Override
	public SelectBuilder addBind(String expression, Object var) throws ParseException {
		getWhereHandler().addBind(expression, makeVar(var));
		return this;
	}

	@Override
	public SelectHandler getSelectHandler() {
		return handlerBlock.getSelectHandler();
	}

	@Override
	public Node list(Object... objs) {
		return getWhereHandler().list(objs);
	}
	
	@Override
	public SelectBuilder addMinus( AbstractQueryBuilder<?> t ) {
		getWhereHandler().addMinus( t );
		return this;
	}

	
}
