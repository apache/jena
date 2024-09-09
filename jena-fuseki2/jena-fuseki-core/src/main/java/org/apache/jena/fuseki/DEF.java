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

import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import static org.apache.jena.riot.WebContent.*;

/**
 * Content negotiation setup.
 */
public class DEF
{
    // @formatter:off

    // ---- Server configuration default media type

    public static MediaType acceptRDFXML        = acceptRDFXMLDefault();
    public static MediaType acceptNQuads        = acceptNQuadsDefault();
    public static MediaType acceptResultSetXML  = acceptResultSetXMLDefault();
    public static MediaType acceptJSON          = acceptJSONDefault();
    public static MediaType acceptTurtle        = acceptTurtleDefault();

    // ---- Server configuration offers for content negotiation

    public static AcceptList jsonOffer          = jsonOfferDefault();

    public static AcceptList constructOffer     = constructOfferDefault();

    public static AcceptList rdfOffer           = rdfOfferDefault();

    public static AcceptList quadsOffer         = quadsOfferDefault();

    // Offer for SELECT
    public static AcceptList rsOfferTable       = rsOfferTableDefault();

    // Offer for ASK
    public static AcceptList rsOfferBoolean     = rsOfferBooleanDefault();


    // ---- Default configuration settings or when content negotiation does not provide a media type.

    public static final MediaType acceptRDFXMLDefault()       { return MediaType.create(contentTypeRDFXML); }
    public static final MediaType acceptNQuadsDefault()       { return MediaType.create(contentTypeNQuads); }
    public static final MediaType acceptResultSetXMLDefault() { return MediaType.create(contentTypeResultsXML); }
    public static final MediaType acceptJSONDefault()         { return MediaType.create(contentTypeJSON); }
    public static final MediaType acceptTurtleDefault()       { return MediaType.create(contentTypeTurtle); }

    // ---- Default offers for content negotiation.

    public static final AcceptList jsonOfferDefault()         { return AcceptList.create(contentTypeJSON); }

    public static final AcceptList constructOfferDefault()    { return AcceptList.create(contentTypeTurtle,
                                                                                         contentTypeTurtleAlt1,
                                                                                         contentTypeNTriples,
                                                                                         contentTypeNTriplesAlt,
                                                                                         contentTypeRDFXML,
                                                                                         contentTypeTriX,
                                                                                         contentTypeTriXxml,
                                                                                         contentTypeJSONLD,
                                                                                         contentTypeRDFJSON,
                                                                                         contentTypeRDFThrift,

                                                                                         contentTypeTriG,
                                                                                         contentTypeTriGAlt1,
                                                                                         contentTypeNQuads,
                                                                                         contentTypeNQuadsAlt1
                                                                                        ); }

    public static final AcceptList rdfOfferDefault()          { return AcceptList.create(contentTypeTurtle,
                                                                                         contentTypeTurtleAlt1,
                                                                                         contentTypeNTriples,
                                                                                         contentTypeNTriplesAlt,
                                                                                         contentTypeRDFXML,
                                                                                         contentTypeTriX,
                                                                                         contentTypeTriXxml,
                                                                                         contentTypeJSONLD,
                                                                                         contentTypeRDFJSON,
                                                                                         contentTypeRDFThrift
                                                                                        ); }

    public static final AcceptList quadsOfferDefault()        { return AcceptList.create(contentTypeTriG,
                                                                                         contentTypeTriGAlt1,
                                                                                         contentTypeJSONLD,
                                                                                         contentTypeNQuads,
                                                                                         contentTypeNQuadsAlt1,
                                                                                         contentTypeTriX,
                                                                                         contentTypeTriXxml
                                                                                        ); }

    // Offer for SELECT
    // This include application/xml and application/json.
    public static final AcceptList rsOfferTableDefault()      { return AcceptList.create(contentTypeResultsJSON,
                                                                                         contentTypeJSON,
                                                                                         contentTypeTextCSV,
                                                                                         contentTypeTextTSV,
                                                                                         contentTypeResultsXML,
                                                                                         contentTypeXML,
                                                                                         contentTypeResultsThrift,
                                                                                         contentTypeTextPlain
                                                                                        ); }

    // Offer for ASK
    // This includes application/xml and application/json and excludes application/sparql-results+thrift
    public static final AcceptList rsOfferBooleanDefault()    { return AcceptList.create(contentTypeResultsJSON,
                                                                                         contentTypeJSON,
                                                                                         contentTypeTextCSV,
                                                                                         contentTypeTextTSV,
                                                                                         contentTypeResultsXML,
                                                                                         contentTypeXML,
                                                                                         contentTypeTextPlain
                                                                                        ); }

    // @formatter:on
}
