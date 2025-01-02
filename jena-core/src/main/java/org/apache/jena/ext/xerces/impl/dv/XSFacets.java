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

package org.apache.jena.ext.xerces.impl.dv;

/**
 * {@literal @xerces.internal}
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id: XSFacets.java 699892 2008-09-28 21:08:27Z mrglavas $
 */
public class XSFacets {

    /**
     * value of whiteSpace facet.
     */
    public short whiteSpace;

    /**
     * string containing value of pattern facet, for multiple patterns values
     * are ORed together.
     */
    public String pattern;

    /**
     * value of maxInclusive facet.
     */
    public String maxInclusive;

    /**
     * value of maxExclusive facet.
     */
    public String maxExclusive;

    /**
     * value of minInclusive facet.
     */
    public String minInclusive;

    /**
     * value of minExclusive facet.
     */
    public String minExclusive;
}
