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

public class TestSAPGraph {
	
	public static junit.framework.Test suite() {
    	TestSuite ts = new TestSuite();
    	
    	ts.addTestSuite(TestSAPIndexGraph.class);
    	ts.addTestSuite(TestSAPIndexQuadGraph.class);
    	ts.addTestSuite(TestSAPHashGraph.class);
    	ts.addTestSuite(TestSAPHashQuadGraph.class);
    	
    	return ts;
	}
	
	public static class TestSAPIndexGraph extends AbstractTestGraphSDB {
		public TestSAPIndexGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getIndexSAP();
			return SDBFactory.connectDefaultGraph(store);
		}
	}
	
	public static class TestSAPIndexQuadGraph extends AbstractTestGraphSDB {
		public TestSAPIndexQuadGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getIndexSAP();
			return SDBFactory.connectNamedGraph(store, "http://example.com/graph");
		}
	}
	
	public static class TestSAPHashGraph extends AbstractTestGraphSDB {
		public TestSAPHashGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getHashSAP();
			return SDBFactory.connectDefaultGraph(store);
		}
	}
	
	public static class TestSAPHashQuadGraph extends AbstractTestGraphSDB {
		public TestSAPHashQuadGraph(String arg0) {
			super(arg0);
		}
		
		@Override
		public Graph getGraph()
		{
			Store store = StoreCreator.getHashSAP();
			return SDBFactory.connectNamedGraph(store, "http://example.com/graph");
		}
	}
}
