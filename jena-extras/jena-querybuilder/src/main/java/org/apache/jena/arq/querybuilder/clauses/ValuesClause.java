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

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.handlers.ValuesHandler;

/**
 * Interface that defines the ValueClause as per
 * https://www.w3.org/TR/sparql11-query/#rValuesClause
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
	 * Add a variable to the value statement.
	 * 
	 * A variable may only be added once. Attempting to add the same variable
	 * multiple times will be silently ignored.
	 * 
	 * @param var
	 *            The variable to add.
	 * @return The builder for chaining.
	 */
	public T addValueVar(Object var);

	/**
	 * Add the values for the variables.  There must be one value for each value var.
	 * 
	 * @param values
	 *            the collection of values to add.
	 * @return The builder for chaining.
	 */
	public T addDataBlock(Collection<Object> values);


}
