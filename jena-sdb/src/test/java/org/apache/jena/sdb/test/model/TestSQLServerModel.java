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

package org.apache.jena.sdb.test.model;

import junit.framework.TestSuite;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sdb.SDBFactory ;
import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.test.StoreCreator ;

public class TestSQLServerModel {
	
	public static junit.framework.Test suite() {
    	TestSuite ts = new TestSuite();
    	
    	ts.addTestSuite(TestSQLServerIndexModel.class);
    	ts.addTestSuite(TestSQLServerIndexQuadModel.class);
    	ts.addTestSuite(TestSQLServerHashModel.class);
    	ts.addTestSuite(TestSQLServerHashQuadModel.class);
    	
    	return ts;
	}
	
	public static class TestSQLServerIndexModel extends AbstractTestModelSDB {

		public TestSQLServerIndexModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getIndexSQLServer();
			return SDBFactory.connectDefaultModel(store);
		}
		
	}
	
	public static class TestSQLServerIndexQuadModel extends AbstractTestModelSDB {

		public TestSQLServerIndexQuadModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getIndexSQLServer();
			return SDBFactory.connectNamedModel(store, "http://example.com/graph");
		}
		
	}
	
	public static class TestSQLServerHashModel extends AbstractTestModelSDB {

		public TestSQLServerHashModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getHashSQLServer();
			return SDBFactory.connectDefaultModel(store);
		}
		
	}
	
	public static class TestSQLServerHashQuadModel extends AbstractTestModelSDB {

		public TestSQLServerHashQuadModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getHashSQLServer();
			return SDBFactory.connectNamedModel(store, "http://example.com/graph");
		}
		
	}

}
