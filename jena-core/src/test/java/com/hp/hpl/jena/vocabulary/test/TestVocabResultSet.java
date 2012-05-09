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

/**
     @author kers
*/
public class TestVocabResultSet extends VocabTestBase
    {
    public TestVocabResultSet(String name)
    	{ super(name); }

	public static TestSuite suite()
		{ return new TestSuite( TestVocabResultSet.class ); }

	public void testResultSet()
		{
		String ns = "http://jena.hpl.hp.com/2003/03/result-set#";
        assertResource( ns + "ResultSolution", ResultSet.ResultSolution );
        assertResource( ns + "ResultBinding", ResultSet.ResultBinding );
        assertResource( ns + "ResultSet", ResultSet.ResultSet );
        assertProperty( ns + "value", ResultSet.value );
        assertProperty( ns + "resultVariable", ResultSet.resultVariable );
        assertProperty( ns + "variable", ResultSet.variable );
        assertProperty( ns + "size", ResultSet.size );
        assertProperty( ns + "binding", ResultSet.binding );
        assertProperty( ns + "solution", ResultSet.solution );
        assertResource( ns + "undefined", ResultSet.undefined );
		}
    }
