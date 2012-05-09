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

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.*;

public class NewRegressionResourceMethods extends NewRegressionBase
    {
    public NewRegressionResourceMethods( String name )
        { super( name );  }

    public static Test suite()
        { return new TestSuite( NewRegressionResourceMethods.class ); }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    
    protected Resource r;

    protected final String lang = "en";
    
    protected Literal tvLiteral;

    protected Resource tvResource;
    
    @Override
    public void setUp()
        { 
        m = getModel();
        tvLiteral = m.createLiteral( "test 12 string 2" );
        tvResource = m.createResource();
        r = 
            m.createResource()
            .addLiteral( RDF.value, tvBoolean )
            .addLiteral( RDF.value, tvByte )
            .addLiteral( RDF.value, tvShort )
            .addLiteral( RDF.value, tvInt )
            .addLiteral( RDF.value, tvLong )
            .addLiteral( RDF.value, tvChar )
            .addLiteral( RDF.value, tvFloat )
            .addLiteral( RDF.value, tvDouble )
            .addProperty( RDF.value, tvString )
            .addProperty( RDF.value, tvString , lang )
            .addLiteral( RDF.value, tvObject )
            .addProperty( RDF.value, tvLiteral )
            .addProperty( RDF.value, tvResource )
            ;
        }
    
    public void testBoolean()
        { assertTrue( r.hasLiteral( RDF.value, tvBoolean ) ); }
    
    public void testByte()
        { assertTrue( r.hasLiteral( RDF.value, tvByte ) ); }
    
    public void testShort()
        { assertTrue( r.hasLiteral( RDF.value, tvShort ) ); }
    
    public void testInt()
        { assertTrue( r.hasLiteral( RDF.value, tvInt ) ); }
    
    public void testLong()
        { assertTrue( r.hasLiteral( RDF.value, tvLong ) ); }
    
    public void testChar()
        { assertTrue( r.hasLiteral( RDF.value, tvChar ) ); }
    
    public void testFloat()
        { assertTrue( r.hasLiteral( RDF.value, tvFloat ) ); }
    
    public void testDouble()
        { assertTrue( r.hasLiteral( RDF.value, tvDouble ) ); }
    
    public void testString()
        { assertTrue( r.hasProperty( RDF.value, tvString ) ); }
    
    public void testStringWithLanguage()
        { assertTrue( r.hasProperty( RDF.value, tvString, lang ) ); }
    
    public void testObject()
        { assertTrue( r.hasLiteral( RDF.value, tvObject ) ); }
    
    public void testLiteral()
        { assertTrue( r.hasProperty( RDF.value, tvLiteral ) ); }
    
    public void testResource()
        { assertTrue( r.hasProperty( RDF.value, tvResource ) ); }
    
    public void testCorrectSubject()
        { assertEquals( r, r.getRequiredProperty( RDF.value ).getSubject() ); }
    
    public void testNoSuchPropertyException()
        {
        try { r.getRequiredProperty( RDF.type ); fail( "missing property should throw exception" ); }
        catch (PropertyNotFoundException e) { pass(); }
        }
    
    public void testNoSuchPropertyNull()
        { assertNull( r.getProperty( RDF.type ) );  }
    
    public void testAllSubjectsCorrect()
        {
        testHasSubjectR( m.listStatements() );
        testHasSubjectR( r.listProperties() );
        }

    protected void testHasSubjectR( StmtIterator it )
        { while (it.hasNext()) assertEquals( r, it.nextStatement().getSubject() ); }
    
    public void testCountsCorrect()
        {
        assertEquals( 13, iteratorToList( m.listStatements() ).size() );
        assertEquals( 13, iteratorToList( r.listProperties( RDF.value ) ).size() );
        assertEquals( 0, iteratorToList( r.listProperties( RDF.type ) ).size() );
        }
    
    public void testRemoveProperties()
        {
        r.removeProperties();
        assertEquals( false, m.listStatements( r, null, (RDFNode) null ).hasNext() );
        }
    }
