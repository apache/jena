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

public class TestVocabDC10 extends VocabTestBase
    {
    public TestVocabDC10(String name)
    	{ super(name); }

	public static TestSuite suite()
		{ return new TestSuite( TestVocabDC10.class ); }

	public void testDC10()
		{
		String ns = "http://purl.org/dc/elements/1.0/";
        assertProperty( ns + "contributor", DC_10.contributor );
        assertProperty( ns + "coverage", DC_10.coverage );
        assertProperty( ns + "creator", DC_10.creator );
        assertProperty( ns + "date", DC_10.date );
        assertProperty( ns + "description", DC_10.description );
        assertProperty( ns + "format", DC_10.format );
        assertProperty( ns + "identifier", DC_10.identifier );
        assertProperty( ns + "language", DC_10.language );
        assertProperty( ns + "publisher", DC_10.publisher );
        assertProperty( ns + "relation", DC_10.relation );
        assertProperty( ns + "rights", DC_10.rights );
        assertProperty( ns + "source", DC_10.source );
        assertProperty( ns + "subject", DC_10.subject );
        assertProperty( ns + "title", DC_10.title );
        assertProperty( ns + "type", DC_10.type );
		}
	}
