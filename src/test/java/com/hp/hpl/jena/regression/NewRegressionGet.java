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

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
//import com.hp.hpl.jena.regression.Regression.ResTestObjF;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionGet extends ModelTestBase
    {
    public NewRegressionGet( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionGet.class ); }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    protected Resource S;
    protected Property P;
    
    @Override
    public void setUp()
        { 
        m = getModel();
        S = m.createResource( "http://nowhere.man/subject" ); 
        P = m.createProperty( "http://nowhere.man/predicate" ); 
        }
    
    @Override
    public void tearDown()
        { m = null; S = null; P = null; }
    
    public void testGetResource()
        {
        String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 110;
        Resource r = m.getResource( uri );
        assertEquals( uri, r.getURI() );
        }

//    public void testGetResourceFactory()
//        {
//        String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 120;
//        Resource r = m.getResource( uri, new ResTestObjF() );
//        assertEquals( uri, r.getURI() );
//        }

    public void testGetPropertyOneArg()
        {
        String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 130;
        Property p = m.getProperty( uri );
        assertEquals( uri, p.getURI() );
        }

    public void testGetPropertyTwoArgs()
        {
        String ns = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 140 + "/";
        Property p = m.getProperty( ns, "foo" );
        assertEquals( ns + "foo", p.getURI() );
        }
    
    public void testGetBag()
        {
        String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 150;
        m.createBag( uri );
        Bag b = m.getBag( uri );
        assertEquals( uri, b.getURI() );
        assertTrue( m.contains( b, RDF.type, RDF.Bag ) );
        }  
    
    public void testGetAlt()
        {
        String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 160;
        m.createAlt( uri );
        Alt a = m.getAlt( uri );
        assertEquals( uri, a.getURI() );
        assertTrue( m.contains( a, RDF.type, RDF.Alt ) );
        }    
    
    public void testGetSeq()
        {
        String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 170;
        m.createSeq( uri );
        Seq s = m.getSeq( uri );
        assertEquals( uri, s.getURI() );
        assertTrue( m.contains( s, RDF.type, RDF.Seq ) );
        }
    }
