/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ext.xerces.xs;

/**
 * This interface represents the Simple Type Definition schema component. This 
 * interface provides several query operations for facet components. Users 
 * can either retrieve the defined facets as XML Schema components, using 
 * the <code>facets</code> and the <code>multiValueFacets</code> attributes; 
 * or users can separately query a facet's properties using methods such as 
 * <code>getLexicalFacetValue</code>, <code>isFixedFacet</code>, etc. 
 */
public interface XSSimpleTypeDefinition extends XSTypeDefinition {
    // Variety definitions
    /**
     * The variety is absent for the anySimpleType definition.
     */
    public static final short VARIETY_ABSENT            = 0;
    /**
     * <code>Atomic</code> type.
     */
    public static final short VARIETY_ATOMIC            = 1;

    // Facets
    /**
     * No facets defined.
     */
    public static final short FACET_NONE                = 0;
    /**
     * 4.3.4 pattern.
     */
    public static final short FACET_PATTERN             = 8;
    /**
     * 4.3.5 whitespace.
     */
    public static final short FACET_WHITESPACE          = 16;
    /**
     * 4.3.7 maxInclusive.
     */
    public static final short FACET_MAXINCLUSIVE        = 32;
    /**
     * 4.3.9 maxExclusive.
     */
    public static final short FACET_MAXEXCLUSIVE        = 64;
    /**
     * 4.3.9 minExclusive.
     */
    public static final short FACET_MINEXCLUSIVE        = 128;
    /**
     * 4.3.10 minInclusive.
     */
    public static final short FACET_MININCLUSIVE        = 256;

    /**
     * A constant defined for the 'ordered' fundamental facet: not ordered.
     */
    public static final short ORDERED_FALSE             = 0;
    /**
     * A constant defined for the 'ordered' fundamental facet: partially 
     * ordered.
     */
    public static final short ORDERED_PARTIAL           = 1;
    /**
     * A constant defined for the 'ordered' fundamental facet: total ordered.
     */
    public static final short ORDERED_TOTAL             = 2;
}
