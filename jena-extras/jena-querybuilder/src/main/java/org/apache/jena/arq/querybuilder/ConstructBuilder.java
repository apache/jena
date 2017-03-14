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

import org.apache.jena.arq.querybuilder.clauses.ConstructClause;
import org.apache.jena.arq.querybuilder.clauses.DatasetClause;
import org.apache.jena.arq.querybuilder.clauses.SolutionModifierClause;
import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.arq.querybuilder.handlers.* ;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

/**
 * Build an Construct query.
 * 
 */
public class ConstructBuilder extends AbstractQueryBuilder<ConstructBuilder> implements DatasetClause<ConstructBuilder>,
		WhereClause<ConstructBuilder>, SolutionModifierClause<ConstructBuilder>, ConstructClause<ConstructBuilder> {

	private final HandlerBlock handlerBlock;

	/**
	 * Constructor
	 */
	public ConstructBuilder() {
		super();
		query.setQueryConstructType();
		handlerBlock = new HandlerBlock(query);
	}

	@Override
	public DatasetHandler getDatasetHandler() {
		return handlerBlock.getDatasetHandler();
	}

	@Override
	public WhereHandler getWhereHandler() {
		return handlerBlock.getWhereHandler();
	}

	@Override
	public ConstructHandler getConstructHandler() {
		return handlerBlock.getConstructHandler();
	}

	@Override
	public SolutionModifierHandler getSolutionModifierHandler() {
		return handlerBlock.getModifierHandler();
	}

	@Override
	public HandlerBlock getHandlerBlock() {
		return handlerBlock;
	}

	@Override
	public ConstructBuilder clone() {
		ConstructBuilder qb = new ConstructBuilder();
		qb.handlerBlock.addAll(handlerBlock);
		return qb;
	}

	@Override
	public ConstructBuilder fromNamed(String graphName) {
		getDatasetHandler().fromNamed(graphName);
		return this;
	}

	@Override
	public ConstructBuilder fromNamed(Collection<String> graphNames) {
		getDatasetHandler().fromNamed(graphNames);
		return this;
	}

	@Override
	public ConstructBuilder from(String graphName) {
		getDatasetHandler().from(graphName);
		return this;
	}

	@Override
	public ConstructBuilder from(Collection<String> graphName) {
		getDatasetHandler().from(graphName);
		return this;
	}

	@Override
	public ConstructBuilder addOrderBy(Expr orderBy) {
		getSolutionModifierHandler().addOrderBy(orderBy);
		return this;
	}

	@Override
	public ConstructBuilder addOrderBy(Object orderBy) {
		getSolutionModifierHandler().addOrderBy(makeVar(orderBy));
		return this;
	}

	@Override
	public ConstructBuilder addOrderBy(SortCondition orderBy) {
		getSolutionModifierHandler().addOrderBy(orderBy);
		return this;
	}

	@Override
	public ConstructBuilder addOrderBy(Expr orderBy, Order order) {
		getSolutionModifierHandler().addOrderBy(orderBy, order);
		return this;
	}

	@Override
	public ConstructBuilder addOrderBy(Object orderBy, Order order) {
		getSolutionModifierHandler().addOrderBy(makeVar(orderBy), order);
		return this;
	}

	@Override
	public ConstructBuilder addGroupBy(Object groupBy) {
		getSolutionModifierHandler().addGroupBy(makeVar(groupBy));
		return this;
	}

	@Override
	public ConstructBuilder addGroupBy(Expr groupBy) {
		getSolutionModifierHandler().addGroupBy(groupBy);
		return this;
	}

	@Override
	public ConstructBuilder addGroupBy(Object var, Expr expr) {
		getSolutionModifierHandler().addGroupBy(makeVar(var), expr);
		return this;
	}

	@Override
	public ConstructBuilder addGroupBy(Object var, String expr) {
		getSolutionModifierHandler().addGroupBy(makeVar(var), makeExpr(expr));
		return this;
	}

	@Override
	public ConstructBuilder addHaving(String having) throws ParseException {
		getSolutionModifierHandler().addHaving(having);
		return this;
	}

	@Override
	public ConstructBuilder addHaving(Expr expression) throws ParseException {
		getSolutionModifierHandler().addHaving(expression);
		return this;
	}

	@Override
	public ConstructBuilder addHaving(Object var) throws ParseException {
		getSolutionModifierHandler().addHaving(makeVar(var));
		return this;
	}

	@Override
	public ConstructBuilder setLimit(int limit) {
		getSolutionModifierHandler().setLimit(limit);
		return this;
	}

	@Override
	public ConstructBuilder setOffset(int offset) {
		getSolutionModifierHandler().setOffset(offset);
		return this;
	}

	@Override
	public ConstructBuilder addWhere(TriplePath t) {
		getWhereHandler().addWhere(t);
		return this;
	}
	
	@Override
	public ConstructBuilder addWhere(Triple t) {
		getWhereHandler().addWhere(new TriplePath(t));
		return this;
	}

	@Override
	public ConstructBuilder addWhere(FrontsTriple t) {
		getWhereHandler().addWhere(new TriplePath(t.asTriple()));
		return this;
	}

	@Override
	public ConstructBuilder addWhere(Object s, Object p, Object o) {
		getWhereHandler().addWhere( makeTriplePath( s, p, o ));
		return this;
	}

	@Override
	public ConstructBuilder addOptional(TriplePath t) {
		getWhereHandler().addOptional(t);
		return this;
	}

	@Override
	public ConstructBuilder addOptional(Triple t) {
		getWhereHandler().addOptional(new TriplePath(t));
		return this;
	}

	@Override
	public ConstructBuilder addOptional(SelectBuilder t) {
		getWhereHandler().addOptional(t.getWhereHandler());
		return this;
	}

	@Override
	public ConstructBuilder addOptional(FrontsTriple t) {
		getWhereHandler().addOptional(new TriplePath(t.asTriple()));
		return this;
	}

	@Override
	public ConstructBuilder addOptional(Object s, Object p, Object o) {
		getWhereHandler().addOptional( makeTriplePath( s, p, o ));
		return this;
	}

	@Override
	public ConstructBuilder addFilter(String s) throws ParseException {
		getWhereHandler().addFilter(s);
		return this;
	}

	@Override
	public ConstructBuilder addSubQuery(SelectBuilder subQuery) {
		getWhereHandler().addSubQuery(subQuery);
		return this;
	}

	@Override
	public ConstructBuilder addUnion(SelectBuilder subQuery) {
		getWhereHandler().addUnion(subQuery);
		return this;
	}

	@Override
	public ConstructBuilder addGraph(Object graph, SelectBuilder subQuery) {
		getPrologHandler().addAll(subQuery.getPrologHandler());
		getWhereHandler().addGraph(makeNode(graph), subQuery.getWhereHandler());
		return this;
	}

	@Override
	public ConstructBuilder addBind(Expr expression, Object var) {
		getWhereHandler().addBind(expression, makeVar(var));
		return this;
	}

	@Override
	public ConstructBuilder addBind(String expression, Object var) throws ParseException {
		getWhereHandler().addBind(expression, makeVar(var));
		return this;
	}

	@Override
	public ConstructBuilder addConstruct(Triple t) {
		getConstructHandler().addConstruct(t);
		return this;
	}

	@Override
	public ConstructBuilder addConstruct(FrontsTriple t) {
		return addConstruct(t.asTriple());
	}

	@Override
	public ConstructBuilder addConstruct(Object s, Object p, Object o) {
		return addConstruct(new Triple(makeNode(s), makeNode(p), makeNode(o)));
	}

	@Override
	public Node list(Object... objs) {
		return getWhereHandler().list(objs);
	}
}