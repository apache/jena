/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.implementation.intersection_patterns;

/**
 *
 *
 */
public interface EgenhoferIntersectionPattern {

    public static final String EQUALS = "TFFFTFFFT";
    //Pattern "TFFFTFFFT" stated in GeoSPARQL 11-052r4 p. 9.

    public static final String DISJOINT = "FF*FF****";

    public static final String MEET1 = "FT*******";

    public static final String MEET2 = "F**T*****";

    public static final String MEET3 = "F***T****";

    public static final String OVERLAP = "T*T***T**";

    public static final String COVERS = "T*TFT*FF*";

    public static final String COVERED_BY = "TFF*TFT**";

    public static final String INSIDE = "TFF*FFT**";

    public static final String CONTAINS = "T*TFF*FF*";

}
