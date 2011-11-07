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

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
//import com.hp.hpl.jena.regression.Regression.ResTestObjF;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.regression.Regression.*;

public class NewRegressionResources extends NewRegressionBase
    {
    public NewRegressionResources( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionResources.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    public void testCreateAnonResource()
        {
        Model m = getModel();
        Resource r = m.createResource();
        assertTrue( r.isAnon() );
        assertNull( r.getURI() );
        assertNull( r.getNameSpace() );
        assertNull( r.getLocalName() );
        }    
    
    public void testCreateAnonResourceWithNull()
        {
        Model m = getModel();
        Resource r = m.createResource( (String) null );
        assertTrue( r.isAnon() );
        assertNull( r.getURI() );
        assertNull( r.getNameSpace() );
        assertNull( r.getLocalName() );
        }
    
    public void testCreateNamedResource()
        {
        Model m = getModel();
        String uri = "http://aldabaran.hpl.hp.com/foo";
        assertEquals( uri, m.createResource( uri ).getURI() );
        }
    
    public void testCreateTypedAnonResource()
        {
        Model m = getModel();
        Resource r = m.createResource( RDF.Property );
        assertTrue( r.isAnon() );
        assertTrue( m.contains( r, RDF.type, RDF.Property ) );
        }

    public void testCreateTypedNamedresource()
        {
        Model m = getModel();
        String uri = "http://aldabaran.hpl.hp.com/foo";
        Resource r = m.createResource( uri, RDF.Property );
        assertEquals( uri, r.getURI() );
        assertTrue( m.contains( r, RDF.type, RDF.Property ) );
        }
    
//    public void testCreateAnonByFactory()
//        {
//        Model m = getModel();
//        assertTrue( m.createResource( new ResTestObjF() ).isAnon() );
//        }
    
//    public void testCreateResourceByFactory()
//        {
//        Model m = getModel();
//        String uri = "http://aldabaran.hpl.hp.com/foo";
//        assertEquals( uri, m.createResource( uri, new ResTestObjF() ).getURI() );
//        }
    
    public void testCreateNullPropertyFails()
        {
        Model m = getModel();
        try { m.createProperty( null ); fail( "should not create null property" ); }
        catch (InvalidPropertyURIException e) { pass(); }
        }
    
    public void testCreatePropertyOneArg()
        {
        Model m = getModel();
        Property p = m.createProperty( "abc/def" );
        assertEquals( "abc/", p.getNameSpace() );
        assertEquals( "def", p.getLocalName() );
        assertEquals( "abc/def", p.getURI() );
        }
    
    public void testCreatePropertyTwoArgs()
        {
        Model m = getModel();
        Property p = m.createProperty( "abc/", "def" );
        assertEquals( "abc/", p.getNameSpace() );
        assertEquals( "def", p.getLocalName() );
        assertEquals( "abc/def", p.getURI() );
        }
    
    public void testCreatePropertyStrangeURI()
        {
        Model m = getModel();
        String uri = RDF.getURI() + "_345";
        Property p = m.createProperty( uri );
        assertEquals( RDF.getURI(), p.getNameSpace() );
        assertEquals( "_345", p.getLocalName() );
        assertEquals( uri, p.getURI() );
        }
    
    public void testCreatePropertyStrangeURITwoArgs()
        {
        Model m = getModel();
        String local = "_345";
        Property p = m.createProperty( RDF.getURI(), local );
        assertEquals( RDF.getURI(), p.getNameSpace() );
        assertEquals( local, p.getLocalName() );
        assertEquals( RDF.getURI() + local, p.getURI() );
        }
    
    public void testEnhancedResources()
        {
        Model m = getModel();
        Resource r = new ResourceImpl( (ModelCom) m );
        testResource( m, r, 0 );
    
        testResource( m, m.createBag(), 1 );
        testContainer( m, m.createBag(), m.createBag());
    
        testResource( m, m.createAlt(), 1 );
        testContainer( m, m.createAlt(), m.createAlt() );
    
        testResource( m, m.createSeq(), 1 );
        testContainer( m, m.createSeq(), m.createSeq() );
        // testSeq( m, m.createSeq(), m.createSeq(), m.createSeq(),
         //                     m.createSeq(), m.createSeq(), m.createSeq(),
       //                       m.createSeq() );
        }
    
    protected Set<Object> setOf( Object x )
        {
        Set<Object> result = new HashSet<Object>();
        result.add( x );
        return result;
        }
    public void testResource( Model m, Resource r, int numProps ) 
        {
        Literal tvLiteral = m.createLiteral( "test 12 string 2" );
        Resource tvResource = m.createResource();
        String lang = "fr";
    //
        assertTrue( r.addLiteral( RDF.value, tvBoolean ).hasLiteral( RDF.value, tvBoolean ) );
        assertTrue( r.addLiteral( RDF.value, tvByte ).hasLiteral( RDF.value, tvByte ) );
        assertTrue( r.addLiteral( RDF.value, tvShort ).hasLiteral( RDF.value, tvShort ) );
        assertTrue( r.addLiteral( RDF.value, tvInt ).hasLiteral( RDF.value, tvInt ) );
        assertTrue( r.addLiteral( RDF.value, tvLong ).hasLiteral( RDF.value, tvLong ) );
        assertTrue( r.addLiteral( RDF.value, tvChar ).hasLiteral( RDF.value, tvChar ) );
        assertTrue( r.addLiteral( RDF.value, tvFloat ).hasLiteral( RDF.value, tvFloat ) );
        assertTrue( r.addLiteral( RDF.value, tvDouble ).hasLiteral( RDF.value, tvDouble ) );
        assertTrue( r.addProperty( RDF.value, tvString ).hasProperty( RDF.value, tvString ) );
        assertTrue( r.addProperty( RDF.value, tvString, lang ).hasProperty( RDF.value, tvString, lang ) );
        assertTrue( r.addLiteral( RDF.value, tvObject ).hasLiteral( RDF.value, tvObject ) );
        assertTrue( r.addProperty( RDF.value, tvLiteral ).hasProperty( RDF.value, tvLiteral ) );
        assertTrue( r.addProperty( RDF.value, tvResource ).hasProperty( RDF.value, tvResource ) );
        assertTrue( r.getRequiredProperty( RDF.value ).getSubject().equals( r ) );
    //
        Property p = m.createProperty( "foo/", "bar" );
        try {r.getRequiredProperty( p ); fail( "should detect missing property" ); }
        catch (PropertyNotFoundException e) { pass(); }
    //
        assertEquals( 13, iteratorToSet( r.listProperties( RDF.value ) ).size() );
        assertEquals( setOf( r ), iteratorToSet( r.listProperties( RDF.value ).mapWith( Statement.Util.getSubject ) ) );
    //
        assertEquals( 0, iteratorToSet( r.listProperties( p ) ).size() );
        assertEquals( new HashSet<Resource>(), iteratorToSet( r.listProperties( p ).mapWith( Statement.Util.getSubject ) ) );
    //
        assertEquals( 13 + numProps, iteratorToSet( r.listProperties() ).size() );
        assertEquals( setOf( r ), iteratorToSet( r.listProperties().mapWith( Statement.Util.getSubject ) ) );
    //
        r.removeProperties();
        assertEquals( 0, m.query( new SimpleSelector( r, null, (RDFNode) null ) ).size() );
        }
    
  public void testContainer( Model m, Container cont1, Container cont2 ) 
        {
        NodeIterator nIter;
        StmtIterator sIter;

        Literal tvLiteral = m.createLiteral( "test 12 string 2" );
//        Resource tvResObj = m.createResource( new ResTestObjF() );
        Object tvLitObj = new LitTestObj( 1234 );
        Bag tvBag = m.createBag();
        Alt tvAlt = m.createAlt();
        Seq tvSeq = m.createSeq();
        String lang = "en";
        Statement stmt;
    //    
        assertEquals( 0, cont1.size() );
        assertEquals( 0, cont2.size() );
    //
        assertTrue( cont1.add( tvBoolean ).contains( tvBoolean ) );
        assertTrue( cont1.add( tvByte ).contains( tvByte ) );
        assertTrue( cont1.add( tvShort ).contains( tvShort ) );
        assertTrue( cont1.add( tvInt ).contains( tvInt ) );
        assertTrue( cont1.add( tvLong ).contains( tvLong ) );
        assertTrue( cont1.add( tvFloat ).contains( tvFloat ) );
        assertTrue( cont1.add( tvDouble ).contains( tvDouble ) );
        assertTrue( cont1.add( tvChar ).contains( tvChar ) );
        assertTrue( cont1.add( tvString ).contains( tvString ) );
        assertFalse( cont1.contains( tvString, lang ) );
        assertTrue( cont1.add( tvString, lang ).contains( tvString, lang ) );
        assertTrue( cont1.add( tvLiteral ).contains( tvLiteral ) );
//        assertTrue( cont1.add( tvResObj ).contains( tvResObj ) );
        assertTrue( cont1.add( tvLitObj ).contains( tvLitObj ) );
        assertEquals( 12, cont1.size() );
    //
        int num = 10;
        for (int i = 0; i < num; i += 1) cont2.add( i );
        assertEquals( num, cont2.size() );
        checkNumericContent( cont2, num );
    //    
        boolean[] found = new boolean[num];
        boolean[] retain = { true, true, true, false, false, false, false, false, true, true };
        retainOnlySpecified( cont2, num, retain );
        seeWhatsThere( cont2, found );
        for (int i = 0; i < num; i += 1)
            assertEquals( i + "th element of array", retain[i], found[i] );
        }

    protected void seeWhatsThere( Container cont2, boolean[] found )
        {
        NodeIterator nit = cont2.iterator();
        while (nit.hasNext())
                {
                int v = ((Literal) nit.nextNode()).getInt();
                assertFalse( found[v] );
                found[v] = true;
                }
        }

    protected void retainOnlySpecified( Container cont2, int num, boolean[] retain )
        {
        NodeIterator nit = cont2.iterator();
        for (int i = 0; i < num; i++)
            {
            nit.nextNode();
            if (retain[i] == false) nit.remove();
            }
        assertFalse( nit.hasNext() );
        }

    protected void checkNumericContent( Container cont2, int num )
        {
        NodeIterator nit = cont2.iterator();
        for (int i = 0; i < num; i += 1)
            assertEquals( i, ((Literal) nit.nextNode()).getInt() );
        assertFalse( nit.hasNext() );
        }
    }
