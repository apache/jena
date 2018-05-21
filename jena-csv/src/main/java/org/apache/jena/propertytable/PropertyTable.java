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
import java.util.List;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * PropertyTable is designed to be a table of RDF terms, or Nodes in Jena. 
 * Each Column of the PropertyTable has an unique columnKey Node of the predicate (or p for short).
 * Each Row of the PropertyTable has an unique rowKey Node of the subject (or s for short).
 * You can use getColumn() to get the Column by its columnKey Node of the predicate, while getRow() for Row.
 * 
 */
@Deprecated
public interface PropertyTable {
	
	/**
	 * Query for ?s <p> <o>
	 * @param column the Column with the columnKey Node of the predicate
	 * @param value the object (or value) Node
	 * @return the ExtendedIterator of the matching Triples
	 */
	ExtendedIterator<Triple> getTripleIterator(Column column, Node value);

	/**
	 * Query for ?s <p> ?o
	 * @param column the Column with the columnKey Node of the predicate
	 * @return the ExtendedIterator of the matching Triples
	 */
	ExtendedIterator<Triple> getTripleIterator(Column column);
	
	/**
	 * Query for ?s ?p <o>
	 * @param value the object (or value) Node
	 * @return the ExtendedIterator of the matching Triples
	 */
	ExtendedIterator<Triple> getTripleIterator(Node value);
	
	/**
	 * Query for <s> ?p ?o
	 * @param row the Row with the rowKey Node of the subject
	 * @return the ExtendedIterator of the matching Triples
	 */
	ExtendedIterator<Triple> getTripleIterator(Row row);
	
	/**
	 * Query for ?s ?p ?o
	 * @return all of the Triples of the PropertyTable
	 */
	ExtendedIterator<Triple> getTripleIterator();

	/**
	 * @return all of the Columns of the PropertyTable
	 */
	Collection<Column> getColumns();

	/**
	 * Get Column by its columnKey Node of the predicate
	 * @param p columnKey Node of the predicate
	 * @return the Column
	 */
	Column getColumn(Node p);

	/**
	 * Create a Column by its columnKey Node of the predicate
	 * @param p
	 */
	Column createColumn(Node p);

	/**
	 * Get Row by its rowKey Node of the subject
	 * @param s rowKey Node of the subject
	 * @return the Row
	 */
	Row getRow(Node s);
	
	
	/**
	 * Create Row by its rowKey Node of the subject
	 * @param s rowKey Node of the subject
	 * @return the Row created
	 */
	Row createRow(Node s);
	
	
	/**
	 * Get all of the rows
	 */
	List<Row> getAllRows() ;
	
	
	/**
	 * Get all the values within a Column
	 * @param column
	 * @return the values
	 */
	List<Node> getColumnValues(Column column);
	
	/**
	 * Get the Rows matching the value of a Column
	 * @param column the Column with the columnKey Node of the predicate
	 * @param value the object (or value) Node
	 * @return the matching Rows
	 */
	Collection<Row> getMatchingRows(Column column, Node value);
	
}
