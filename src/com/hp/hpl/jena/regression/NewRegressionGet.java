/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionGet.java,v 1.1 2005-08-17 14:02:16 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.regression.Regression.ResTestObjF;
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
    
    public void setUp()
        { 
        m = getModel();
        S = m.createResource( "http://nowhere.man/subject" ); 
        P = m.createProperty( "http://nowhere.man/predicate" ); 
        }
    
    public void tearDown()
        { m = null; S = null; P = null; }
    
    public void testGetResource()
        {
        String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 110;
        Resource r = m.getResource( uri );
        assertEquals( uri, r.getURI() );
        }

    public void testGetResourceFactory()
        {
        String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 120;
        Resource r = m.getResource( uri, new ResTestObjF() );
        assertEquals( uri, r.getURI() );
        }

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