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

package org.apache.jena.riot.lang.rdfxml.rrx;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Detailed RRX tests, not manifest driven.
 */
@Suite
@SelectClasses( {
    // Local file comparison tests
    Test_RRX_Local_SAX.class,
    Test_RRX_Local_StAXev.class,
    Test_RRX_Local_StAXsr.class,
    // RDF 1.1 test suite as detailed tests.
    // See Scripts_RIOT_
    Test_RRX_W3C_SAX.class,
    Test_RRX_W3C_StAXev.class,
    Test_RRX_W3C_StAXsr.class,

    // Additional RRX specific tests.
    TestExtraRRX.class
})

public class TS_RRX {}
