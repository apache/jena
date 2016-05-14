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

package org.apache.jena.propertytable.impl;

import java.util.* ;
import java.util.Map.Entry;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorConcat;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.SetMultimap ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.propertytable.Column;
import org.apache.jena.propertytable.PropertyTable;
import org.apache.jena.propertytable.Row;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/**
 * A PropertyTable Implementation using HashMap.
 * It contains PSO and POS indexes.
 * 
 */
public class PropertyTableHashMapImpl implements PropertyTable {

	private Map<Node, Column> columnIndex; // Maps property Node key to Column
	private List<Column> columnList; // Stores the list of columns in the table
	private Map<Node, Row> rowIndex; // Maps the subject Node key to Row.
	private List<Row> rowList; // Stores the list of rows in the table

	// PSO index
	// Maps column Node to (subject Node, value) pairs
	private Map<Node, Map<Node, Node>> valueIndex; 
	// POS index
	// Maps column Node to (value, subject Node) pairs
	private Map<Node, SetMultimap<Node, Node>> valueReverseIndex; 

	PropertyTableHashMapImpl() {
		columnIndex = new HashMap<Node, Column>();
		columnList = new ArrayList<Column>();
		rowIndex = new HashMap<Node, Row>();
		rowList = new ArrayList<Row>();
		valueIndex = new HashMap<Node, Map<Node, Node>>();
		valueReverseIndex = new HashMap<Node, SetMultimap<Node, Node>>();
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator() {
		
		// use PSO index to scan all the table (slow)
		IteratorConcat<Triple> iter = new IteratorConcat<Triple>();
		for (Column column : getColumns()) {
			iter.add(getTripleIterator(column));
		}
		return WrappedIterator.create(Iter.distinct(iter));
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator(Column column) {
		
		// use PSO index directly (fast)
		
		if (column == null || column.getColumnKey() == null)
			throw new NullPointerException("column is null");
		
		ArrayList<Triple> triples = new ArrayList<Triple>();
		Map<Node, Node> values = valueIndex.get(column.getColumnKey());

		for (Entry<Node, Node> entry : values.entrySet()) {
			Node subject = entry.getKey();
			Node value = entry.getValue();
			triples.add(Triple.create(subject, column.getColumnKey(), value));
		}
		return WrappedIterator.create(triples.iterator());
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator(Node value) {
		
		// use POS index ( O(n), n= column count )
		
		if (value == null)
			throw new NullPointerException("value is null");
		
		IteratorConcat<Triple> iter = new IteratorConcat<Triple>();
		for (Column column : this.getColumns()) {
			ExtendedIterator<Triple> eIter = getTripleIterator(column,value);
			iter.add(eIter);
		}
		return WrappedIterator.create(Iter.distinct(iter));
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator(Column column, Node value) {
		
		// use POS index directly (fast)
		
		if (column == null || column.getColumnKey() == null)
			throw new NullPointerException("column is null");
		
		if (value == null)
			throw new NullPointerException("value is null");
		
		
		Node p = column.getColumnKey();
		final SetMultimap<Node, Node> valueToSubjectMap = valueReverseIndex.get(p);
		if ( valueToSubjectMap == null ) 
		    return NullIterator.instance() ;
		final Set<Node> subjects = valueToSubjectMap.get(value);
		ArrayList<Triple> triples = new ArrayList<Triple>();
		for (Node subject : subjects) {
		    triples.add(Triple.create(subject, p, value));
		}
		return WrappedIterator.create(triples.iterator());
	}


	@Override
	public ExtendedIterator<Triple> getTripleIterator(Row row) {
		// use PSO index ( O(n), n= column count )
		
		if (row == null || row.getRowKey() == null)
			throw new NullPointerException("row is null");
		
		ArrayList<Triple> triples = new ArrayList<Triple>();
		for (Column column : getColumns()) {
			Node value = row.getValue(column);
			triples.add(Triple.create(row.getRowKey(), column.getColumnKey(), value));
		}
		return WrappedIterator.create(triples.iterator());
	}

	@Override
	public Collection<Column> getColumns() {
		return columnList;
	}

	@Override
	public Column getColumn(Node p) {
		if (p == null)
			throw new NullPointerException("column node is null");
		return columnIndex.get(p);
	}

	@Override
	public Column createColumn(Node p) {
		if (p == null)
			throw new NullPointerException("column node is null");

		if (columnIndex.containsKey(p))
			throw new IllegalArgumentException("column already exists: '"
					+ p.toString());

		columnIndex.put(p, new ColumnImpl(this, p));
		columnList.add(columnIndex.get(p));
		valueIndex.put(p, new HashMap<Node, Node>());
		valueReverseIndex.put(p, HashMultimap.create());
		return getColumn(p);
	}

	@Override
	public Row getRow(final Node s) {
		if (s == null)
			throw new NullPointerException("subject node is null");
		Row row = rowIndex.get(s);
		return row;

	}
	
	@Override
	public Row createRow(final Node s){
		Row row = this.getRow(s);
		if (row != null)
			return row;

		row = new InternalRow(s);
		rowIndex.put(s, row);
		rowList.add(row);

		return row;
	}
	
	@Override
	public List<Row> getAllRows() {
		return rowList;
	}

	
	
	@Override
	public List<Node> getColumnValues(Column column) {
		if (column == null || column.getColumnKey() == null)
			throw new NullPointerException("column is null");
		
		Map<Node, Node> values = valueIndex.get(column.getColumnKey());

		List<Node> list = new ArrayList<Node>(values.size());
		list.addAll(values.values());
		return list;
	}
	
	@Override
	public Collection<Row> getMatchingRows(Column column, Node value) {
		if (column == null || column.getColumnKey() == null)
			throw new NullPointerException("column is null");
		
		if (value == null)
			throw new NullPointerException("value is null");
		
		
		Node p = column.getColumnKey();
		final SetMultimap<Node, Node> valueToSubjectMap = valueReverseIndex.get(p);
		if ( valueToSubjectMap == null )
		    return Collections.emptyList() ;
		final Set<Node> subjects = valueToSubjectMap.get(value);
		if ( subjects == null )
		    return Collections.emptyList() ;
		final ArrayList<Row> matchingRows = new ArrayList<Row>();
		for (Node subject : subjects) {
		    matchingRows.add(this.getRow(subject));
		}
		return matchingRows;
	}

	private final void setX(final Node s, final Node p, final Node value) {
		if (p == null)
			throw new NullPointerException("column Node must not be null.");
		if (value == null)
			throw new NullPointerException("value must not be null.");

		Map<Node, Node> subjectToValueMap = valueIndex.get(p);
		if (!columnIndex.containsKey(p) || subjectToValueMap == null)
			throw new IllegalArgumentException("column: '" + p
					+ "' does not yet exist.");

		Node oldValue = subjectToValueMap.get(s);
		subjectToValueMap.put(s, value);
		addToReverseMap(p, s, oldValue, value);
	}

	private void addToReverseMap(final Node p, final Node s, final Node oldValue, final Node value) {

		final SetMultimap<Node, Node> valueToSubjectMap = valueReverseIndex.get(p);
		if ( valueToSubjectMap == null )
            return ; 
		valueToSubjectMap.remove(oldValue, s);
		valueToSubjectMap.put(value, s);
	}

	private void unSetX(final Node s, final Node p) {

		final Map<Node, Node> subjectToValueMap = valueIndex.get(p);
		if (!columnIndex.containsKey(p) || subjectToValueMap == null)
			throw new IllegalArgumentException("column: '" + p
					+ "' does not yet exist.");

		final Node value = subjectToValueMap.get(s);
		if (value == null)
			return;

		subjectToValueMap.remove(s);
		removeFromReverseMap(p, s, value);
	}

	private void removeFromReverseMap(final Node p, final Node s,
			final Node value) {
		final SetMultimap<Node, Node> valueTokeysMap = valueReverseIndex.get(p);
		if ( valueTokeysMap == null )
		    return ;
		valueTokeysMap.remove(s, value);
	}

	private Node getX(final Node s, final Node p) {
		final Map<Node, Node> subjectToValueMap = valueIndex.get(p);
		if (!columnIndex.containsKey(p) || subjectToValueMap == null)
			throw new IllegalArgumentException("column: '" + p
					+ "' does not yet exist.");
		return subjectToValueMap.get(s);
	}

	private final class InternalRow implements Row {
		private final Node key;

		InternalRow(final Node key) {
			this.key = key;
		}

		@Override
		public void setValue(Column column, Node value) {
			if (value == null)
				unSetX(key, column.getColumnKey());
			else
				setX(key, column.getColumnKey(), value);
		}

		@Override
		public Node getValue(Column column) {
			return getX(key, column.getColumnKey());
		}
		
		@Override
		public Node getValue(Node columnKey) {
			return getX(key, columnKey);
		}

		@Override
		public PropertyTable getTable() {
			return PropertyTableHashMapImpl.this;
		}

		@Override
		public Node getRowKey() {
			return key;
		}

		@Override
		public Collection<Column> getColumns() {
			// TODO Auto-generated method stub
			return PropertyTableHashMapImpl.this.getColumns();
		}

		@Override
		public ExtendedIterator<Triple> getTripleIterator() {
			ArrayList<Triple> triples = new ArrayList<Triple>();
			for (Column column : getColumns()) {
				Node value = this.getValue(column);
				triples.add(Triple.create(key, column.getColumnKey(), value));
			}
			return WrappedIterator.create(triples.iterator());
		}

	}

}
