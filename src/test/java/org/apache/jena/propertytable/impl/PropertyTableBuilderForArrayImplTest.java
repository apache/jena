package org.apache.jena.propertytable.impl;

import org.junit.After;
import org.junit.Before;

public class PropertyTableBuilderForArrayImplTest extends AbstractPropertyTableBuilderTest{
	
	private static int rowNum = 10;
	private static int columnNum = 10 ;
	
	@Before
	public void setUp() {
		table = new PropertyTableArrayImpl(rowNum, columnNum);
	}

	@After
	public void tearDown() {
		table = null;
	}

}
