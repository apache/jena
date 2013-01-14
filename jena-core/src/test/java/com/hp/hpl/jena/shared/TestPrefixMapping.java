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

package com.hp.hpl.jena.shared;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.vocabulary.* ;

/**
    Tests PrefixMappingImpl by subclassing AbstractTestPrefixMapping, qv.
*/

public class TestPrefixMapping extends AbstractTestPrefixMapping
    {
    public TestPrefixMapping( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestPrefixMapping.class ); }

    @Override
    protected PrefixMapping getMapping()
        { return new PrefixMappingImpl(); }

    public void testStandard()
        { testStandard( PrefixMapping.Standard ); }

    public void testExtended()
        { testExtended( PrefixMapping.Extended ); }

    public void testStandard( PrefixMapping st )
        {
        assertEquals( RDF.getURI(), st.getNsPrefixURI( "rdf" ) );
        assertEquals( RDFS.getURI(), st.getNsPrefixURI( "rdfs" ) );
        assertEquals( DC_11.getURI(), st.getNsPrefixURI( "dc" ) );
        assertEquals( OWL.getURI(), st.getNsPrefixURI( "owl" ) );
        }

    public void testExtended( PrefixMapping st )
        {
        testStandard( st );
        assertEquals( RSS.getURI(), st.getNsPrefixURI( "rss" ) );
        assertEquals( VCARD.getURI(), st.getNsPrefixURI( "vcard" ) );
        assertEquals( JA.getURI(), st.getNsPrefixURI( "ja" ) );
        assertEquals( "http://www.example.org/", st.getNsPrefixURI( "eg" ) );
        }

    }
