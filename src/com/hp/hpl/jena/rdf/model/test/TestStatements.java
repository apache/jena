/*
	(c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
	[see end of file]
	$Id: TestStatements.java,v 1.1 2003-03-26 11:46:49 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.GraphTestBase;
import junit.framework.*;

public class TestStatements extends GraphTestBase
    {
    public TestStatements( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestStatements.class ); }   
        
    /**
        this case came up when Chris was sorting out ReifedStatement and
        had mishacked Model.createStatement. A resource created in one
        model and incorporated into a statement asserted constructed by a
        different model should test equal to the resource extracted from that
        statement, even if it's a bnode.
    */
    public void testStuff()
        {
        Model red = ModelFactory.createDefaultModel();
        Model blue = ModelFactory.createDefaultModel();
        Resource r = red.createResource();
        Property p = red.createProperty( "" );
        Statement s = blue.createStatement( r, p, r );
        assertEquals( "subject preserved", r, s.getSubject() );
        assertEquals( "object preserved", r, s.getObject() );
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


