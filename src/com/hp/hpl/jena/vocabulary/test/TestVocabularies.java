/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestVocabularies.java,v 1.6 2004-12-06 13:50:31 andy_seaborne Exp $
*/

package com.hp.hpl.jena.vocabulary.test;

import junit.framework.*;

/**
    Collect together all the vocabulary tests.
 	@author kers
*/
public class TestVocabularies extends TestCase
    {
    public TestVocabularies( String name )
        { super( name ); }

    public static TestSuite suite()
        {
        TestSuite result = new TestSuite();
        result.addTest( TestVocabRDF.suite() );
        result.addTest( TestVocabRDFS.suite() );
        result.addTest( TestVocabVCARD.suite() );
        result.addTest( TestVocabTestQuery.suite() );
        result.addTest( TestVocabTestManifest.suite() );
        result.addTest( TestVocabDB.suite() );
        result.addTest( TestVocabRSS.suite() );
        result.addTest( TestVocabResultSet.suite() );
        result.addTest( TestVocabDC10.suite() );
        return result;
        }
    }


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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