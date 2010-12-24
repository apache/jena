/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;


import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.riot.ErrorHandlerTestLib.ExError ;
import org.openjena.riot.ErrorHandlerTestLib.ExWarning ;
import org.openjena.riot.system.Checker ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.impl.JenaParameters ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

public class TestChecker
{
    static Checker checker = new Checker(new ErrorHandlerTestLib.ErrorHandlerEx()) ;
    
    boolean b ;

    @Before
    public void before()
    {
        b = JenaParameters.enableWhitespaceCheckingOfTypedLiterals ;
        // The default is false which allows whitespace around integers.
        // Jena seems to allow white space around dateTimes either way. 
        // JenaParameters.enableWhitespaceCheckingOfTypedLiterals = true ;
    }

    @After
    public void after()
    {
        JenaParameters.enableWhitespaceCheckingOfTypedLiterals = b ;
    }
    
    @Test public void checker01() { check("''") ; }
    @Test public void checker02() { check("''@en") ; }
    @Test public void checker03() { check("<http://example/x>") ; }

    @Test(expected=ExError.class)
    public void checker04() { check("<x>") ; }

    // CheckerIRI specifically does not complain about these sorts of illegal URNs
    // although they are wrong (URNs must be "urn:2+chars:1+chars")
    
    @Test //(expected=ExWarning.class)
    public void checker05() { check("<urn:abc>") ; }

    @Test //(expected=ExWarning.class)
    public void checker06() { check("<urn:abc:>") ; }
    
    @Test
    public void checker07() { check("<urn:abc:y>") ; }

    @Test (expected=ExWarning.class) 
    public void checker10() { check("''^^xsd:dateTime") ; }

    // Whitespace facet processing.  
    // Strictly illegal RDF but Jena accepts them.
    // See JenaParameters.enableWhitespaceCheckingOfTypedLiterals
    
    @Test public void checker11() { check("'  2010-05-19T01:01:01.01+01:00'^^xsd:dateTime") ; }
    @Test public void checker12() { check("'\\n2010-05-19T01:01:01.01+01:00\\t\\r  '^^xsd:dateTime") ; }
    @Test public void checker13() { check("' 123'^^xsd:integer") ; }
    
    // Internal white space - illegal
    @Test (expected=ExWarning.class) public void checker14() { check("'12 3'^^xsd:integer") ; }
    @Test public void checker15() { check("'\\n123'^^xsd:integer") ; }

    // Test all the data type hierarchies that whitespace foo affects.
    @Test public void checker16() { check("'123.0  '^^xsd:float") ; }
    @Test public void checker17() { check("'123.0\\n'^^xsd:double") ; }
    // Jena "bug"
    //@Test(expected=ExWarning.class) public void checker18() { check("'\\b123.0\\n'^^xsd:double") ; }

    
    // Other bad lexical forms.
    @Test(expected=ExWarning.class) public void checker20() { check("'XYZ'^^xsd:integer") ; }
    // Lang tag
    @Test(expected=ExWarning.class) public void checker21() { check("'XYZ'@abcdefghijklmn") ; }
    
    
    @Test(expected=ExWarning.class) public void checker30() { check("<http://base/[]iri>") ; }
    
    //Bad IRI

    
    //@Test public void checker12() { check("''@en") ; }
    
    // XML Literals.
    
    @Test
    public void checker40() { check("\"<x></x>\"^^rdf:XMLLiteral") ; }

    @Test(expected=ExWarning.class)
    // Unmatched tag
    public void checker41() { check("\"<x>\"^^rdf:XMLLiteral") ; }
    
    @Test(expected=ExWarning.class)
    // Bad tagging.
    public void checker42() { check("\"<x><y></x></y>\"^^rdf:XMLLiteral") ; }

    @Test(expected=ExWarning.class)
    // Not exclusive canonicalization
    public void checker43() { check("\"<x/>\"^^rdf:XMLLiteral") ; }
    
    @Test
    public void checker44() { check("'''<x xmlns=\"http://example/ns#\" attr=\"foo\"></x>'''^^rdf:XMLLiteral" ) ; }
    
    @Test(expected=ExWarning.class)
    // Exclusive canonicalization requires namespace declaration before attributes
    public void checker45() { check("'''<x attr=\"foo\" xmlns=\"http://example/ns#\"></x>'''^^rdf:XMLLiteral") ; }
    

    private static void check(String string)
    {
        Node n = NodeFactory.parseNode(string) ;
        checker.check(n, -1, -1) ;
    }
    
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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