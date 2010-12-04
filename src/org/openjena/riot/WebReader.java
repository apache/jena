/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import static org.openjena.riot.WebContent.* ;
import java.util.HashMap ;
import java.util.Map ;

/** Retrieve data from the web */
public class WebReader
{
    interface SinkTriplesFactory {} 

    /** Content type to sink factory for triples */ 
    static Map<String, SinkTriplesFactory> readerTriplesMap = new HashMap<String, SinkTriplesFactory>() ;
    static {
        readerTriplesMap.put(contentTypeN3, null) ;
        readerTriplesMap.put(contentTypeN3Alt1, null) ;
        readerTriplesMap.put(contentTypeN3Alt2, null) ;

        readerTriplesMap.put(contentTypeTurtle1, null) ;
        readerTriplesMap.put(contentTypeTurtle2, null) ;
        readerTriplesMap.put(contentTypeTurtle3, null) ;

        readerTriplesMap.put(contentTypeRDFXML, null) ;
    }

    interface SinkQuadsFactory {} 

    /** Content type to sink factory for quads */ 
    static Map<String, SinkQuadsFactory> readerQuadsMap = new HashMap<String, SinkQuadsFactory>() ;

    static {
        readerQuadsMap.put(contentTypeNTriples, null) ;
        readerQuadsMap.put(contentTypeNTriplesAlt, null) ;

        readerQuadsMap.put(contentTypeTriG, null) ;
        readerQuadsMap.put(contentTypeTriGAlt, null) ;

        readerQuadsMap.put(contentTypeNQuads, null) ;
        readerQuadsMap.put(contentTypeNQuadsAlt, null) ;
    }

    //public static content
    
    // Lang.guess 
    
    // open(Name) -> types stream.
    //   files by ext
    //   c.f FileManager
    
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