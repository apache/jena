/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestVocabDC10.java,v 1.2 2003-08-27 13:08:11 andy_seaborne Exp $
*/

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

/**
     @author kers
*/
public class TestVocabDC10 extends VocabTestBase
    {
    public TestVocabDC10(String name)
    	{ super(name); }

	public static TestSuite suite()
		{ return new TestSuite( TestVocabDC10.class ); }

	public void testDC10()
		{
		String ns = "http://purl.org/dc/elements/1.0/";
        assertProperty( ns + "contributor", DC_10.contributor );
        assertProperty( ns + "coverage", DC_10.coverage );
        assertProperty( ns + "creator", DC_10.creator );
        assertProperty( ns + "date", DC_10.date );
        assertProperty( ns + "description", DC_10.description );
        assertProperty( ns + "format", DC_10.format );
        assertProperty( ns + "identifier", DC_10.identifier );
        assertProperty( ns + "language", DC_10.language );
        assertProperty( ns + "publisher", DC_10.publisher );
        assertProperty( ns + "relation", DC_10.relation );
        assertProperty( ns + "rights", DC_10.rights );
        assertProperty( ns + "source", DC_10.source );
        assertProperty( ns + "subject", DC_10.subject );
        assertProperty( ns + "title", DC_10.title );
        assertProperty( ns + "type", DC_10.type );
		}
	}


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
