/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestReificationStyle.java,v 1.1 2003-09-08 13:44:31 chris-dollin Exp $
*/

package com.hp.hpl.jena.shared.test;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.rdf.model.test.*;

import junit.framework.*;

/**
	TestReificationStyle: test that ReificationStyle sets its fields correctly from its
    constructor arguments, and that the defined constants have the correct fields.

	@author kers
*/
public class TestReificationStyle extends ModelTestBase 
    {
    public TestReificationStyle( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestReificationStyle.class ); }
    
    public void testConstructorIntercepts()
        { assertEquals( true, new ReificationStyle( true, false ).intercepts() );
        assertEquals( false, new ReificationStyle( false, false ).intercepts() );   
        assertEquals( true, new ReificationStyle( true, true ).intercepts() );
        assertEquals( false, new ReificationStyle( false, true ).intercepts() ); }

    public void testConstructorConceals()
        { assertEquals( false, new ReificationStyle( true, false ).conceals() );
        assertEquals( false, new ReificationStyle( false, false ).conceals() );   
        assertEquals( true, new ReificationStyle( true, true ).conceals() );
        assertEquals( true, new ReificationStyle( false, true ).conceals() ); }
        
    public void testConstants()
        { assertEquals( false, ReificationStyle.Minimal.intercepts() );
        assertEquals( true, ReificationStyle.Minimal.conceals() );
        assertEquals( true, ReificationStyle.Standard.intercepts() );
        assertEquals( false, ReificationStyle.Standard.conceals() );
        assertEquals( true, ReificationStyle.Convenient.intercepts() );
        assertEquals( true, ReificationStyle.Convenient.conceals() ); }
    }

/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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