/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.fuseki.conneg.AcceptList ;
import org.openjena.fuseki.conneg.MediaType ;


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
                "application/n3,application/rdf+xml;q=0.5",
                "application/n3,application/rdf+xml" , 
                "application/n3") ;
    }
    

    // Worker. New way.
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
    
    // Worker.  Does request 'header' match server 'offer' with 'result'?
    private void testMatch1(String header, String offer, String result)
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
        assertEquals("Match different", result, matchItem.getMediaType()) ;
    }
    
    private String q(Object obj)
    {
        if ( obj == null )
            return "<null>" ;
        return "'"+obj.toString()+"'" ;
    }

}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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