/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionResources.java,v 1.1 2005-08-17 09:48:55 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.regression.Regression.ResTestObjF;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionResources extends ModelTestBase
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
    
    public void testCreateAnonByFactory()
        {
        Model m = getModel();
        assertTrue( m.createResource( new ResTestObjF() ).isAnon() );
        }
    
    public void testCreateResourceByFactory()
        {
        Model m = getModel();
        String uri = "http://aldabaran.hpl.hp.com/foo";
        assertEquals( uri, m.createResource( uri, new ResTestObjF() ).getURI() );
        }
    
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
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/