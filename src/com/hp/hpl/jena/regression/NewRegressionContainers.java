/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionContainers.java,v 1.2 2006-03-22 13:52:54 andy_seaborne Exp $
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
    
    public void setUp()
        { m = getModel(); }
    
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


/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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