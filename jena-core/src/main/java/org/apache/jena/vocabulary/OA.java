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

package org.apache.jena.vocabulary;

import org.apache.jena.rdf.model.* ;

/**
    The standard Open Annotation vocabulary.
 */

public class OA {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String NS = "http://www.w3.org/ns/oa#";

    /** returns the URI for this schema
        @return the URI for this schema
     */
    public static String getURI()
    { return NS; }

    protected static final Resource resource( String local )
    { return ResourceFactory.createResource( NS + local ); }

    protected static final Property property( String local )
    { return ResourceFactory.createProperty( NS, local ); }

    public final static Resource Annotation = resource( "Annotation" );
    public final static Resource Choice = resource( "Choice" );
    public final static Resource CssSelector = resource( "CssSelector" );
    public final static Resource CssStyle = resource( "CssStyle" );
    public final static Resource DataPositionSelector = resource( "DataPositionSelector" );
    public final static Resource Direction = resource( "Direction" );
    public final static Resource FragmentSelector = resource( "FragmentSelector" );
    public final static Resource HttpRequestState = resource( "HttpRequestState" );
    public final static Resource Motivation = resource( "Motivation" );
    public final static Resource RangeSelector = resource( "RangeSelector" );
    public final static Resource ResourceSelection = resource( "ResourceSelection" );
    public final static Resource Selector = resource( "Selector" );
    public final static Resource SpecificResource = resource( "SpecificResource" );
    public final static Resource State = resource( "State" );
    public final static Resource Style = resource( "Style" );
    public final static Resource SvgSelector = resource( "SvgSelector" );
    public final static Resource TextPositionSelector = resource( "TextPositionSelector" );
    public final static Resource TextQuoteSelector = resource( "TextQuoteSelector" );
    public final static Resource TextualBody = resource( "TextualBody" );
    public final static Resource TimeState = resource( "TimeState" );
    public final static Resource XPathSelector = resource( "XPathSelector" );
    public final static Resource PreferContainedDescriptions = resource( "PreferContainedDescriptions" );
    public final static Resource PreferContainedIRIs = resource( "PreferContainedIRIs" );
    public final static Property annotationService = property( "annotationService" );
    public final static Resource assessing = resource( "assessing" );
    public final static Property bodyValue = property( "bodyValue" );
    public final static Resource bookmarking = resource( "bookmarking" );
    public final static Property cachedSource = property( "cachedSource" );
    public final static Property canonical = property( "canonical" );
    public final static Resource classifying = resource( "classifying" );
    public final static Resource commenting = resource( "commenting" );
    public final static Resource describing = resource( "describing" );
    public final static Resource editing = resource( "editing" );
    public final static Property end = property( "end" );
    public final static Property exact = property( "exact" );
    public final static Property hasBody = property( "hasBody" );
    public final static Property hasEndSelector = property( "hasEndSelector" );
    public final static Property hasPurpose = property( "hasPurpose" );
    public final static Property hasScope = property( "hasScope" );
    public final static Property hasSelector = property( "hasSelector" );
    public final static Property hasSource = property( "hasSource" );
    public final static Property hasStartSelector = property( "hasStartSelector" );
    public final static Property hasState = property( "hasState" );
    public final static Property hasTarget = property( "hasTarget" );
    public final static Resource highlighting = resource( "highlighting" );
    public final static Resource identifying = resource( "identifying" );
    public final static Resource linking = resource( "linking" );
    public final static Resource ltrDirection = resource( "ltrDirection" );
    public final static Resource moderating = resource( "moderating" );
    public final static Property motivatedBy = property( "motivatedBy" );
    public final static Property prefix = property( "prefix" );
    public final static Property processingLanguage = property( "processingLanguage" );
    public final static Resource questioning = resource( "questioning" );
    public final static Property refinedBy = property( "refinedBy" );
    public final static Property renderedVia = property( "renderedVia" );
    public final static Resource replying = resource( "replying" );
    public final static Resource rtlDirection = resource( "rtlDirection" );
    public final static Property sourceDate = property( "sourceDate" );
    public final static Property sourceDateEnd = property( "sourceDateEnd" );
    public final static Property sourceDateStart = property( "sourceDateStart" );
    public final static Property start = property( "start" );
    public final static Property styleClass = property( "styleClass" );
    public final static Property styledBy = property( "styledBy" );
    public final static Property suffix = property( "suffix" );
    public final static Resource tagging = resource( "tagging" );
    public final static Property textDirection = property( "textDirection" );
    public final static Property via = property( "via" );
}
