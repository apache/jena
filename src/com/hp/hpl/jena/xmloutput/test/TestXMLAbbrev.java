/*
    (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
    All rights reserved. [See end of file]
    $Id: TestXMLAbbrev.java,v 1.4 2008-01-02 12:06:48 andy_seaborne Exp $
*/

package com.hp.hpl.jena.xmloutput.test;

import java.io.IOException;

/**
    The tests testReaderAndWriter includes for the language 
    "RDF/XML-ABBREV". Extracted from TestXMLFeatures as part 
    of a clarification exercise intended to extend the testing to
    allow for the entity-including option.
    
    @author eh
*/
public class TestXMLAbbrev extends XMLOutputTestBase
    {
    public TestXMLAbbrev( String name )
        { super( name, "RDF/XML-ABBREV" ); }
    
    public void testNoPropAttr() throws IOException 
        {
        check
            (
            TestXMLFeatures.file1,
            null,
            "prop1=",
            Change.blockRules( "propertyAttr" )
            );
        }    
    
    public void testNoDamlCollection() throws IOException 
        {
        check
            (
            "testing/abbreviated/daml.rdf",
            null,
            "[\"']daml:collection[\"']",
            Change.blockRules( "daml:collection" )
            );
        }

    public void testNoRdfCollection() throws IOException 
        {
        check
            (
            "testing/abbreviated/collection.rdf",
            null,
            "[\"']Collection[\"']",
            Change.blockRules( "parseTypeCollectionPropertyElt" )
            );
        }
    
    public void testNoLi() throws IOException 
        {
        check
            (
            "testing/abbreviated/container.rdf",
            null,
            "rdf:li",
            Change.blockRules( "section-List-Expand" )
            );
        }
    
    public void testNoID() throws IOException 
        {
        check
            (
            "testing/abbreviated/container.rdf",
            "rdf:ID",
            Change.blockRules( "idAttr" ),
            "http://example.org/foo"
            );
        }

    public void testNoID2() throws IOException 
        {
        check
            (
            "testing/abbreviated/container.rdf",
            "rdf:ID",
            Change.blockRules( "idAttr" ),
            "http://example.org/foo#"
            );
        }

    public void testNoResource() throws IOException 
        {
        check
            (
            "testing/abbreviated/container.rdf",
            "['\"]Resource[\"']",
            Change.blockRules( "parseTypeResourcePropertyElt" ),
            "http://example.org/foo#"
            );
        }
    
    public void testPropAttrs() throws IOException 
        {
        check
            (
            "testing/abbreviated/namespaces.rdf",
            ":prop0 *=",
            null,
            Change.blockRules( ""  )
            );
        }
    
    public void testNoPropAttrs() throws IOException 
        {
        check
            (
            "testing/abbreviated/namespaces.rdf",
            null,
            ":prop0 *=",
            Change.none() 
            );
        }

    public void testNoReification() throws IOException 
        {
         // System.err.println("WARNING: reification output tests suppressed.");
         String filename = "testing/abbreviated/reification.rdf";
         String base = "http://example.org/foo";
         /* * Heisenbug, reification prettiness sometimes fails. * /
         check(filename,null,null,"rdf:subject",false,new Change(){
                    public void code(RDFWriter w){}
                },base);
        /* */
        check  
            (
            filename, 
            null, 
            "rdf:subject",
            null,  
            false,
            Change.blockRules( "section-Reification" ), 
            base
            );
        }


    public void testNoCookUp() throws IOException 
        {
        check
            (
            "testing/abbreviated/cookup.rdf",
            null,
            "j.cook.up",
            Change.blockRules( "" )
            );
        }
    }

/*
 *  (c)   Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *    All rights reserved.
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
 *
 */