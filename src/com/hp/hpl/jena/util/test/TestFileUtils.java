/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestFileUtils.java,v 1.1 2004-03-19 13:32:54 chris-dollin Exp $
*/

package com.hp.hpl.jena.util.test;

import com.hp.hpl.jena.util.FileUtils;

import junit.framework.*;

/**
 TestFileUtils

 @author kers
*/
public class TestFileUtils extends TestCase 
    {
	public TestFileUtils(String name) 
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestFileUtils.class ); }

    public void testLangXML()
        { assertEquals( "RDF/XML", FileUtils.langXML ); }
    
    public void testLangXMLAbbrev()
        { assertEquals( "RDF/XML-ABBREV", FileUtils.langXMLAbbrev ); }
    
    public void testLangNTriple()
        { assertEquals( "N-TRIPLE", FileUtils.langNTriple ); }
    
    public void testLangN3()
        { assertEquals( "N3", FileUtils.langN3 ); }
    
    public void testGuessLangLowerCase()
        {
        assertEquals( FileUtils.langN3, FileUtils.guessLang( "simple.n3") );
        assertEquals( FileUtils.langN3, FileUtils.guessLang( "hello.there.n3") );
        assertEquals( FileUtils.langNTriple, FileUtils.guessLang( "simple.nt" ) );
        assertEquals( FileUtils.langNTriple, FileUtils.guessLang( "whats.up.nt" ) );
        assertEquals( FileUtils.langXML, FileUtils.guessLang( "poggle.rdf") );
        assertEquals( FileUtils.langXML, FileUtils.guessLang( "wise.owl" ) );
        assertEquals( FileUtils.langXML, FileUtils.guessLang( "dotless" ) );
        }
    
    public void testGuessLangMixedCase()
        {
        assertEquals( FileUtils.langN3, FileUtils.guessLang( "simple.N3") );
        assertEquals( FileUtils.langN3, FileUtils.guessLang( "hello.there.N3") );
        assertEquals( FileUtils.langNTriple, FileUtils.guessLang( "simple.NT" ) );
        assertEquals( FileUtils.langNTriple, FileUtils.guessLang( "whats.up.Nt" ) );
        assertEquals( FileUtils.langXML, FileUtils.guessLang( "poggle.rDf") );
        assertEquals( FileUtils.langXML, FileUtils.guessLang( "wise.OwL" ) );
        assertEquals( FileUtils.langXML, FileUtils.guessLang( "dotless" ) );
        }
    
    public void testGuessLangFallback()
        {
        assertEquals( "spoo", FileUtils.guessLang( "noSuffix", "spoo" ) );
        assertEquals( "pots", FileUtils.guessLang( "suffix.unknown", "pots" ) );
        assertEquals( FileUtils.langXML, FileUtils.guessLang( "rdf.rdf", "spoo" ) );
        assertEquals( FileUtils.langXML, FileUtils.guessLang( "rdf.owl", "spoo" ) );
        }
    
    public void testMisplacedDots()
	    {
        assertEquals( "spoo", FileUtils.guessLang( "stuff.left/right", "spoo" ) );
        assertEquals( "spoo", FileUtils.guessLang( "stuff.left\\right", "spoo" ) );
        }  
    }

/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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