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

package com.hp.hpl.jena.sdb.test.graph;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.test.StoreCreator;

public class TestDB2Graph {
	
	public static junit.framework.Test suite() {
    	TestSuite ts = new TestSuite();
    	
    	ts.addTestSuite(TestDB2IndexGraph.class);
    	ts.addTestSuite(TestDB2IndexQuadGraph.class);
    	ts.addTestSuite(TestDB2HashGraph.class);
    	ts.addTestSuite(TestDB2HashQuadGraph.class);
    	
    	return ts;
	}
	
	public static class TestDB2IndexGraph extends AbstractTestGraphSDB {
		public TestDB2IndexGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getIndexDB2();
			return SDBFactory.connectDefaultGraph(store);
		}
	}
	
	public static class TestDB2IndexQuadGraph extends AbstractTestGraphSDB {
		public TestDB2IndexQuadGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getIndexDB2();
			return SDBFactory.connectNamedGraph(store, "http://example.com/graph");
		}
	}
	
	public static class TestDB2HashGraph extends AbstractTestGraphSDB {
		public TestDB2HashGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getHashDB2();
			return SDBFactory.connectDefaultGraph(store);
		}
	}
	
	public static class TestDB2HashQuadGraph extends AbstractTestGraphSDB {
		public TestDB2HashQuadGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getHashDB2();
			return SDBFactory.connectNamedGraph(store, "http://example.com/graph");
		}
	}
}
