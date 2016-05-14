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

import org.apache.jena.propertytable.AbstractPropertyTableTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for PropertyTableArrayImpl
 *
 */
public class PropertyTableArrayImplTest extends AbstractPropertyTableTest{
	
	private static int rowNum = 10;
	private static int columnNum = 10 ;
	
	@Before
	public void setUp() {
		table = new PropertyTableArrayImpl(rowNum, columnNum);
		table2 = new PropertyTableArrayImpl(rowNum, columnNum);
		row = table.createRow(rowSubject);

	}

	@After
	public void tearDown() {
		table = null;
		table2 = null;
		row = null;
	}
	
	@Test
	public void testColumnOutofBounds1() {
		for (int i=0;i<columnNum;i++){
			table.createColumn(URI("something_"+i));
		}
		Assert.assertEquals(columnNum, table.getColumns().size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testColumnOutofBounds2() {
		for (int i=0;i<columnNum+1;i++){
			table.createColumn(URI("something_"+i));
		}
	}
	
	@Test
	public void testRowOutofBounds1() {
		
		// we've already created a new Row in @Before
		for (int i=0;i<rowNum-1;i++){
			table.createRow(URI("something_"+i));
		}
		Assert.assertEquals(rowNum, table.getAllRows().size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRowOutofBounds2() {
		
		// we've already created a new Row in @Before
		for (int i=0;i<rowNum;i++){
			table.createRow(URI("something_"+i));
		}
	}
}
