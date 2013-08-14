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

package org.apache.jena.atlas.web;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaType ;
import org.junit.Test ;

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
    { testMatch("text/*", "text/plain", "text/plain") ; }
    
    @Test public void listNeg1()
    { testMatch("text/xml,text/*", "text/xml", "text/xml") ; }
    
    @Test public void listNeg2()
    { testMatch("text/xml,text/*", "text/plain,text/xml", "text/xml") ; }
    
    @Test public void listNeg3()
    { testMatch("text/xml,text/*", "text/plain", "text/plain") ; }

    @Test public void qualNeg1() { 
        testMatch("text/xml;q=0.5,text/plain",
                  "text/plain",
                  "text/plain") ; }
    
    @Test public void qualNeg2() {
        testMatch(
                "text/turtle,application/rdf+xml;q=0.5",
                "application/rdf+xml,text/turtle" , 
                "text/turtle") ;
    }
    
    @Test public void qualNeg3() {
        testMatch(
                "text/turtle,application/rdf+xml;q=0.5",
                "text/turtle,application/rdf+xml" , 
                "text/turtle") ;
    }
    
    @Test public void qualNeg4()    
    {
        testMatch(
                  "application/rdf+xml;q=0.5,text/turtle",
                  "text/turtle,application/rdf+xml" , 
                  "text/turtle") ;
    }
    
    @Test public void qualNeg5()    
    {
        testMatch(
                  "application/rdf+xml;q=0.5,text/turtle",
                  ",application/rdf+xml,text/turtle" , 
                  "text/turtle") ;
    }

    // Content negotiations Jena/Fuseki tend to use.
    // See DEF.rsOffer and DEF.rdfOffer in Fuseki.
    // See WebContent.defaultGraphAcceptHeader, defaultDatasetAcceptHeader, defaultRDFAcceptHeader
    
    private static final String offerResultSet = "application/sparql-results+xml, application/sparql-results+json, text/csv , text/tab-separated-values, text/plain" ;
    private static final String offerRDF = "text/turtle, application/turtle, application/x-turtle,  application/n-triples, text/plain, application/rdf+xml, application/rdf+json" ;
    
    // SPARQL: result set
    @Test public void connegResultSet_01()
    {
        testMatch(
                "application/sparql-results+json , application/sparql-results+xml;q=0.9 , application/rdf+xml , application/turtle;q=0.9 , */*;q=0.1",
                offerResultSet,
                "application/sparql-results+json") ;
    }
    
    @Test public void connegResultSet_02()
    {
        testMatch(
                "application/sparql-results+xml;q=0.9, */*;q=0.1",
                offerResultSet,
                "application/sparql-results+xml") ;
    }
    
//    conneg("application/sparql-results+xml;q=0.9, */*;q=0.1", DEF.rsOffer) ;
//    conneg("application/sparql-results+json;q=0.9, */*;q=0.1", DEF.rsOffer) ;
    
    @Test public void connegResultSet_03()
    {
        testMatch(
                "application/sparql-results+json;q=0.9, */*;q=0.1",
                offerResultSet,
                "application/sparql-results+json") ;
    }

    // SPARQL - all
    @Test public void conneg_01()
    {
        testMatch(
                  // SPARQL -- ask for either.
                "application/sparql-results+json , application/sparql-results+xml;q=0.9 , text/turtle, application/rdf+xml;q=0.9 , */*;q=0.1",
                offerRDF,
                "text/turtle") ;
    }
    

    @Test public void connegRDF_01()
    {
        testMatch(
                "application/rdf+xml , text/turtle;q=0.9 , */*;q=0.1",
                offerRDF, 
                "application/rdf+xml") ;
    }
    
    @Test public void connegRDF_02()
    {
        testMatch(
                "application/turtle;q=0.9 , application/rdf+xml , */*;q=0.1",
                offerRDF,
                "application/rdf+xml") ;
    }
    
    // HTTP: RDF
    //See WebContent.defaultGraphAcceptHeader, defaultDatasetAcceptHeader, defaultRDFAcceptHeader
    
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
