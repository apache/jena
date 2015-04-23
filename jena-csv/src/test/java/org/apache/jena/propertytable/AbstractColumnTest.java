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

import java.util.List;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests related to Column.
 *
 */
public abstract class AbstractColumnTest extends BaseTest{


	@Test(expected = NullPointerException.class)
	public void testCreateColumnWithArgNull() {
		table.createColumn(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateListColumnWithAlreadyExistingCoulmnName() {
		table.createColumn(URI("something"));
		table.createColumn(URI("something"));
	}
	
	@Test
	public void testColumnCreate() {
		table.createColumn(URI("something"));
		Assert.assertEquals(1, table.getColumns().size());
		Assert.assertTrue(collectionContains(table.getColumns(), URI("something")));
	}
	
	@Test
	public void testGetColumnValues() {
		Column something = table.createColumn(URI("something"));
		final Row row1 = table.createRow(NodeFactory.createAnon());
		row1.setValue(something, URI("apple"));
		final Row row2 = table.createRow(NodeFactory.createAnon());
		row2.setValue(something, URI("orange"));
		final List<Node> values = something.getValues();
		Assert.assertTrue(values.size() == 2);
		Assert.assertTrue(values.contains( URI("apple")));
		Assert.assertTrue(values.contains(  URI("orange")));
	}
	
	@Test
	public void testGetColumn() {
		table.createColumn(URI("something"));
		Assert.assertNotNull(table.getColumn(URI("something")));
		Assert.assertNull(table.getColumn( URI("nonExistentColumnName")));
	}

	@Test
	public void testGetTable() {
		Column something = table.createColumn(URI("something"));
		Assert.assertEquals(table, something.getTable());
	}

}
