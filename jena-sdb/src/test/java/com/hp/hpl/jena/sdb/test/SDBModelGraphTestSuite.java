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

package com.hp.hpl.jena.sdb.test;

import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.hp.hpl.jena.sdb.test.graph.TestHSQLGraph;
import com.hp.hpl.jena.sdb.test.graph.TestMySQLGraph;
import com.hp.hpl.jena.sdb.test.graph.TestPgSQLGraph;
import com.hp.hpl.jena.sdb.test.graph.TestSAPGraph;
import com.hp.hpl.jena.sdb.test.model.TestHSQLModel;
import com.hp.hpl.jena.sdb.test.model.TestMySQLModel;
import com.hp.hpl.jena.sdb.test.model.TestPgSQLModel;
import com.hp.hpl.jena.sdb.test.model.TestSAPModel;

@RunWith(AllTests.class)
public class SDBModelGraphTestSuite extends TestSuite
{
    static boolean includeMySQL = true ;
    static boolean includeHSQL = true ;
    static boolean includePGSQL = true ;
    static boolean includeSAP = true ;
    
    public static junit.framework.Test suite() {
    	TestSuite ts = new TestSuite();
    	
        if ( includeMySQL )
        {
        	ts.addTestSuite(TestMySQLModel.TestMySQLHashModel.class);
        	ts.addTestSuite(TestMySQLGraph.TestMySQLHashGraph.class);
        }
        
        
        if ( includeHSQL )
        {
        	ts.addTestSuite(TestHSQLModel.TestHSQLHashModel.class);
        	ts.addTestSuite(TestHSQLGraph.TestHSQLHashGraph.class);
        }
        
        if ( includePGSQL )
        {
        	ts.addTestSuite(TestPgSQLModel.TestPgSQLHashModel.class);
        	ts.addTestSuite(TestPgSQLGraph.TestPgSQLHashGraph.class);
        }
        
        if ( includeSAP )
        {
        	ts.addTestSuite(TestSAPModel.TestSAPHashModel.class);
        	ts.addTestSuite(TestSAPGraph.TestSAPHashGraph.class);
        }
        
        return ts;
    }

}
