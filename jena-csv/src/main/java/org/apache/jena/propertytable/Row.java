/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.propertytable;

import java.util.Collection;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * Each Row of the PropertyTable has an unique rowKey Node of the subject (or s for short).
 *
 */
public interface Row {

	/**
	 * @return the PropertyTable it belongs to
	 */
	PropertyTable getTable();
	
	/**
	 * Set the value of the Column in this Row
	 * @param column
	 * @param value
	 */
	void setValue(Column column, Node value);
	
	/**
	 * Get the value of the Column in this Row
	 * @param column
	 * @return value
	 */
	Node getValue(Column column);
	
	
	/**
	 * Get the value of the Column in this Row
	 * @param ColumnKey
	 * @return value
	 */
	Node getValue(Node ColumnKey);
	
	/**
	 * @return the rowKey Node of the subject
	 */
	Node getRowKey();
	
	/**
	 * @return the Triple Iterator over the values in this Row
	 */
	ExtendedIterator<Triple> getTripleIterator();
	
	/**
	 * @return all of the Columns of the PropertyTable
	 */
	Collection<Column> getColumns();

}
