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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorConcat;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.propertytable.Column;
import org.apache.jena.propertytable.PropertyTable;
import org.apache.jena.propertytable.Row;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/**
 * A PropertyTable Implementation using a two dimension array.
 * It contains SPO and PSO indexes.
 * 
 */
public class PropertyTableArrayImpl implements PropertyTable {
	
	private final List<Node> rowList;
	private final List<Node> columnList;

	private final Map<Node, Integer> rowKeyToIndex;
	private final Map<Node, Integer> columnKeyToIndex;
	private final Node[][] array;
	private final int rowNum;
	private final int columnNum;
	
	public PropertyTableArrayImpl(int rowNum, int columnNum){
		rowList = new ArrayList<Node>(rowNum);
		columnList = new ArrayList<Node>(columnNum);
		rowKeyToIndex = new HashMap<Node, Integer>();
		columnKeyToIndex = new HashMap<Node, Integer>();
		this.rowNum = rowNum;
		this.columnNum = columnNum;
		array = new Node [rowNum][columnNum];
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator(Column column, Node value) {
		if (column == null || column.getColumnKey() == null)
			throw new NullPointerException("column is null");
		
		if (value == null){
			throw new NullPointerException("value is null");
		}
		
		ArrayList<Triple> triples = new ArrayList<Triple>();
		
		Node p = column.getColumnKey();
		Integer columnIndex = this.columnKeyToIndex.get(p);
		if (columnIndex != null){
			for(int rowIndex=0; rowIndex< rowList.size();rowIndex++){
				if ( value.equals( this.get(rowIndex, columnIndex))){
					triples.add(Triple.create(rowList.get(rowIndex), p, value));
				}
			}
		}
		return WrappedIterator.create(triples.iterator());
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator(Column column) {
		
		if (column == null || column.getColumnKey() == null)
			throw new NullPointerException("column is null");
		
		ArrayList<Triple> triples = new ArrayList<Triple>();
		
		Node p = column.getColumnKey();
		Integer columnIndex = this.columnKeyToIndex.get(p);
		if (columnIndex != null){
			for(int rowIndex=0; rowIndex< rowList.size();rowIndex++){
				if(this.get(rowIndex, columnIndex)!=null){
					triples.add(Triple.create(rowList.get(rowIndex), p, this.get(rowIndex, columnIndex)));
				}
			}
		}
		return WrappedIterator.create(triples.iterator());
	}

	@Override
	public ExtendedIterator<Triple> getTripleIterator(Node value) {
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
	public ExtendedIterator<Triple> getTripleIterator(Row row) {
		if (row == null || row.getRowKey() == null)
			throw new NullPointerException("row is null");
		
		ArrayList<Triple> triples = new ArrayList<Triple>();
	    Integer rowIndex = this.rowKeyToIndex.get(row.getRowKey());

		if (rowIndex != null){
			for(int columnIndex=0; columnIndex < columnList.size(); columnIndex++){
				triples.add(Triple.create( row.getRowKey(), columnList.get(columnIndex), this.get(rowIndex, columnIndex)));
			}
		}
		return WrappedIterator.create(triples.iterator());
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
	public Collection<Column> getColumns() {
		Collection<Column> columns = new ArrayList<Column>();
		for(Node p: columnKeyToIndex.keySet() ){
			columns.add(new ColumnImpl(this, p));
		}
		return columns;
	}

	@Override
	public Column getColumn(Node p) {
		if (p == null)
			throw new NullPointerException("column name is null");
	    Integer columnIndex = columnKeyToIndex.get(p);
	    return (columnIndex == null)
	        ? null : new ColumnImpl(this, p);
	}

	@Override
	public Column createColumn(Node p) {
		if (p == null)
			throw new NullPointerException("column name is null");

		if (columnKeyToIndex.containsKey(p))
			throw new IllegalArgumentException("column already exists: '"
					+ p.toString());
		
		if (columnList.size()>= columnNum)
			throw new IllegalArgumentException("cannot create new column for max column count: " + columnNum);
		
		columnList.add(p);
		columnKeyToIndex.put(p, columnList.indexOf(p));
		return getColumn(p);
	}

	@Override
	public Row getRow(Node s) {
		if (s == null)
			throw new NullPointerException("subject node is null");
		
		Integer rowIndex = rowKeyToIndex.get(s);
		return (rowIndex == null) ? null : new InternalRow(rowIndex);
	}

	@Override
	public Row createRow(Node s) {
		Row row = this.getRow(s);
		if (row != null)
			return row;

		if (rowList.size()>= rowNum)
			throw new IllegalArgumentException("cannot create new row for max row count: " + rowNum);
		
		rowList.add(s);
		int rowIndex = rowList.indexOf(s);
		rowKeyToIndex.put(s, rowIndex);		
		
		return new InternalRow(rowIndex);
	}
	
	private void set(int rowIndex, int columnIndex, Node value) {
		
		if (rowIndex >= rowList.size())
			throw new IllegalArgumentException("row index out of bound: " + rowList.size());
		if (columnIndex >= columnList.size())
			throw new IllegalArgumentException("column index out of bound: " + columnList.size());
		array[rowIndex][columnIndex] = value;
	}
	
	public Node get(int rowIndex, int columnIndex) {
	    if (rowIndex >= rowList.size())
			throw new IllegalArgumentException("row index out of bound: " + rowList.size());
		if (columnIndex >= columnList.size())
			throw new IllegalArgumentException("column index out of bound: " + columnList.size());
		return array[rowIndex][columnIndex];
    }

	@Override
	public List<Row> getAllRows() {
		ArrayList<Row> rows = new ArrayList<Row>();
		for (int rowIndex=0;rowIndex<rowList.size();rowIndex++){
			rows.add( new InternalRow(rowIndex));
		}
		return rows;
	}

	@Override
	public List<Node> getColumnValues(Column column) {
		if (column == null || column.getColumnKey() == null)
			throw new NullPointerException("column is null");
		
		Node p = column.getColumnKey();
		Integer columnIndex = this.columnKeyToIndex.get(p);
		
		List<Node> list = new ArrayList<Node>();
		
		if (columnIndex != null){
			for(int rowIndex=0; rowIndex< rowList.size();rowIndex++){
				if(this.get(rowIndex, columnIndex)!=null){
					list.add(this.get(rowIndex, columnIndex));
				}
			}
		}
		return list;
	}
	

	@Override
	public Collection<Row> getMatchingRows(Column column, Node value) {
		
		if (column == null || column.getColumnKey() == null)
			throw new NullPointerException("column is null");
		
		if (value == null){
			throw new NullPointerException("value is null");
		}
		
		final ArrayList<Row> matchingRows = new ArrayList<Row>();
		
		Node p = column.getColumnKey();
		Integer columnIndex = this.columnKeyToIndex.get(p);
		if (columnIndex != null){
			for(int rowIndex=0; rowIndex< rowList.size();rowIndex++){
				if ( value.equals( this.get(rowIndex, columnIndex))){
					matchingRows.add( this.getRow( rowList.get(rowIndex) ));
				}
			}
		}
		return matchingRows;
	}
	
	private final class InternalRow implements Row {

	    final int rowIndex;

	    InternalRow(int rowIndex) {
	      this.rowIndex = rowIndex;
	    }
		
		@Override
		public PropertyTable getTable() {
			return PropertyTableArrayImpl.this;
		}

		@Override
		public void setValue(Column column, Node value) {
			if (column == null || column.getColumnKey() == null)
				throw new NullPointerException("column is null");
			
		    Integer columnIndex = columnKeyToIndex.get(column.getColumnKey());
		    if (columnIndex == null)
		    	throw new IllegalArgumentException("column index does not exist: " + column.getColumnKey());

		    set(rowIndex, columnIndex, value);
			
		}

		@Override
		public Node getValue(Column column) {
			if (column == null)
				throw new NullPointerException("column is null");
			return this.getValue(column.getColumnKey());
		}

		@Override
		public Node getValue(Node columnKey) {
			if (columnKey == null)
				throw new NullPointerException("column key is null");
			
		    Integer columnIndex = columnKeyToIndex.get(columnKey);
		    if (columnIndex == null)
		    	throw new IllegalArgumentException("column index does not exist: " + columnKey);
		    
		    return get(rowIndex, columnIndex);
		}

		@Override
		public Node getRowKey() {
			return rowList.get(rowIndex);
		}

		@Override
		public ExtendedIterator<Triple> getTripleIterator() {
			ArrayList<Triple> triples = new ArrayList<Triple>();
			for (int columnIndex=0;columnIndex<columnList.size();columnIndex++) {
				triples.add(Triple.create(getRowKey(), columnList.get(columnIndex), get(rowIndex, columnIndex)));
			}
			return WrappedIterator.create(triples.iterator());
		}

		@Override
		public Collection<Column> getColumns() {
			return PropertyTableArrayImpl.this.getColumns();
		}
		
	}




}
