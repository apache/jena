/*
  (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPrefixMapping.java,v 1.9 2003-08-27 13:07:30 andy_seaborne Exp $
*/

package com.hp.hpl.jena.shared.test;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

/**
    Tests PrefixMappingImpl by subclassing AbstractTestPrefixMapping, qv.
    @author kers
*/

public class TestPrefixMapping extends AbstractTestPrefixMapping
    {
    public TestPrefixMapping( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestPrefixMapping.class ); }   
    
    protected PrefixMapping getMapping()
        { return new PrefixMappingImpl(); }        
        
    public void testStandard()
        {
        PrefixMapping st = PrefixMapping.Standard;
        assertEquals( RDF.getURI(), st.getNsPrefixURI( "rdf" ) );
        assertEquals( RDFS.getURI(), st.getNsPrefixURI( "rdfs" ) );
        assertEquals( DC.getURI(), st.getNsPrefixURI( "dc" ) );
        assertEquals( OWL.NAMESPACE, st.getNsPrefixURI( "owl" ) );
        assertEquals( RSS.getURI(), st.getNsPrefixURI( "rss" ) );
        assertEquals( VCARD.getURI(), st.getNsPrefixURI( "vcard" ) );
        assertEquals( JMS.getURI(), st.getNsPrefixURI( "jms" ) );
        assertEquals( DAMLVocabulary.NAMESPACE_DAML_2001_03_URI, st.getNsPrefixURI( "daml" ) );
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