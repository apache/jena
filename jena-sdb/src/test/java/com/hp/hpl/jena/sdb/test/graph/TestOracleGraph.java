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

public class TestOracleGraph {
	
	public static junit.framework.Test suite() {
    	TestSuite ts = new TestSuite();
    	
    	ts.addTestSuite(TestOracleIndexGraph.class);
    	ts.addTestSuite(TestOracleIndexQuadGraph.class);
    	ts.addTestSuite(TestOracleHashGraph.class);
    	ts.addTestSuite(TestOracleHashQuadGraph.class);
    	
    	return ts;
	}
	
	public static class TestOracleIndexGraph extends AbstractTestGraphSDB {
		public TestOracleIndexGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getIndexOracle();
			return SDBFactory.connectDefaultGraph(store);
		}
	}
	
	public static class TestOracleIndexQuadGraph extends AbstractTestGraphSDB {
		public TestOracleIndexQuadGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getIndexOracle();
			return SDBFactory.connectNamedGraph(store, "http://example.com/graph");
		}
	}
	
	public static class TestOracleHashGraph extends AbstractTestGraphSDB {
		public TestOracleHashGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getHashOracle();
			return SDBFactory.connectDefaultGraph(store);
		}
	}
	
	public static class TestOracleHashQuadGraph extends AbstractTestGraphSDB {
		public TestOracleHashQuadGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getHashOracle();
			return SDBFactory.connectNamedGraph(store, "http://example.com/graph");
		}
	}
}
