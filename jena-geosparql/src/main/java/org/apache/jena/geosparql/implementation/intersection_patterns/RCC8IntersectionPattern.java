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
public interface RCC8IntersectionPattern {

    public static final String EQUALS = "TFFFTFFFT";

    public static final String DISCONNECTED = "FFTFFTTTT";

    public static final String EXTERNALLY_CONNECTED = "FFTFTTTTT";

    public static final String PARTIALLY_OVERLAPPING = "TTTTTTTTT";

    public static final String TANGENTIAL_PROPER_PART_INVERSE = "TTTFTTFFT";

    public static final String TANGENTIAL_PROPER_PART = "TFFTTFTTT";

    public static final String NON_TANGENTIAL_PROPER_PART = "TFFTFFTTT";

    public static final String NON_TANGENTIAL_PROPER_PART_INVERSE = "TTTFFTFFT";

}
