/**
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

package com.hp.hpl.jena.sparql.engine.index ;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.fail ;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.LinkedHashSet ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import org.junit.Test ;
import org.junit.Before ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap ;
import com.hp.hpl.jena.sparql.engine.index.HashIndexTable.Key ;
import com.hp.hpl.jena.sparql.engine.index.HashIndexTable.MissingBindingException ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

import static com.hp.hpl.jena.reasoner.rulesys.Util.makeIntNode ;

/**
 * Tests the {@link com.hp.hpl.jena.sparql.engine.index.HashIndexTable} and
 * {@link com.hp.hpl.jena.sparql.engine.index.LinearIndex} classes. Also tests
 * that the {@link com.hp.hpl.jena.sparql.engine.index.IndexFactory} instantiates
 * the correct type of index depending on the data.
 * 
 * Contribution from Paul Gearon
 */
public class TestIndexTable {

	private Var[] vars ;

	// sets of vars with different iteration orders
	private Set<Var> order1 ;
	private Set<Var> order2 ;

	private List<Binding> fData ;
	private List<Binding> pData ;
	
	@Before
	public void setup()
	{
		vars = new Var[] { Var.alloc("a"), Var.alloc("b"), Var.alloc("c") } ;
		order1 = new LinkedHashSet<>() ;
		order2 = new LinkedHashSet<>() ;
		for ( int i = 0 ; i < vars.length ; i++ )
		{
			order1.add(vars[i]) ;
			order2.add(vars[vars.length - i - 1]) ;
		}
		
		fData = new ArrayList<>() ;
		pData = new ArrayList<>() ;
		for ( int i = 10 ; i <= 100 ; i += 10 )
		{
			BindingHashMap bindingFull = new BindingHashMap() ;
			BindingHashMap bindingPart = new BindingHashMap() ;
			for ( int b = 0 ; b < vars.length ; b++ )
			{
				bindingFull.add(vars[b], makeIntNode(i + b)) ;  // 10,11,12 - 20,21,22 - 30,31,32 ... 100,101,102
				if ( (i + b) % 7 != 0 ) bindingPart.add(vars[b], makeIntNode(i + b)) ; // skips 21, 42, 70, 91
			}
			fData.add(bindingFull) ;
			pData.add(bindingPart) ;
		}
	}

	@Test
	public void testHashIndexTableConstruction() throws Exception
	{
		new HashIndexTable(order1, fullData()) ;
		assertTrue(IndexFactory.createIndex(order1, fullData()) instanceof HashIndexTable) ;
		assertTrue(IndexFactory.createIndex(order1, partData()) instanceof LinearIndex) ;

		try {
			
			new HashIndexTable(order1, partData()) ;
			fail("Index built without failure on partial bindings") ;
			
		} catch (MissingBindingException e)
		{
			// check that the expected mapping occurred
			Map<Var,Integer> map = e.getMap() ;
			for ( int i = 0 ; i < vars.length ; i++ )
			{
				assertEquals(Integer.valueOf(i), map.get(vars[i])) ;
			}

			// check for rows of {a=10,b=11,c=12}, {a=20,c=22}
			Set<Key> data = e.getData() ;
			assertEquals(2, data.size()) ;

			for ( Key key: data )
			{
				Binding b = LinearIndex.toBinding(key, map) ;
				if ( b.size() == 3 )
				{
					for ( int i = 0 ; i < vars.length ; i++ )
						assertEquals(b.get(vars[i]), makeIntNode(10 + i)) ;
				} else
				{
					assertEquals(b.get(vars[0]), makeIntNode(20)) ;
					assertEquals(b.get(vars[2]), makeIntNode(22)) ;
				}
			}
		}
	}

	@Test
	public void testHashIndexTableData() throws Exception
	{
		// test twice with different internal mappings
		testTableData(new HashIndexTable(order1, fullData())) ;
		testTableData(new HashIndexTable(order2, fullData())) ;
	}
	
	@Test
	public void testLinearIndexTableData() throws Exception
	{
		// test twice with different internal mappings
		testTableData(IndexFactory.createIndex(order1, partData())) ;
		testTableData(IndexFactory.createIndex(order2, partData())) ;

		// test the linear index with full data, since this should also work
		Set<Key> emptyKeys = Collections.emptySet() ;
		Map<Var,Integer> emptyMapping = Collections.emptyMap() ;

		testTableData(new LinearIndex(order1, fullData(), emptyKeys, emptyMapping)) ;
		testTableData(new LinearIndex(order2, fullData(), emptyKeys, emptyMapping)) ;
		
		// construction directly from part data should also work
		testTableData(new LinearIndex(order1, partData(), emptyKeys, emptyMapping)) ;
		testTableData(new LinearIndex(order2, partData(), emptyKeys, emptyMapping)) ;
	}
	
	private void testTableData(IndexTable index) throws Exception
	{
		// positive test for matching
		for ( Binding b: fData )
			assertTrue(index.containsCompatibleWithSharedDomain(b)) ;

		assertTrue(index.containsCompatibleWithSharedDomain(binding("abcd", 10, 11, 12, 13))) ;
		assertTrue(index.containsCompatibleWithSharedDomain(binding("ab", 10, 11))) ;
		assertTrue(index.containsCompatibleWithSharedDomain(binding("bc", 11, 12))) ;
		assertTrue(index.containsCompatibleWithSharedDomain(binding("ac", 10, 12))) ;
		assertTrue(index.containsCompatibleWithSharedDomain(binding("a", 10))) ;
		assertTrue(index.containsCompatibleWithSharedDomain(binding("ab", 70, 71))) ;
		assertTrue(index.containsCompatibleWithSharedDomain(binding("bc", 71, 72))) ;
		assertTrue(index.containsCompatibleWithSharedDomain(binding("ac", 70, 72))) ;
		assertTrue(index.containsCompatibleWithSharedDomain(binding("a", 80))) ;  // a=70 won't match for partData

		// negative test for matching
		assertFalse(index.containsCompatibleWithSharedDomain(binding("abc", 10, 11, 11))) ;
		assertFalse(index.containsCompatibleWithSharedDomain(binding("d", 10))) ;
		assertFalse(index.containsCompatibleWithSharedDomain(binding("abc", 10, 21, 32))) ;
		assertFalse(index.containsCompatibleWithSharedDomain(binding("xyz", 10, 11, 12))) ;
	}

	private QueryIterator fullData() { return new QueryIterPlainWrapper(fData.iterator()) ; }

	private QueryIterator partData() { return new QueryIterPlainWrapper(pData.iterator()) ; }


	/**
	 * A convenience method that creates a binding of Vars with single letter names bound to integers.
	 * @param varNames A string of variable names. The length must match the number of integers to bind to.
	 * @param ints The values of the integers to be bound to the variables.
	 */
	private static Binding binding(String varNames, Integer... ints)
	{
		assert varNames.length() == ints.length ;

		BindingHashMap b = new BindingHashMap() ;
		for ( int s = 0 ; s < varNames.length() ; s++ )
			b.add(Var.alloc(varNames.substring(s, s + 1)), makeIntNode(ints[s])) ;
		return b ;
	}
}

