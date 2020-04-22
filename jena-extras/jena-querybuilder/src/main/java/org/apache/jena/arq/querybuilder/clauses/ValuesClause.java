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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.Converters;
import org.apache.jena.arq.querybuilder.handlers.ValuesHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

/**
 * Interface that defines the ValueClause as per
 * https://www.w3.org/TR/sparql11-query/#rValuesClause
 * 
 * Conceptually this the values clause constructs a table comprising columns named
 * by variables and rows of data for those columns.  Most of the methods specified here 
 * add variable columns and/or add values for them.
 * 
 * The order that variables are added to the table is retained.
 * 
 * @param <T>
 *            The Builder type that the clause is part of.
 */
public interface ValuesClause<T extends AbstractQueryBuilder<T>> {
	
	/**
	 * Get the value handler for this clause. 
	 * @return The ValueHandler this clause is using.
	 */
	public ValuesHandler getValuesHandler();

	/**
	 * Add a variable or variable and values to the value statement.
	 * 
	 * The first var (or first item in a collection) is converted to a variable using 
	 * the makeVar strategy.  A variable may be added multiple times, doing so 
	 * will append values to the list of variable values.  The order in which variables 
	 * are added to the values table is preserved.
	 * 
	 * Adding a collection as the var will use the first object in the collection as
	 * the var and the remaining objects as values.
	 * 
	 * Values are created using makeNode() strategy except that null values are converted 
	 * to UNDEF.
	 * 
	 * @param var
	 *            The variable or collection to add.
	 * @return The builder for chaining.
	 * @see AbstractQueryBuilder#makeNode(Object)
	 * @see Converters#makeVar(Object)
	 */
	public T addValueVar(Object var);
	
	/**
	 * Add a variable and values to the value statement.
	 * 
	 * The var is converted to a variable using 
	 * the makeVar strategy.  A variable may be added multiple times, doing so 
	 * will append values to the list of variable values.  The order in which variables 
	 * are added to the values table is preserved.
	 * 
	 * Values are created using makeNode() strategy except that null values are converted 
	 * to UNDEF.
	 * 
	 * @param var
	 *            The variable to add.
	 * @param values The values for the variable          
	 * @return The builder for chaining.
	 * @see AbstractQueryBuilder#makeNode(Object)
	 * @see Converters#makeVar(Object)
	 */
	public T addValueVar(Object var, Object... values);
	
	
	/**
	 * Add a data table to the value statement.
	 * 
	 * Each key in the map is used converted into a variable using the makeVar strategy.
	 * The order in which variables 
	 * are added to the values table is preserved.
	 * 
	 * Each item in the value collection is converted into a node using makeNode() strategy except that null values are converted 
	 * to UNDEF.
	 * 
	 * If there are already values in the value statement the data table is adds as follows:
	 * <ul>
	 * <li>If the variable already exists in the table the map values are appended to the list of values</li>
	 * <li>If the variable does not exist in the table and there are other variables defined, an appropriate
	 * number of nulls is added to the front of the map values to create UNDEF entries for the existing rows</li>
	 * <li>If there are variables in the value statement that are not specified in the map additional UNDEF
	 * entries are appended to them to account for new rows that are added.</li>
	 * </ul>
	 * 
	 * @param dataTable
	 *            The data table to add.
	 * @return The builder for chaining.
	 * @see AbstractQueryBuilder#makeNode(Object)
	 * @see Converters#makeVar(Object)
	 */
	public <K extends Collection<?>> T addValueVars(Map<?,K> dataTable);
	
	
	/**
	 * Add objects as a row of values.  This method is different from the other methods in that
	 * the values are appended to each of the variables in the clause.  There must be 
	 * sufficient entries in the list to provide data for each variable in the table.
	 * Values objects are converted to nodes using the makeNode strategy.  Variables will always
	 * be in the order added to the values table.
	 * 
	 * @param values
	 *            the collection of values to add.
	 * @return The builder for chaining.
 	 * @see AbstractQueryBuilder#makeNode(Object)
	 */
	public T addValueRow(Object... values);

	/**
	 * Add a collection of objects as row of values.  This method is different from the other methods in that
	 * the values are appended to each of the variables in the clause.  There must be 
	 * sufficient entries in the list to provide data for each variable in the table.
	 * Values objects are converted to nodes using the makeNode strategy.  Variables will always
	 * be in the order added to the values table.
	 *  
	 * @param values
	 *            the collection of values to add.
	 * @return The builder for chaining.
 	 * @see AbstractQueryBuilder#makeNode(Object)
	 */
	public T addValueRow(Collection<?> values);
	
	/**
	 * Reset the values table to the initial undefined state.  Used primarily to reset the builder values table
	 * to a known state. 
	 * 
	 * @return The builder for chaining.
	 */
	public T clearValues();
	
	/**
	 * Get an unmodifiable list of vars in the order that they appear in the values table.
	 * @return an unmodifiable list of vars.
	 */
	public List<Var> getValuesVars();
	
	/**
	 * Get an unmodifiable map of vars and their values.
	 * 
	 * Null values are considered as UNDEF values.
	 * 
	 * @return an unmodifiable map of vars and their values.
	 */
	public Map<Var,List<Node>> getValuesMap();
}
