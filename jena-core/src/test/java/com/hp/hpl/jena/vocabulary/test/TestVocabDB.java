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

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

public class TestVocabDB extends VocabTestBase
    {
    public TestVocabDB(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestVocabDB.class ); }

    public void testXX()
        {
		String ns = "http://jena.hpl.hp.com/2003/04/DB#";
    /* */
        assertResource( ns + "SystemGraph", DB.systemGraphName );
        assertProperty( ns + "EngineType", DB.engineType );
        assertProperty( ns + "DriverVersion", DB.driverVersion );
        assertProperty( ns + "FormatDate", DB.formatDate );
        assertProperty( ns + "Graph", DB.graph );
        // assertProperty( ns + "MaxLiteral", DB.maxLiteral );
    /* */
        assertProperty( ns + "GraphName", DB.graphName );
        assertProperty( ns + "GraphType", DB.graphType );
        assertProperty( ns + "GraphLSet", DB.graphLSet );
        assertProperty( ns + "GraphPrefix", DB.graphPrefix );
        assertProperty( ns + "GraphId", DB.graphId );
        assertProperty( ns + "GraphDBSchema", DB.graphDBSchema );
        assertProperty( ns + "StmtTable", DB.stmtTable );
        assertProperty( ns + "ReifTable", DB.reifTable );
    /* */
        assertProperty( ns + "PrefixValue", DB.prefixValue );
        assertProperty( ns + "PrefixURI", DB.prefixURI );
    /* */
        assertProperty( ns + "LSetName", DB.lSetName );
        assertProperty( ns + "LSetType", DB.lSetType );
        assertProperty( ns + "LSetPSet", DB.lSetPSet );
    /* */
        assertProperty( ns + "PSetName", DB.pSetName );
        assertProperty( ns + "PSetType", DB.pSetType );
        assertProperty( ns + "PSetTable", DB.pSetTable );
    /* */
        assertResource( ns + "undefined", DB.undefined );
        }
    }
