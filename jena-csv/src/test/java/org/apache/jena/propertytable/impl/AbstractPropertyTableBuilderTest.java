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

import java.io.StringReader ;

import org.apache.jena.atlas.csv.CSVParser ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.propertytable.BaseTest ;
import org.apache.jena.propertytable.Row ;
import org.junit.Assert ;
import org.junit.Test ;


/**
 * Tests related to PropertyTableBuilder, or more explicitly for the CSV parser in the current release.
 *
 */
public abstract class AbstractPropertyTableBuilderTest extends BaseTest {

	@Test
	public void testFillPropertyTable() {
		CSVParser iterator = csv("a,b\nc,d\ne,f");
		PropertyTableBuilder.fillPropertyTable(table, iterator, csvFilePath);

		Assert.assertEquals(3, table.getColumns().size());
		containsColumn(PropertyTableBuilder.CSV_ROW_NODE);
		containsColumn("a");
		containsColumn("b");

		Assert.assertEquals(2, table.getAllRows().size());
		containsValue(0, "a", "c");
		containsValue(0, "b", "d");

		containsValue(1, "a", "e");
		containsValue(1, "b", "f");

	}

	@Test
	public void testIrregularTable1() {
		CSVParser iterator = csv("a,b\nc\ne,f");
		PropertyTableBuilder.fillPropertyTable(table, iterator, csvFilePath);

		Assert.assertEquals(3, table.getColumns().size());
		containsColumn(PropertyTableBuilder.CSV_ROW_NODE);
		containsColumn("a");
		containsColumn("b");

		Assert.assertEquals(2, table.getAllRows().size());
		containsValue(0, "a", "c");
		nullValue(0, "b");

		containsValue(1, "a", "e");
		containsValue(1, "b", "f");
	}

	@Test
	public void testIrregularTable2() {
		CSVParser iterator = csv("a,b\nc,d1,d2\ne,f");
		PropertyTableBuilder.fillPropertyTable(table, iterator, csvFilePath);

		Assert.assertEquals(3, table.getColumns().size());
		containsColumn(PropertyTableBuilder.CSV_ROW_NODE);
		containsColumn("a");
		containsColumn("b");

		Assert.assertEquals(2, table.getAllRows().size());
		containsValue(0, "a", "c");
		containsValue(0, "b", "d1");

		containsValue(1, "a", "e");
		containsValue(1, "b", "f");
	}

	@Test
	public void testIrregularTable3() {
		CSVParser iterator = csv("a,b\n,d\ne,f");
		PropertyTableBuilder.fillPropertyTable(table, iterator, csvFilePath);

		Assert.assertEquals(3, table.getColumns().size());
		containsColumn(PropertyTableBuilder.CSV_ROW_NODE);
		containsColumn("a");
		containsColumn("b");

		Assert.assertEquals(2, table.getAllRows().size());
		nullValue(0, "a");
		containsValue(0, "b", "d");

		containsValue(1, "a", "e");
		containsValue(1, "b", "f");
	}

	private void nullValue(int rowIndex, String column) {
		Row row = table.getAllRows().get(rowIndex);
		Node v = row.getValue(NodeFactory.createURI(getColumnKey(column)));
		Assert.assertEquals(null, v);
	}

	private void containsValue(int rowIndex, String column, String value) {
		Row row = table.getAllRows().get(rowIndex);
		Node v = row.getValue(NodeFactory.createURI(getColumnKey(column)));
		Assert.assertEquals(value, v.getLiteralValue());
	}

	private String getColumnKey(String column) {
		return PropertyTableBuilder.createColumnKeyURI(csvFilePath, column);
	}

	private void containsColumn(String column) {
		containsColumn(NodeFactory.createURI(getColumnKey(column)));
	}

	private void containsColumn(Node columnKey) {
		Assert.assertTrue(collectionContains(table.getColumns(), columnKey));
	}

	private CSVParser csv(String input) {
	    return CSVParser.create(new StringReader(input));
	}
}
