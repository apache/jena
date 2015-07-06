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

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests related to Row.
 *
 */
public abstract class AbstractRowTest extends AbstractColumnTest{

	@Test
	public void testAddRowValue() {

		Column something = table.createColumn(URI("something"));
		Column somethingElse = table.createColumn(URI("somethingElse"));

		row.setValue(something, URI("apple"));
		row.setValue(somethingElse, URI("orange"));

		Assert.assertEquals(URI("apple"), row.getValue(something));
		Assert.assertEquals(URI("orange"), row.getValue(somethingElse));
	}
	
	@Test
	public void testUnsetRowValue() {
		Column something = table.createColumn(URI("something"));
		row.setValue( something , URI("apple"));
		Assert.assertEquals(URI("apple"), row.getValue(something));
		row.setValue( something , null);
		Assert.assertEquals(null, row.getValue(something));
	}
	
	@Test(expected=NullPointerException.class)
	public void testGetRowWithNullKey() {
		table.getRow(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddValueToNotExistingColumn() {
		row.setValue(table.getColumn(URI("something")), URI("apple"));
	}
	

	
	@Test(expected=IllegalArgumentException.class)
	public void testGetListWithANonExistantColumn() {
		Assert.assertNull(row.getValue( NodeFactory.createAnon() ));
	}
	
	@Test
	public void testGetListWithAnMissingRowValue() {
		Column something = table.createColumn(URI("something"));
		Assert.assertNull(row.getValue(something));
	}

    @Test
    public void testGetValue() {
    	Column something = table.createColumn(URI("something"));
        row.setValue(something, URI("apple"));
        Node value = row.getValue(something);
        Assert.assertEquals(URI("apple"), value);
    }
    
    @Test
    public void testRowExistsFalse(){
    	Assert.assertNull(table.getRow(NodeFactory.createAnon()));
    }
    
    @Test
    public void testRowExistsTrue() {
		Assert.assertNotNull(table.getRow(rowSubject));
    }

    @Test
    public void testGetRowFalseAndDoesntCreateRow() {
    	Assert.assertNull(table.getRow(NodeFactory.createAnon()));
    	Assert.assertNull(table.getRow(NodeFactory.createAnon()));
    }
    
    @Test(expected=IllegalArgumentException.class)
	public void testGetValueBeforeColumnExists() {
		row.getValue(URI("nonexistentColumnX"));
	}
}
