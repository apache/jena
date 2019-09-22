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
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

/**
 * Builder for SPARQL Describe Queries.
 * <p>
 * The DescribeBuilder provides chainable methods to programmatically generate SPARQL Describe Queries.
 * The application order of the methods is not relevant for the resulting query.
 * An {@link ExprFactory} is intended for use along with the DescribeBuilder to generate needed {@link Expr} parameter values.
 * An {@link ExprFactory} that works with the same prefixes can be obtained with {@link DescribeBuilder#getExprFactory()}.
 * <p>
 * The DescribeBuilder can be used as <b>prepared query</b>.
 * Values for variables in the created query can be set with {@link DescribeBuilder#setVar(Object, Object)} and {@link DescribeBuilder#setVar(Var, Node)}.
 * The method {@link DescribeBuilder#clearWhereValues()} allows to clear the set values. 
 * 
 * @see AskBuilder
 * @see ConstructBuilder
 * @see SelectBuilder
 * @see UpdateBuilder
 */
public class DescribeBuilder extends AbstractQueryBuilder<DescribeBuilder> implements 
DatasetClause<DescribeBuilder>,
	WhereClause<DescribeBuilder>, SolutionModifierClause<DescribeBuilder>, SelectClause<DescribeBuilder>{

	private final HandlerBlock handlerBlock;

	public DescribeBuilder() {
		super();
		query.setQueryDescribeType();
		handlerBlock = new HandlerBlock(query);
	}
	
	@Override
	public SelectHandler getSelectHandler() {
		return handlerBlock.getSelectHandler();
	}

	@Override
	public DescribeBuilder addVar(Object var) {
		getSelectHandler().addVar(makeVar(var));
		return this;
	}

	@Override
	public DescribeBuilder addVar(Expr expr, Object var) {
		getSelectHandler().addVar(expr, makeVar(var));
		return this;
	}

	@Override
	public DescribeBuilder addVar(String expr, Object var) throws ParseException {
		getSelectHandler().addVar(expr, makeVar(var));
		return this;
	}

	@Override
	public List<Var> getVars() {
		return getSelectHandler().getVars();
	}

	@Override
	public DescribeBuilder addOrderBy(Expr orderBy) {
		getSolutionModifierHandler().addOrderBy(orderBy);
		return this;
	}

	@Override
	public DescribeBuilder addOrderBy(Object orderBy) {
		getSolutionModifierHandler().addOrderBy(makeVar(orderBy));
		return this;
	}

	@Override
	public DescribeBuilder addOrderBy(SortCondition orderBy) {
		getSolutionModifierHandler().addOrderBy(orderBy);
		return this;
	}

	@Override
	public DescribeBuilder addOrderBy(Expr orderBy, Order order) {
		getSolutionModifierHandler().addOrderBy(orderBy, order);
		return this;
	}

	@Override
	public DescribeBuilder addOrderBy(Object orderBy, Order order) {
		getSolutionModifierHandler().addOrderBy(makeVar(orderBy), order);
		return this;
	}

	@Override
	public DescribeBuilder addGroupBy(Object groupBy) {
		getSolutionModifierHandler().addGroupBy(makeVar(groupBy));
		return this;
	}

	@Override
	public DescribeBuilder addGroupBy(Expr groupBy) {
		getSolutionModifierHandler().addGroupBy(groupBy);
		return this;
	}

	@Override
	public DescribeBuilder addGroupBy(Object var, Expr expr) {
		getSolutionModifierHandler().addGroupBy(makeVar(var), expr);
		return this;
	}

	@Override
	public DescribeBuilder addGroupBy(Object var, String expr) {
		getSolutionModifierHandler().addGroupBy(makeVar(var), makeExpr(expr));
		return this;
	}

	@Override
	public DescribeBuilder addHaving(String expression) throws ParseException {
		getSolutionModifierHandler().addHaving(expression);
		return this;
	}

	@Override
	public DescribeBuilder addHaving(Expr expression) throws ParseException {
		getSolutionModifierHandler().addHaving(expression);
		return this;
	}

	@Override
	public DescribeBuilder addHaving(Object var) throws ParseException {
		getSolutionModifierHandler().addHaving(makeVar(var));
		return this;
	}

	@Override
	public DescribeBuilder setLimit(int limit) {
		getSolutionModifierHandler().setLimit(limit);
		return this;
	}

	@Override
	public DescribeBuilder setOffset(int offset) {
		getSolutionModifierHandler().setOffset(offset);
		return this;
	}

	@Override
	public SolutionModifierHandler getSolutionModifierHandler() {
		return handlerBlock.getModifierHandler();
	}

	@Override
	public DescribeBuilder addWhere(Triple t) {
		getWhereHandler().addWhere(new TriplePath(t));
		return this;
	}

	@Override
	public DescribeBuilder addWhere(TriplePath t) {
		getWhereHandler().addWhere(t);
		return this;
	}

	@Override
	public DescribeBuilder addWhere(FrontsTriple t) {
		getWhereHandler().addWhere(new TriplePath(t.asTriple()));
		return this;
	}

	@Override
	public DescribeBuilder addWhere(Object s, Object p, Object o) {
		getWhereHandler().addWhere( makeTriplePath(s,p,o));
		return this;
	}

	@Override
	public DescribeBuilder addWhereValueVar(Object var) {
		getWhereHandler().addValueVar(getPrologHandler().getPrefixes(), var);
		return this;
	}
	
	@Override
	public DescribeBuilder addWhereValueVar(Object var, Object... values)
	{
		getWhereHandler().addValueVar(getPrologHandler().getPrefixes(), var, values);
		return this;
	}
		
	@Override
	public <K extends Collection<?>> DescribeBuilder addWhereValueVars(Map<?,K> dataTable)
	{
		getWhereHandler().addValueVars(getPrologHandler().getPrefixes(), dataTable);
		return this;
	}
	
	@Override
	public DescribeBuilder addWhereValueRow(Object... values)
	{
		getWhereHandler().addValueRow(getPrologHandler().getPrefixes(), values);
		return this;
	}

	@Override
	public DescribeBuilder addWhereValueRow(Collection<?> values) {
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
	public DescribeBuilder clearWhereValues() {
		getWhereHandler().clearValues();
		return this;
	}
	
	@Override
	public DescribeBuilder addOptional(Triple t) {
		getWhereHandler().addOptional( new TriplePath( t ) );
		return this;
	}

	@Override
	public DescribeBuilder addOptional(TriplePath t) {
		getWhereHandler().addOptional( t );
		return this;
	}

	@Override
	public DescribeBuilder addOptional(FrontsTriple t) {
		getWhereHandler().addOptional( new TriplePath( t.asTriple() ) );
		return this;
	}

	@Override
	public DescribeBuilder addOptional(Object s, Object p, Object o) {
		getWhereHandler().addOptional( makeTriplePath( s, p, o ) );
		return this;
	}

	@Override
	public DescribeBuilder addOptional(AbstractQueryBuilder<?> t) {
		getWhereHandler().addOptional(t.getWhereHandler());
		return this;
	}

	@Override
	public DescribeBuilder addFilter(String expression) throws ParseException {
		getWhereHandler().addFilter(expression);
		return this;
	}

	@Override
	public DescribeBuilder addFilter(Expr expression) {
		getWhereHandler().addFilter(expression);
		return this;
	}

	@Override
	public DescribeBuilder addSubQuery(AbstractQueryBuilder<?> subQuery) {
		getWhereHandler().addSubQuery(subQuery);
		return this;
	}

	@Override
	public DescribeBuilder addUnion(AbstractQueryBuilder<?> union) {
		getWhereHandler().addUnion(union);
		return this;
	}

	@Override
	public DescribeBuilder addGraph(Object graph, AbstractQueryBuilder<?> subQuery) {
		getPrologHandler().addAll(subQuery.getPrologHandler());
		getWhereHandler().addGraph(makeNode(graph), subQuery.getWhereHandler());
		return this;
	}
	@Override
	public DescribeBuilder addGraph(Object graph, FrontsTriple triple) {
		getWhereHandler().addGraph(makeNode(graph), new TriplePath(triple.asTriple()));
		return this;
	}
	@Override
	public DescribeBuilder addGraph(Object graph, Object subject, Object predicate, Object object)
	{
		getWhereHandler().addGraph(makeNode(graph), makeTriplePath( subject, predicate, object ));
		return this;
	}
	@Override
	public DescribeBuilder addGraph(Object graph, Triple triple) {
		getWhereHandler().addGraph(makeNode(graph), new TriplePath(triple));
		return this;
	}
	@Override
	public DescribeBuilder addGraph(Object graph, TriplePath triplePath) {
		getWhereHandler().addGraph(makeNode(graph), triplePath );
		return this;
	}

	@Override
	public DescribeBuilder addBind(Expr expression, Object var) {
		getWhereHandler().addBind(expression, makeVar(var));
		return this;
	}

	@Override
	public DescribeBuilder addBind(String expression, Object var) throws ParseException {
		getWhereHandler().addBind(expression, makeVar(var));
		return this;
	}

	@Override
	public Node list(Object... objs) {
		return getWhereHandler().list(objs);
	}

	@Override
	public DescribeBuilder addMinus(AbstractQueryBuilder<?> t) {
		getWhereHandler().addMinus( t );
		return this;
	}

	@Override
	public DescribeBuilder fromNamed(String graphName) {
		getDatasetHandler().fromNamed(graphName);
		return this;
	}

	@Override
	public DescribeBuilder fromNamed(Collection<String> graphNames) {
		getDatasetHandler().fromNamed(graphNames);
		return this;
	}

	@Override
	public DescribeBuilder from(String graphName) {
		getDatasetHandler().from(graphName);
		return this;
	}

	@Override
	public DescribeBuilder from(Collection<String> graphName) {
		getDatasetHandler().from(graphName);
		return this;
	}

	@Override
	public DatasetHandler getDatasetHandler() {
		return handlerBlock.getDatasetHandler();
	}

	@Override
	public HandlerBlock getHandlerBlock() {
		return handlerBlock;
	}
	
	

}
