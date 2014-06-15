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

package com.hp.hpl.jena.propertytable;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
	 * @return the rowKey Node of the subject
	 */
	Node getRowKey();
	
	/**
	 * @return the Triple Iterator over the values in this Row
	 */
	ExtendedIterator<Triple> getTripleIterator();

}
