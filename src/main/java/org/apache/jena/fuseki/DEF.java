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

package org.apache.jena.fuseki;

import org.apache.jena.fuseki.conneg.AcceptList ;
import org.openjena.atlas.web.MediaType ;
import org.openjena.riot.WebContent ;

public class DEF
{
    public static MediaType acceptRDFXML        = new MediaType(WebContent.contentTypeRDFXML) ;
    public static MediaType acceptTurtle1       = new MediaType(WebContent.contentTypeTurtle) ;
    public static MediaType acceptTurtle2       = new MediaType(WebContent.contentTypeTurtleAlt1) ;
    public static MediaType acceptTurtle3       = new MediaType(WebContent.contentTypeTurtleAlt2) ;
    public static MediaType acceptNTriples      = new MediaType(WebContent.contentTypeNTriples) ;
    public static MediaType acceptNTriplesAlt   = new MediaType(WebContent.contentTypeNTriplesAlt) ;
    public static MediaType acceptTriG          = new MediaType(WebContent.contentTypeTriG) ;
    public static MediaType acceptTriGAlt       = new MediaType(WebContent.contentTypeTriGAlt) ;
    public static MediaType acceptNQuads        = new MediaType(WebContent.contentTypeNQuads) ;
    public static MediaType acceptNQuadsAlt     = new MediaType(WebContent.contentTypeNQuadsAlt) ;
    public static MediaType charsetUTF8         = new MediaType(WebContent.charsetUTF8) ;
    
    public static MediaType acceptRSXML         = new MediaType(WebContent.contentTypeResultsXML) ;

    public static AcceptList rdfOffer           = new AcceptList(acceptRDFXML, acceptTurtle1, acceptTurtle2, acceptTurtle3, acceptNTriples, acceptNTriplesAlt) ;
    public static AcceptList quadsOffer         = new AcceptList(acceptTriG, acceptTriGAlt, acceptNQuads, acceptNQuadsAlt) ;
    public static AcceptList charsetOffer       = new AcceptList(charsetUTF8) ;
    
    public static AcceptList rsOffer            = new AcceptList(WebContent.contentTypeResultsXML, 
                                                                 WebContent.contentTypeResultsJSON,
                                                                 WebContent.contentTypeTextCSV,
                                                                 WebContent.contentTypeTextTSV,
                                                                 WebContent.contentTypeTextPlain) ;
    // New to WebContent - remove when update happens.
}
