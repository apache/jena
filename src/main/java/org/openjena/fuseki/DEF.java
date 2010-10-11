/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import org.openjena.fuseki.conneg.AcceptList ;
import org.openjena.fuseki.conneg.MediaType ;
import org.openjena.riot.WebContent ;

public class DEF
{
    public static MediaType acceptRDFXML        = new MediaType(WebContent.contentTypeRDFXML) ;
    public static MediaType acceptTurtle1       = new MediaType(WebContent.contentTypeTurtle1) ;
    public static MediaType acceptTurtle2       = new MediaType(WebContent.contentTypeTurtle2) ;
    public static MediaType acceptTurtle3       = new MediaType(WebContent.contentTypeTurtle3) ;
    public static MediaType acceptNTriples      = new MediaType(WebContent.contentTypeNTriples) ;
    public static MediaType acceptTriG          = new MediaType(WebContent.contentTypeNTriples) ;
    public static MediaType acceptNQuads        = new MediaType(WebContent.contentTypeNTriples) ;
    public static MediaType charsetUTF8         = new MediaType(WebContent.charsetUTF8) ;
    
    public static MediaType acceptRSXML         = new MediaType(WebContent.contentTypeResultsXML) ;

    public static MediaType[] rdfOffer$         = { acceptRDFXML, acceptTurtle1, acceptTurtle2, acceptTurtle3, acceptNTriples } ;
    public static AcceptList  rdfOffer          = new AcceptList(rdfOffer$) ;
//    public static MediaType[] quadsOffer$       = { acceptTriG, acceptNQuads } ;
//    public static AcceptList  quadsOffer        = new AcceptList(quadsOffer$) ;
    public static AcceptList  charsetOffer      = new AcceptList(charsetUTF8) ;
    public static AcceptList  rsOffer           = new AcceptList(WebContent.contentTypeResultsXML, WebContent.contentTypeResultsJSON) ;

    // New to WebContent - remove when update happens.
}

/*
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