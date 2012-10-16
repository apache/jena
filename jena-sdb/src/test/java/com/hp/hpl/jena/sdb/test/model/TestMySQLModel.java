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

package com.hp.hpl.jena.sdb.test.model;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.test.StoreCreator;

public class TestMySQLModel {
	
	public static junit.framework.Test suite() {
    	TestSuite ts = new TestSuite();
    	
    	ts.addTestSuite(TestMySQLIndexModel.class);
    	ts.addTestSuite(TestMySQLIndexQuadModel.class);
    	ts.addTestSuite(TestMySQLHashModel.class);
    	ts.addTestSuite(TestMySQLHashQuadModel.class);
    	
    	return ts;
	}
	
	public static class TestMySQLIndexModel extends AbstractTestModelSDB {

		public TestMySQLIndexModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getIndexMySQL();
			return SDBFactory.connectDefaultModel(store);
		}
		
	}
	
	public static class TestMySQLIndexQuadModel extends AbstractTestModelSDB {

		public TestMySQLIndexQuadModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getIndexMySQL();
			return SDBFactory.connectNamedModel(store, "http://example.com/graph");
		}
		
	}
	
	public static class TestMySQLHashModel extends AbstractTestModelSDB {

		public TestMySQLHashModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getHashMySQL();
			return SDBFactory.connectDefaultModel(store);
		}
		
	}
	
	public static class TestMySQLHashQuadModel extends AbstractTestModelSDB {

		public TestMySQLHashQuadModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getHashMySQL();
			return SDBFactory.connectNamedModel(store, "http://example.com/graph");
		}
		
	}

}
