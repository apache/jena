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
import org.apache.jena.arq.querybuilder.handlers.DatasetHandler;

/**
 * Interface that defines the DatasetClause as per
 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rDatasetClause
 * 
 * @param <T>
 *            The Builder type that the clause is part of.
 */
public interface DatasetClause<T extends AbstractQueryBuilder<T>> {
	/**
	 * Add the "FROM NAMED" graph name.
	 * 
	 * @param graphName
	 *            the graph name to add.
	 * @return This builder for chaining.
	 */
	public T fromNamed(String graphName);

	/**
	 * Add several "FROM NAMED" graph names.
	 * 
	 * @param graphNames
	 *            the collection graph names to add.
	 * @return This builder for chaining.
	 */
	public T fromNamed(Collection<String> graphNames);

	/**
	 * Add the "FROM" graph name.
	 * 
	 * @param graphName
	 *            the graph name to add.
	 * @return This builder for chaining.
	 */
	public T from(String graphName);

	/**
	 * Add several "FROM" graph names.
	 * 
	 * @param graphName
	 *            the collection graph names to add.
	 * @return This builder for chaining.
	 */
	public T from(Collection<String> graphName);

	/**
	 * Get the Dataset handler for this clause.
	 * @return The DatasetHandler this clause is using.
	 */
	public DatasetHandler getDatasetHandler();

}
