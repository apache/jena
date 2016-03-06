package org.apache.jena.arq.querybuilder.handlers;
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
import java.util.Map;
import java.util.Stack;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.clauses.ConstructClause;
import org.apache.jena.arq.querybuilder.clauses.DatasetClause;
import org.apache.jena.arq.querybuilder.clauses.SelectClause;
import org.apache.jena.arq.querybuilder.clauses.SolutionModifierClause;
import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public class HandlerBlock {
	private final AggregationHandler aggHandler;
	private final ConstructHandler constructHandler;
	private final DatasetHandler datasetHandler;
	private final PrologHandler prologHandler;
	private final SelectHandler selectHandler;
	private final SolutionModifierHandler modifierHandler;
	private final WhereHandler whereHandler;

	public HandlerBlock(Query query) {
		prologHandler = new PrologHandler(query);
		aggHandler = new AggregationHandler(query);
		whereHandler = new WhereHandler(query);
		datasetHandler = new DatasetHandler(query);
		modifierHandler = new SolutionModifierHandler(query);
		SelectHandler sTemp = null;
		ConstructHandler cTemp = null;
		if (query.isSelectType()) {
			sTemp = new SelectHandler(aggHandler);

		} else if (query.isAskType()) {
			// nochange
		} else if (query.isDescribeType()) {
			// no change
		} else if (query.isConstructType()) {
			cTemp = new ConstructHandler(query);
		}
		selectHandler = sTemp;
		constructHandler = cTemp;
	}

	public AggregationHandler getAggregationHandler() {
		return aggHandler;
	}

	public ConstructHandler getConstructHandler() {
		return constructHandler;
	}

	public DatasetHandler getDatasetHandler() {
		return datasetHandler;
	}

	public PrologHandler getPrologHandler() {
		return prologHandler;
	}

	public SelectHandler getSelectHandler() {
		return selectHandler;
	}

	public SolutionModifierHandler getModifierHandler() {
		return modifierHandler;
	}

	public WhereHandler getWhereHandler() {
		return whereHandler;
	}

	public void addAll(PrologHandler handler) {
		prologHandler.addAll(handler);
	}

	public void addAll(AggregationHandler handler) {
		aggHandler.addAll(handler);
	}

	public void addAll(ConstructHandler handler) {
		if (constructHandler != null) {
			constructHandler.addAll(handler);
		}
	}

	public void addAll(DatasetHandler handler) {
		datasetHandler.addAll(handler);
	}

	public void addAll(SolutionModifierHandler handler) {
		modifierHandler.addAll(handler);
	}

	public void addAll(SelectHandler handler) {
		if (selectHandler != null) {
			selectHandler.addAll(handler);
		}
	}

	public void addAll(WhereHandler handler) {
		whereHandler.addAll(handler);
	}
	
	public void addAll(HandlerBlock handler)
	{
		addAll(handler.aggHandler);
		if (handler.constructHandler != null)
		{
			addAll(handler.constructHandler);
		}
		if (handler.selectHandler != null)
		{
			addAll(handler.selectHandler);
		}
		addAll( handler.datasetHandler);
		addAll( handler.modifierHandler);
		addAll( handler.prologHandler);
		addAll( handler.whereHandler);
	}

	public void setVars(Map<Var, Node> values) {
		aggHandler.setVars(values);

		if (constructHandler != null) {
			constructHandler.setVars(values);
		}

		datasetHandler.setVars(values);

		prologHandler.setVars(values);

		if (selectHandler != null) {
			selectHandler.setVars(values);
		}

		modifierHandler.setVars(values);
		whereHandler.setVars(values);

	}
	
	public void build() {
		prologHandler.build();
		
		if (selectHandler != null) {
			selectHandler.build();
		}
		if (constructHandler != null) {
			constructHandler.build();
		}
		datasetHandler.build();
		modifierHandler.build();
		whereHandler.build();
		aggHandler.build();
	}
}
