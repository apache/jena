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
import org.apache.jena.arq.querybuilder.handlers.ConstructHandler;
import org.apache.jena.arq.querybuilder.handlers.DatasetHandler;
import org.apache.jena.arq.querybuilder.handlers.SolutionModifierHandler;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.FrontsTriple ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException ;

/**
 * Build an Construct query.
 * 
 */
public class ConstructBuilder extends AbstractQueryBuilder<ConstructBuilder>
		implements DatasetClause<ConstructBuilder>,
		WhereClause<ConstructBuilder>,
		SolutionModifierClause<ConstructBuilder>,
		ConstructClause<ConstructBuilder> {

	// the handlers used by this builder
	private final DatasetHandler datasetHandler;
	private final WhereHandler whereHandler;
	private final SolutionModifierHandler solutionModifier;
	private final ConstructHandler constructHandler;

	/**
	 * Constructor
	 */
	public ConstructBuilder() {
		super();
		query.setQueryConstructType();
		datasetHandler = new DatasetHandler(query);
		whereHandler = new WhereHandler(query);
		solutionModifier = new SolutionModifierHandler(query);
		constructHandler = new ConstructHandler(query);
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
	public ConstructHandler getConstructHandler() {
		return constructHandler;
	}

	@Override
	public SolutionModifierHandler getSolutionModifierHandler() {
		return solutionModifier;
	}

	@Override
	public ConstructBuilder clone() {
		ConstructBuilder qb = new ConstructBuilder();
		qb.prologHandler.addAll(prologHandler);
		qb.datasetHandler.addAll(datasetHandler);
		qb.whereHandler.addAll(whereHandler);
		qb.solutionModifier.addAll(solutionModifier);
		qb.constructHandler.addAll(constructHandler);
		return qb;
	}

	@Override
	public ConstructBuilder fromNamed(String graphName) {
		datasetHandler.fromNamed(graphName);
		return this;
	}

	@Override
	public ConstructBuilder fromNamed(Collection<String> graphNames) {
		datasetHandler.fromNamed(graphNames);
		return this;
	}

	@Override
	public ConstructBuilder from(String graphName) {
		datasetHandler.from(graphName);
		return this;
	}

	@Override
	public ConstructBuilder from(Collection<String> graphName) {
		datasetHandler.from(graphName);
		return this;
	}

	@Override
	public ConstructBuilder addOrderBy(String orderBy) {
		solutionModifier.addOrderBy(orderBy);
		return this;
	}

	@Override
	public ConstructBuilder addGroupBy(String groupBy) {
		solutionModifier.addGroupBy(groupBy);
		return this;
	}

	@Override
	public ConstructBuilder addHaving(String having) throws ParseException {
		solutionModifier.addHaving(having);
		return this;
	}

	@Override
	public ConstructBuilder setLimit(int limit) {
		solutionModifier.setLimit(limit);
		return this;
	}

	@Override
	public ConstructBuilder setOffset(int offset) {
		solutionModifier.setOffset(offset);
		return this;
	}

	@Override
	public ConstructBuilder addWhere(Triple t) {
		whereHandler.addWhere(t);
		return this;
	}

	@Override
	public ConstructBuilder addWhere(FrontsTriple t) {
		whereHandler.addWhere(t.asTriple());
		return this;
	}

	@Override
	public ConstructBuilder addWhere(Object s, Object p, Object o) {
		addWhere(new Triple(makeNode(s), makeNode(p), makeNode(o)));
		return this;
	}

	@Override
	public ConstructBuilder addOptional(Triple t) {
		whereHandler.addOptional(t);
		return this;
	}

	@Override
	public ConstructBuilder addOptional(FrontsTriple t) {
		whereHandler.addOptional(t.asTriple());
		return this;
	}

	@Override
	public ConstructBuilder addOptional(Object s, Object p, Object o) {
		addOptional(new Triple(makeNode(s), makeNode(p), makeNode(o)));
		return this;
	}

	@Override
	public ConstructBuilder addFilter(String s) throws ParseException {
		whereHandler.addFilter(s);
		return this;
	}

	@Override
	public ConstructBuilder addSubQuery(SelectBuilder subQuery) {
		prologHandler.addAll(subQuery.prologHandler);
		whereHandler.addSubQuery(subQuery);
		return this;
	}

	@Override
	public ConstructBuilder addUnion(SelectBuilder subQuery) {
		whereHandler.addUnion(subQuery);
		return this;
	}

	@Override
	public ConstructBuilder addGraph(Object graph, SelectBuilder subQuery) {
		prologHandler.addAll(subQuery.prologHandler);
		whereHandler.addGraph(makeNode(graph), subQuery.getWhereHandler());
		return this;
	}

	@Override
	public ConstructBuilder addBind(Expr expression, Object var) {
		whereHandler.addBind( expression, makeVar(var) );
		return this;
	}

	@Override
	public ConstructBuilder addBind(String expression, Object var) throws ParseException {
		whereHandler.addBind( expression, makeVar(var) );
		return this;
	}
	
	@Override
	public ConstructBuilder addConstruct(Triple t) {
		constructHandler.addConstruct(t);
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

}