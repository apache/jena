package org.apache.jena.propertytable.impl;

import org.junit.After;
import org.junit.Before;

public class PropertyTableBuilderForHashMapImplTest extends AbstractPropertyTableBuilderTest{
	@Before
	public void setUp() {
		table = new PropertyTableHashMapImpl();
	}

	@After
	public void tearDown() {
		table = null;
	}
}
