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

import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.riot.WebContent ;

public class DEF
{
    public static final MediaType acceptRDFXML        = MediaType.create(WebContent.contentTypeRDFXML) ;
    public static final MediaType acceptTurtle1       = MediaType.create(WebContent.contentTypeTurtle) ;
    public static final MediaType acceptTurtle2       = MediaType.create(WebContent.contentTypeTurtleAlt1) ;
    public static final MediaType acceptTurtle3       = MediaType.create(WebContent.contentTypeTurtleAlt2) ;
    public static final MediaType acceptNTriples      = MediaType.create(WebContent.contentTypeNTriples) ;
    public static final MediaType acceptNTriplesAlt   = MediaType.create(WebContent.contentTypeNTriplesAlt) ;
    public static final MediaType acceptTriG          = MediaType.create(WebContent.contentTypeTriG) ;
    public static final MediaType acceptTriGAlt1      = MediaType.create(WebContent.contentTypeTriGAlt1) ;
    public static final MediaType acceptTriGAlt2      = MediaType.create(WebContent.contentTypeTriGAlt2) ;
    public static final MediaType acceptRDFJSON       = MediaType.create(WebContent.contentTypeRDFJSON) ; 
    public static final MediaType acceptNQuads        = MediaType.create(WebContent.contentTypeNQuads) ;
    public static final MediaType acceptNQuadsAlt1    = MediaType.create(WebContent.contentTypeNQuadsAlt1) ;
    public static final MediaType acceptNQuadsAlt2    = MediaType.create(WebContent.contentTypeNQuadsAlt2) ;
   
    public static final MediaType acceptRSXML         = MediaType.create(WebContent.contentTypeResultsXML) ;

    public static final AcceptList rdfOffer           = AcceptList.create(acceptTurtle1, acceptTurtle2, acceptTurtle3, 
                                                                          acceptNTriples, acceptNTriplesAlt,
                                                                          acceptRDFXML, 
                                                                          acceptRDFJSON) ;
    
    public static final AcceptList quadsOffer         = AcceptList.create(acceptTriG, acceptTriGAlt1, acceptTriGAlt2,
                                                                          acceptNQuads, acceptNQuadsAlt1, acceptNQuadsAlt2 
                                                                          ) ;
    
    public static final AcceptList rsOffer            = AcceptList.create(WebContent.contentTypeResultsJSON,
                                                                          WebContent.contentTypeTextCSV,
                                                                          WebContent.contentTypeTextTSV,
                                                                          WebContent.contentTypeResultsXML,
                                                                          WebContent.contentTypeTextPlain) ;
}
