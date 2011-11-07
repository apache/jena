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
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionContainers extends ModelTestBase
    {
    public NewRegressionContainers( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionContainers.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    
    @Override
    public void setUp()
        { m = getModel(); }
    
    @Override
    public void tearDown()
        { m = null; }
    
    public void testCreateAnonBag()
        {
        Bag tv = m.createBag();
        assertTrue( tv.isAnon() );
        assertTrue( m.contains( tv, RDF.type, RDF.Bag ) );
        }
    
    public void testCreateNamedBag()
        {
        String uri = "http://aldabaran/foo";
        Bag tv = m.createBag( uri );
        assertEquals( uri, tv.getURI() );
        assertTrue( m.contains( tv, RDF.type, RDF.Bag ) );
        }    
    
    public void testCreateAnonAlt()
        {
        Alt tv = m.createAlt();
        assertTrue( tv.isAnon() );
        assertTrue( m.contains( tv, RDF.type, RDF.Alt ) );
        }
    
    public void testCreateNamedAlt()
        {
        String uri = "http://aldabaran/sirius";
        Alt tv = m.createAlt( uri );
        assertEquals( uri, tv.getURI() );
        assertTrue( m.contains( tv, RDF.type, RDF.Alt ) );
        } 
    
    public void testCreateAnonSeq()
        {
        Seq tv = m.createSeq();
        assertTrue( tv.isAnon() );
        assertTrue( m.contains( tv, RDF.type, RDF.Seq ) );
        }
    
    public void testCreateNamedSeq()
        {
        String uri = "http://aldabaran/andromeda";
        Seq tv = m.createSeq( uri );
        assertEquals( uri, tv.getURI() );
        assertTrue( m.contains( tv, RDF.type, RDF.Seq ) );
        }
    }
