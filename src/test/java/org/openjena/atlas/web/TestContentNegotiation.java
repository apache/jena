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

package org.openjena.atlas.web;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

public class TestContentNegotiation extends BaseTest
{
    static final String ctFirefox = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5" ;
    static final String ctIE_6  = "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/msword, */*" ;
    
    static final String ctApplicationXML     =  "application/xml" ;
    static final String ctApplicationRDFXML  =  "application/rdf+xml" ;
    static final String ctApplicationStar    =  "application/*" ;
    // Legal?? */xml
    
    static final String ctTextPlain          =  "text/plain" ;
    static final String ctTextXML            =  "text/xml" ;
    static final String ctTextStar           =  "text/*" ;
    
    static final String ctStarStar           = "*/*" ;
    
    @Test public void simpleNeg1()
    { testMatch("text/plain", "text/plain", "text/plain") ; }
    
    @Test public void simpleNeg2()
    { testMatch("application/xml", "text/plain", null) ; }
    
    @Test public void simpleNeg3()
    { testMatch("text/*", "text/*", "text/*") ; }
    
    @Test public void simpleNeg4()
    { testMatch("text/xml", "text/*", "text/xml") ; }
    
    @Test public void simpleNeg5()
    { testMatch("text/*", "text/xml", "text/xml") ; }
    
    @Test public void listItemNeg1()
    { testMatch("text/xml,text/*", "text/*", "text/xml") ; }
    
    @Test public void listListNeg1()
    { testMatch("text/xml,text/*", "text/plain,text/*", "text/plain") ; }
    
    @Test public void listListNeg2()
    { testMatch("text/xml,text/*", "text/*,text/plain", "text/xml") ; }
    
    @Test public void qualNeg1() { testMatch("text/xml;q=0.5,text/plain", "text/*", "text/plain") ; }
    
    @Test public void qualNeg2()
    {
        testMatch(
                "application/n3,application/rdf+xml;q=0.5",
                "application/rdf+xml,application/n3" , 
                "application/n3") ;
    }
    
    @Test public void qualNeg3()
    {
        testMatch(
                "application/rdf+xml;q=0.5 , application/n3",
                "application/n3,application/rdf+xml" , 
                "application/n3") ;
    }
    
    @Test public void qualNeg4()
    {
        testMatch(
                "application/rdf+xml;q=0.5 , application/n3",
                "application/rdf+xml , application/n3" , 
                "application/n3") ;
    }

    // SPARQL: result set
    @Test public void qualNeg5()
    {
        testMatch(
                "application/sparql-results+json , application/sparql-results+xml;q=0.9 , application/rdf+xml , application/turtle;q=0.9 , */*;q=0.1",
                "application/sparql-results+xml, application/sparql-results+json, text/csv , text/tab-separated-values, text/plain",
                "application/sparql-results+json") ;
    }
    
    // SPARQL: result set
    @Test public void qualNeg5a()
    {
        testMatch(
                "application/sparql-results+json , application/sparql-results+xml;q=0.9 , application/rdf+xml , application/turtle;q=0.9 , */*;q=0.1",
                "application/sparql-results+json, application/sparql-results+xml, text/csv , text/tab-separated-values, text/plain",
                "application/sparql-results+json") ;
    }
    
    // SPARQL: RDF
    @Test public void qualNeg6()
    {
        testMatch(
                "application/sparql-results+json , application/sparql-results+xml;q=0.9 , application/rdf+xml , application/turtle;q=0.9 , */*;q=0.1",
                "application/rdf+xml , application/turtle , application/x-turtle ,  text/turtle , text/plain  application/n-triples",
                "application/rdf+xml") ;
    }
    
    // HTTP: RDF
    @Test public void qualNeg7()
    {
        testMatch(
                "application/rdf+xml , application/turtle;q=0.9 , */*;q=0.1",
                "application/rdf+xml , application/turtle , application/x-turtle ,  text/turtle , text/plain  application/n-triples",
                "application/rdf+xml") ;
    }
    
    // HTTP: RDF
    @Test public void qualNeg8()
    {
        testMatch(
                "application/turtle;q=0.9 , application/rdf+xml , */*;q=0.1",
                "application/rdf+xml , application/turtle , application/x-turtle ,  text/turtle , text/plain  application/n-triples",
                "application/rdf+xml") ;
    }
    
    // TODO Standard headers from clients of RDf and for SPARQL results
    
    // RDF:
    //  Accept: application/rdf+xml , application/turtle;q=0.9 , */*;q=0.1
    //  Offer: application/rdf+xml , application/turtle , application/x-turtle ,  text/turtle , text/plain  application/n-triples
    
    // SPARQL:
    //  Accept: application/sparql-results+json , application/sparql-results+xml;q=0.9 , application/rdf+xml , application/turtle;q=0.9 , */*;q=0.1
    //  Offer:  application/sparql-results+xml, application/sparql-results+json, text/csv , text/tab-separated-values, text/plain
    
    private void testMatch(String header, String offer, String result)
    {
        AcceptList list1 = new AcceptList(header) ;
        AcceptList list2 = new AcceptList(offer) ;
        MediaType matchItem = AcceptList.match(list1, list2) ;

        if ( result == null )
        {
            assertNull("Match not null: from "+q(header)+" :: "+q(offer),
                       matchItem) ;
            return ;
        }
        assertNotNull("Match is null: expected "+q(result), matchItem) ;
        assertEquals("Match different", result, matchItem.toHeaderString()) ;
    }
    
    private String q(Object obj)
    {
        if ( obj == null )
            return "<null>" ;
        return "'"+obj.toString()+"'" ;
    }

}
