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

package com.hp.hpl.jena.propertytable.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorConcat;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.propertytable.Column;
import com.hp.hpl.jena.propertytable.PropertyTable;
import com.hp.hpl.jena.propertytable.Row;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class PropertyTableImpl implements PropertyTable {

	private Map<Node, Column> columnIndex; // Maps property Node to Column
	private List<Column> columnList; // Stores the list of columns in the table
	private Map<Object, Row> rowIndex; // Maps the row number to Row.
	private List<Row> rowList; // Stores the list of rows in the table
	private Map<Node, Map<Object, Node>> valueIndex; // Maps column Node to
														// (rowNum,value) pairs

	PropertyTableImpl() {
		columnIndex = new HashMap<Node, Column>();
		columnList = new ArrayList<Column>();
		rowIndex = new HashMap<Object, Row>();
		rowList = new ArrayList<Row>();
		valueIndex = new HashMap<Node, Map<Object, Node>>();
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator() {
		IteratorConcat<Triple> iter = new IteratorConcat<Triple>();
		for (Column column : getColumns()) {
			iter.add(getTripleIterator(column));
		}
		return WrappedIterator.create(Iter.distinct(iter));
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator(Column column) {
		ArrayList<Triple> triples = new ArrayList<Triple>();
		Map<Object, Node> values = valueIndex.get(column.getNode());
		
		for(Entry<Object,Node> entry :values.entrySet()){
			
			Object rowNum = entry.getKey();
			Node s  = NodeFactory.createAnon(AnonId.create( "_:"+rowNum  ));
			Node value = entry.getValue();
			triples.add( Triple.create(s, column.getNode(), value) );
		}
		return WrappedIterator.create(triples.iterator());
	}

	@Override
	public Collection<Column> getColumns() {
		return columnList;
	}

	@Override
	public Column getColumn(Node p) {
		return columnIndex.get(p);
	}

	@Override
	public void createColumn(Node p) {
		if (p == null)
			throw new NullPointerException("column name is null");

		if (columnIndex.containsKey(p))
			throw new IllegalArgumentException("column already exists: '"
					+ p.toString());

		columnIndex.put(p, new ColumnImpl(this, p));
		columnList.add(columnIndex.get(p));
		valueIndex.put(p, new HashMap<Object, Node>());
	}

	@Override
	public Row getRow(final Object rowNum) {
		if (rowNum == null)
			throw new NullPointerException("row number is null");
		Row row = rowIndex.get(rowNum);
		if (row != null)
			return row;

		row = new InternalRow(rowNum);
		rowIndex.put(rowNum, row);
		rowList.add(row);

		return row;
	}

	private final void setX(final Object rowNum, final Node p, final Node value) {
		if (p == null)
			throw new NullPointerException("column Node must not be null.");
		if (value == null)
			throw new NullPointerException("value must not be null.");

		if (columnIndex.get(p) == null)
			throw new IllegalArgumentException("column: '" + p
					+ "' does not yet exist.");

		Map<Object, Node> rowNumToValueMap = valueIndex.get(p);
		
		rowNumToValueMap.put(rowNum, value);
		
	}

	private void unSetX(final Object rowNum, final Node p) {

		final Map<Object, Node> rowNumToValueMap = valueIndex.get(p);
		if (!columnIndex.containsKey(p) || rowNumToValueMap == null)
			throw new IllegalArgumentException("column: '" + p
					+ "' does not yet exist.");

		final Object value = rowNumToValueMap.get(rowNum);
		if (value == null)
			return;
		
		rowNumToValueMap.remove(rowNum);

	}

	private final class InternalRow implements Row {
		private final Object key;

		InternalRow(final Object key) {
			this.key = key;
		}

		@Override
		public void set(Node p, Node value) {
			if (value == null)
				unSetX(key, p);
			else
				setX(key, p, value);
		}
	}
}
