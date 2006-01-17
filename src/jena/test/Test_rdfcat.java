/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: Test_rdfcat.java,v 1.1 2006-01-17 09:23:54 chris-dollin Exp $
*/

package jena.test;

import junit.framework.TestCase;

public class Test_rdfcat extends TestCase
    {
    public void testAbbreviationTable()
        {
        assertEquals( "RDF/XML", jena.rdfcat.unabbreviate.get( "x" ) );
        assertEquals( "RDF/XML", jena.rdfcat.unabbreviate.get( "rdf" ) );
        assertEquals( "RDF/XML", jena.rdfcat.unabbreviate.get( "rdfxml" ) );
        assertEquals( "RDF/XML", jena.rdfcat.unabbreviate.get( "xml" ) );
        assertEquals( "N3", jena.rdfcat.unabbreviate.get( "n3" ) );
        assertEquals( "N3", jena.rdfcat.unabbreviate.get( "n" ) );
        assertEquals( "N3", jena.rdfcat.unabbreviate.get( "ttl" ) );
        assertEquals( "N-TRIPLE", jena.rdfcat.unabbreviate.get( "ntriples" ) );
        assertEquals( "N-TRIPLE", jena.rdfcat.unabbreviate.get( "ntriple" ) );
        assertEquals( "N-TRIPLE", jena.rdfcat.unabbreviate.get( "t" ) );
        assertEquals( "RDF/XML-ABBREV", jena.rdfcat.unabbreviate.get( "owl" ) );
        assertEquals( "RDF/XML-ABBREV", jena.rdfcat.unabbreviate.get( "abbrev" ) );
        }
    
    public void testExistingLanguage()
        {
        assertEquals( "RDF/XML", jena.rdfcat.getCheckedLanguage( "x" ) );
        assertEquals( "RDF/XML", jena.rdfcat.getCheckedLanguage( "xml" ) );
        assertEquals( "RDF/XML-ABBREV", jena.rdfcat.getCheckedLanguage( "owl" ) );
        assertEquals( "N3", jena.rdfcat.getCheckedLanguage( "N3" ) );
        assertEquals( "N-TRIPLE", jena.rdfcat.getCheckedLanguage( "N-TRIPLE" ) );
        }
    
    public void testNonexistantLanguage()
        {
        try 
            { jena.rdfcat.getCheckedLanguage( "noSuchLanguageAsThisOneFruitcake" ); 
            fail( "should trap non-existant language" ); }
        catch (IllegalArgumentException e)
            {
            assertTrue( "message should mention bad language", e.getMessage().indexOf( "Fruitcake" ) > 0 );
            }
        }
    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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