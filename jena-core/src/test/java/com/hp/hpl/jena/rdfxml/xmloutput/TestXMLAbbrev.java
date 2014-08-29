/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdfxml.xmloutput;

import java.io.IOException;

/**
    The tests testReaderAndWriter includes for the language 
    "RDF/XML-ABBREV". Extracted from TestXMLFeatures as part 
    of a clarification exercise intended to extend the testing to
    allow for the entity-including option.
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
