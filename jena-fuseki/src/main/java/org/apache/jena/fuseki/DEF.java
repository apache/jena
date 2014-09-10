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

import static org.apache.jena.riot.WebContent.* ;
import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaType ;

public class DEF
{
    public static final MediaType acceptRDFXML        = MediaType.create(contentTypeRDFXML) ;
    public static final MediaType acceptNQuads        = MediaType.create(contentTypeNQuads) ;
    public static final MediaType acceptRSXML         = MediaType.create(contentTypeResultsXML) ;

    public static final AcceptList rdfOffer           = AcceptList.create(contentTypeTurtle, 
                                                                          contentTypeTurtleAlt1,
                                                                          contentTypeTurtleAlt2,
                                                                          contentTypeNTriples,
                                                                          contentTypeNTriplesAlt,
                                                                          contentTypeRDFXML,
                                                                          contentTypeJSONLD,
                                                                          contentTypeRDFJSON,
                                                                          contentTypeRDFThrift
                                                                          ) ;
    
    public static final AcceptList quadsOffer         = AcceptList.create(contentTypeTriG,
                                                                          contentTypeTriGAlt1,
                                                                          contentTypeTriGAlt2,
                                                                          contentTypeJSONLD,
                                                                          contentTypeNQuads,
                                                                          contentTypeNQuadsAlt1,
                                                                          contentTypeNQuadsAlt2 
                                                                          ) ;
    
    public static final AcceptList rsOfferTable            = AcceptList.create(contentTypeResultsJSON,
                                                                          contentTypeTextCSV,
                                                                          contentTypeTextTSV,
                                                                          contentTypeResultsXML,
                                                                          contentTypeResultsThrift,
                                                                          contentTypeTextPlain
                                                                          ) ;
    
    public static final AcceptList rsOfferBoolean            = AcceptList.create(contentTypeResultsJSON,
                                                                          contentTypeTextCSV,
                                                                          contentTypeTextTSV,
                                                                          contentTypeResultsXML,
                                                                          contentTypeTextPlain
                                                                          ) ;

}
