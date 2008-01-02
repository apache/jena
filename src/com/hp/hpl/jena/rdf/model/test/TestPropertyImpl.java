/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestPropertyImpl.java,v 1.2 2008-01-02 12:04:41 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.*;

public class TestPropertyImpl extends ModelTestBase
    {
    public TestPropertyImpl( String name )
        { super( name ); }
    
    public void testOrdinalValues() 
        {
        testRDFOrdinalValue( 1, "_1" );
        testRDFOrdinalValue( 2, "_2" );
        testRDFOrdinalValue( 3, "_3" );
        testRDFOrdinalValue( 4, "_4" );
        testRDFOrdinalValue( 5, "_5" );
        testRDFOrdinalValue( 6, "_6" );
        testRDFOrdinalValue( 7, "_7" );
        testRDFOrdinalValue( 8, "_8" );
        testRDFOrdinalValue( 9, "_9" );
        testRDFOrdinalValue( 10, "_10" );
        testRDFOrdinalValue( 100, "_100" );
        testRDFOrdinalValue( 1234, "_1234" );
        testRDFOrdinalValue( 67890, "_67890" );
        }

    public void testNonOrdinalRDFURIs()
        {
        testRDFOrdinalValue( 0, "x" );
        testRDFOrdinalValue( 0, "x1" );
        testRDFOrdinalValue( 0, "_x" );
        testRDFOrdinalValue( 0, "x123" );
        testRDFOrdinalValue( 0, "0xff" );
        testRDFOrdinalValue( 0, "_xff" );
        }
    
    private void testRDFOrdinalValue( int i, String local )
        { testOrdinalValue( i, RDF.getURI() + local ); }

    public void testNonRDFElementURIsHaveOrdinal0()
        {
        testOrdinalValue( 0, "foo:bar" );
        testOrdinalValue( 0, "foo:bar1" );
        testOrdinalValue( 0, "foo:bar2" );
        testOrdinalValue( 0, RDFS.getURI() + "_17" );
        }

    private void testOrdinalValue( int i, String URI )
        {
        String message = "property should have expected ordinal value for " + URI;
        assertEquals( message, i, createProperty( URI ).getOrdinal() );
        }

    protected Property createProperty( String uri )
        { return new PropertyImpl( uri ); }
    }

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
