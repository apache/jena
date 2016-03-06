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

import org.apache.jena.arq.querybuilder.clauses.DatasetClause;
import org.apache.jena.arq.querybuilder.clauses.SolutionModifierClause;
import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.arq.querybuilder.handlers.DatasetHandler;
import org.apache.jena.arq.querybuilder.handlers.HandlerBlock;
import org.apache.jena.arq.querybuilder.handlers.SolutionModifierHandler;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.FrontsTriple ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException ;

/**
 * Build an ASK query.
 * 
 */
public class AskBuilder extends AbstractQueryBuilder<AskBuilder> implements
		DatasetClause<AskBuilder>, WhereClause<AskBuilder>,
		SolutionModifierClause<AskBuilder> {
	
	private final HandlerBlock handlerBlock;

	/**
	 * The constructor
	 */
	public AskBuilder() {
		super();
		query.setQueryAskType();
		handlerBlock = new HandlerBlock( query );
	}
	
	@Override
	public HandlerBlock getHandlerBlock()
	{
		return handlerBlock;
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
	public AskBuilder clone() {
		AskBuilder qb = new AskBuilder();
		qb.handlerBlock.addAll( handlerBlock );
		return qb;
	}

	@Override
	public AskBuilder fromNamed(String graphName) {
		getDatasetHandler().fromNamed(graphName);
		return this;
	}

	@Override
	public AskBuilder fromNamed(Collection<String> graphNames) {
		getDatasetHandler().fromNamed(graphNames);
		return this;
	}

	@Override
	public AskBuilder from(String graphName) {
		getDatasetHandler().from(graphName);
		return this;
	}

	@Override
	public AskBuilder from(Collection<String> graphName) {
		getDatasetHandler().from(graphName);
		return this;
	}

	@Override
	public AskBuilder addWhere(Triple t) {
		getWhereHandler().addWhere(t);
		return this;
	}

	@Override
	public AskBuilder addWhere(FrontsTriple t) {
		getWhereHandler().addWhere(t.asTriple());
		return this;
	}

	@Override
	public AskBuilder addWhere(Object s, Object p, Object o) {
		addWhere(new Triple(makeNode(s), makeNode(p), makeNode(o)));
		return this;
	}

	@Override
	public AskBuilder addOptional(Triple t) {
		getWhereHandler().addOptional(t);
		return this;
	}
	
	@Override
	public AskBuilder addOptional(SelectBuilder t)
	{
		getWhereHandler().addOptional(t.getWhereHandler());
		return this;
	}

	@Override
	public AskBuilder addOptional(FrontsTriple t) {
		getWhereHandler().addOptional(t.asTriple());
		return this;
	}

	@Override
	public AskBuilder addOptional(Object s, Object p, Object o) {
		addOptional(new Triple(makeNode(s), makeNode(p), makeNode(o)));
		return this;
	}

	@Override
	public AskBuilder addFilter(String s) throws ParseException {
		getWhereHandler().addFilter(s);
		return this;
	}

	@Override
	public AskBuilder addSubQuery(SelectBuilder subQuery) {
		getWhereHandler().addSubQuery(subQuery);
		return this;
	}

	@Override
	public AskBuilder addUnion(SelectBuilder subQuery) {
		getWhereHandler().addUnion(subQuery);
		return this;
	}

	@Override
	public AskBuilder addGraph(Object graph, SelectBuilder subQuery) {
		getPrologHandler().addAll(subQuery.getPrologHandler());
		getWhereHandler().addGraph(makeNode(graph), subQuery.getWhereHandler());
		return this;
	}
	
	@Override
	public AskBuilder addBind(Expr expression, Object var) {
		getWhereHandler().addBind( expression, makeVar(var) );
		return this;
	}

	@Override
	public AskBuilder addBind(String expression, Object var) throws ParseException {
		getWhereHandler().addBind( expression, makeVar(var) );
		return this;
	}
	@Override
	public AskBuilder addOrderBy(String orderBy) {
		getSolutionModifierHandler().addOrderBy(orderBy);
		return this;
	}

	@Override
	public AskBuilder addGroupBy(String groupBy) {
		getSolutionModifierHandler().addGroupBy(groupBy);
		return this;
	}

	@Override
	public AskBuilder addHaving(String having) throws ParseException {
		getSolutionModifierHandler().addHaving(having);
		return this;
	}

	@Override
	public AskBuilder setLimit(int limit) {
		getSolutionModifierHandler().setLimit(limit);
		return this;
	}

	@Override
	public AskBuilder setOffset(int offset) {
		getSolutionModifierHandler().setOffset(offset);
		return this;
	}

	@Override
	public SolutionModifierHandler getSolutionModifierHandler() {
		return handlerBlock.getModifierHandler();
	}

	@Override
	public Node list(Object... objs) {
		return getWhereHandler().list(objs);
	}
}