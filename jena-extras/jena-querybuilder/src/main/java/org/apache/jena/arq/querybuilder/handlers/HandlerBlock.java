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

import java.util.Map;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

/**
 * A class to handle all the handlers of a query builder and keep them in sync
 * as needed.
 *
 */
public class HandlerBlock {
	private final AggregationHandler aggHandler;
	private final ConstructHandler constructHandler;
	private final DatasetHandler datasetHandler;
	private final PrologHandler prologHandler;
	private final SelectHandler selectHandler;
	private final SolutionModifierHandler modifierHandler;
	private final WhereHandler whereHandler;
	private final ValuesHandler valueHandler;

	/**
	 * Constructor.
	 * 
	 * @param query
	 *            The query we are working with.
	 */
	public HandlerBlock(Query query) {
		prologHandler = new PrologHandler(query);
		aggHandler = new AggregationHandler(query);
		whereHandler = new WhereHandler(query);
		datasetHandler = new DatasetHandler(query);
		modifierHandler = new SolutionModifierHandler(query);
		valueHandler = new ValuesHandler(query);
		/*
		 * selecthandler and constructhandler may be null so processthem
		 * accordingly
		 */
		SelectHandler sTemp = null;
		ConstructHandler cTemp = null;
		if (query.isSelectType()) {
			sTemp = new SelectHandler(aggHandler);
		} else if (query.isAskType()) {
			// nochange
		} else if (query.isDescribeType()) {
			sTemp = new SelectHandler(aggHandler);
		} else if (query.isConstructType()) {
			cTemp = new ConstructHandler(query);
		}
		selectHandler = sTemp;
		constructHandler = cTemp;
	}

	/**
	 * Get the aggregation handler.
	 * 
	 * @return the aggregation handler.
	 */
	public AggregationHandler getAggregationHandler() {
		return aggHandler;
	}

	/**
	 * Get the construct handler.
	 * 
	 * @return the construct handler or null.
	 */
	public ConstructHandler getConstructHandler() {
		return constructHandler;
	}

	/**
	 * Get the dataset handler.
	 * 
	 * @return the dataset handler.
	 */
	public DatasetHandler getDatasetHandler() {
		return datasetHandler;
	}

	/**
	 * Get the prolog handler.
	 * 
	 * @return the prolog handler.
	 */
	public PrologHandler getPrologHandler() {
		return prologHandler;
	}

	/**
	 * Get the select handler.
	 * 
	 * @return the select handler or null.
	 */
	public SelectHandler getSelectHandler() {
		return selectHandler;
	}

	/**
	 * Get the solution modifier handler.
	 * 
	 * @return the solution modifier handler.
	 */
	public SolutionModifierHandler getModifierHandler() {
		return modifierHandler;
	}

	/**
	 * Get the where handler.
	 * 
	 * @return the where handler.
	 */
	public WhereHandler getWhereHandler() {
		return whereHandler;
	}

	/**
	 * Get the value handler.
	 * 
	 * @return the value handler.
	 */
	public ValuesHandler getValueHandler() {
		return valueHandler;
	}
	
	/**
	 * Add the prolog handler contents to this prolog handler.
	 * 
	 * @param handler
	 *            The prolog handler to add to this one.
	 */
	public void addAll(PrologHandler handler) {
		prologHandler.addAll(handler);
	}

	/**
	 * Add the aggregation handler contents to this prolog handler.
	 * 
	 * @param handler
	 *            The aggregation handler to add to this one.
	 */
	public void addAll(AggregationHandler handler) {
		aggHandler.addAll(handler);
	}

	/**
	 * Add the construct handler contents to this prolog handler. If this
	 * construct handler is null or the handler argument is null this method
	 * does nothing.
	 * 
	 * @param handler
	 *            The construct handler to add to this one.
	 */
	public void addAll(ConstructHandler handler) {
		if (constructHandler != null && handler != null) {
			constructHandler.addAll(handler);
		}
	}

	/**
	 * Add the dataset handler contents to this prolog handler.
	 * 
	 * @param handler
	 *            The dataset handler to add to this one.
	 */
	public void addAll(DatasetHandler handler) {
		datasetHandler.addAll(handler);
	}

	/**
	 * Add the solution modifier handler contents to this prolog handler.
	 * 
	 * @param handler
	 *            The solution modifier handler to add to this one.
	 */
	public void addAll(SolutionModifierHandler handler) {
		modifierHandler.addAll(handler);
	}

	/**
	 * Add the select handler contents to this prolog handler. If this select
	 * handler is null or the handler argument is null this method does nothing.
	 * 
	 * @param handler
	 *            The construct handler to add to this one.
	 */
	public void addAll(SelectHandler handler) {
		if (selectHandler != null && handler != null) {
			selectHandler.addAll(handler);
		}
	}

	/**
	 * Add the where handler contents to this prolog handler.
	 * 
	 * @param handler
	 *            The where handler to add to this one.
	 */
	public void addAll(WhereHandler handler) {
		whereHandler.addAll(handler);
	}

	/**
	 * Add the values handler contents to this prolog handler.
	 * 
	 * @param handler
	 *            The values handler to add to this one.
	 */
	public void addAll(ValuesHandler handler) {
		valueHandler.addAll(handler);
	}
	
	/**
	 * Add all of the handlers in the handler block to this one. Any handler
	 * that is null or is null in the handler argument are properly skipped.
	 * 
	 * @param handler
	 *            The handler block to add to this one.
	 */
	public void addAll(HandlerBlock handler) {
		addAll(handler.aggHandler);
		addAll(handler.constructHandler);
		addAll(handler.selectHandler);
		addAll(handler.datasetHandler);
		addAll(handler.modifierHandler);
		addAll(handler.prologHandler);
		addAll(handler.whereHandler);
		addAll(handler.valueHandler);
	}

	/**
	 * Set the variables in all the enclosed handlers in the proper order.
	 * 
	 * @param values
	 *            The map of values to set.
	 */
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

	/**
	 * Build all the enclosed handlers in the proper order.
	 */
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
		valueHandler.build();
	}
}
