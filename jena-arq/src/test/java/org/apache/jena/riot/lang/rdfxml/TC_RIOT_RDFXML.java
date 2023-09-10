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

package org.apache.jena.riot.lang.rdfxml;

import org.apache.jena.riot.lang.rdfxml.converted_legacy.TS_ConvertedARP1;
import org.apache.jena.riot.lang.rdfxml.manifest_rdf11.Scripts_RRX_RDFXML;
import org.apache.jena.riot.lang.rdfxml.rrx.TS_RRX;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    // Local file and rdf11-xml - detailed checking.
    TS_RRX.class,

    // Manifest-driven rdf11-xml - all parsers
    Scripts_RRX_RDFXML.class,

    // jena-core legacy test (RDF 1.0)
    TS_ConvertedARP1.class
})

/**
 * RIOT test suites for RDFXML.
 * <p>
 * {@linkplain TS_ConvertedARP1} runs the ARP (final) tests
 * converted to run as RIOT tests.
 * <p>
 * {@linkplain TS_RRX} runs local RRX tests by comparing the different RRX parsers
 * to ARP1. tese test check for the same number of warnig as well.
 * The {@code TestRDFXML_RRX_*} are running on extra local files. The
 * TestRDFXML_W3C_* are running on the RDF 1.0 test suite that ARP1 has used.
 * <p>
 * {@linkplain Scripts_RRX_RDFXML} runs the RRX and APR parsers on the rdf-tests maintained
 * test suite. These are RDF1.1 and RDF 1.2 tests.
 * <p>
 * ARP1 is ARP is the RDFXML parser using IRIx in Jena4 from Jena 4.7.0.
 * <p>
 * ARP0 is ARP before conversion to IRIx and used in Jena up and including  to 4.6.1.
 * It is unlikely to be maintained.
 */
public class TC_RIOT_RDFXML {}
