/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestHiddenStatements.java,v 1.2 2003-08-27 13:05:52 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.shared.*;
import junit.framework.*;

/**
 	@author kers
*/
public class TestHiddenStatements extends ModelTestBase
    {
    public TestHiddenStatements(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestHiddenStatements.class ); }
    
    public void assertSameMapping( PrefixMapping L, PrefixMapping R )
        {
        if (sameMapping( L, R ) == false)
            fail( "wanted " + L + " but got " + R );
        }
        
    public boolean sameMapping( PrefixMapping L, PrefixMapping R )
        {
//        System.err.println( ">> " + L.getNsPrefixMap() );
//        System.err.println( ">> " + R.getNsPrefixMap() );
        return L.getNsPrefixMap().equals( R.getNsPrefixMap() );
        }
        
    /**
        Test that withHiddenStatements copies the prefix mapping
        TODO add some extra prefixs for checking; should check for non-
        default models. 
    */
    public void testPrefixCopied()
        {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefixes( PrefixMapping.Standard );
        assertSameMapping( PrefixMapping.Standard, ModelReifier.withHiddenStatements( m ) );
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