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

package com.hp.hpl.jena.db.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.db.impl.DBType;
import com.hp.hpl.jena.test.JenaTestBase;

public class TestDBType extends JenaTestBase {

	public TestDBType ( String name ) { super(name); }
	public static TestSuite suite() { return new TestSuite( TestDBType.class ); }
	
	public void testFromName() {
		assertEquals(DBType.DERBY, DBType.fromName("Derby")) ;
		assertEquals(DBType.HSQL, DBType.fromName("HSQL")) ;
		assertEquals(DBType.HSQLDB, DBType.fromName("HSQLDB")) ;
		assertEquals(DBType.MSSQL, DBType.fromName("MsSQL")) ;
		assertEquals(DBType.MYSQL, DBType.fromName("MySQL")) ;
		assertEquals(DBType.ORACLE_LOB, DBType.fromName("Oracle_LOB")) ;
		assertEquals(DBType.ORACLE, DBType.fromName("Oracle")) ;
		assertEquals(DBType.POSTGRESQL, DBType.fromName("PostgreSQL")) ;
	}

	public void testFromNameIsCaseInsensitive() {
		assertEquals(DBType.ORACLE, DBType.fromName("oRacle")) ;
		assertEquals(DBType.ORACLE, DBType.fromName("ORACLE")) ;
	}

	public void testGetDriverClassName() throws ClassNotFoundException {
		for (DBType type : DBType.values()) {
			Class.forName( type.getDriverClassName() ) ;
		}
	}

	public void testGetSupportedTypesAsString() {
		String supported = DBType.getSupportedTypesAsString() ;
		for (DBType type : DBType.values()) {
			supported.concat(type.getDisplayName()) ;
		}
	}

}
