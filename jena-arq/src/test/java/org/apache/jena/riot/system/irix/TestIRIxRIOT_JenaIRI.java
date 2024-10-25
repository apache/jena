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

package org.apache.jena.riot.system.irix;

import org.junit.jupiter.api.Test;

import org.apache.jena.irix.IRIProvider;
import org.apache.jena.irix.IRIProviderJenaIRI;
import org.apache.jena.riot.Lang;

/**
 * Test IRIx in parser usage.
 * jena-iri provider (up to Jena 5.2.0)
 */
public class TestIRIxRIOT_JenaIRI extends AbstractTestIRIxRIOT_system {

    protected TestIRIxRIOT_JenaIRI() {
        super("jena-iri");
    }
    private static final IRIProvider testProvider = makeTestProvider();
    private static final IRIProvider makeTestProvider() {
        // Needs to agree with SystemIRIx.
        IRIProvider newProviderJenaIRI = new IRIProviderJenaIRI();
        newProviderJenaIRI.strictMode("urn",  false);
        newProviderJenaIRI.strictMode("http", false);
        newProviderJenaIRI.strictMode("file", false);
        return newProviderJenaIRI;
    }

    @Override
    protected IRIProvider getProviderForTest() {
        return testProvider;
    }

    // Policy change: NT, no checking is "IRI 3986 syntax" - no scheme violations.
    // // The IRIPRoviderJenaIRI handled this in a way that is not parser-checking sensitive.

    @Override @Test public void irix_http_2_nt_check()    { testLang(httpUri02, Lang.NT, UNSET, TRUE, 0, 0); }
    @Override @Test public void irix_http_2_ttl()         { testLang(httpUri02, Lang.NT, UNSET, TRUE, 0, 0); }

    // http://EXAMPLE/
    @Override @Test public void irix_http_3_nt_check()    { testLang(httpUri03, Lang.NT, UNSET, TRUE, 0, 0); }
    @Override @Test public void irix_http_3_ttl()         { testDft (httpUri03, Lang.TTL, 0, 0); }

    // jena-iri always warns on user/password in the authority.
    // jena-iri3986 does not in NT unchecked.
    // nt_check, ttl - different number of warnings.
    @Override @Test public void irix_http_4_nt_check()    { testLang(httpUri04, Lang.NT, UNSET, TRUE, 0, 1); }
    @Override @Test public void irix_http_4_ttl()         { testDft (httpUri04, Lang.TTL, 0, 1); }

    // jena-iri does not warn on user, no password
    @Override @Test public void irix_http_5_nt()          { testDft (httpUri05, Lang.NT, 0, 0); }

    //@Override @Test public void irix_urn_uuid_4_nt()      { testDft (urnuuid04, Lang.NT, 0, 1); }
    //@Override @Test public void irix_urn_uuid_6_nt()      { testDft (urnuuid06, Lang.NT, 0, 1); }

    // The IRIPRoviderJenaIRI handled this in a way that is not parser-checking sensitive.
    @Override @Test public void irix_urn_uuid_3_nt()      { testDft (urnuuid03, Lang.NT, 0, 1); }

    // jena-iri3986 does not issue warning in NT, unchecked.
    @Override @Test public void irix_uuid_3_nt()          { testDft (uuid03, Lang.NT, 0, 1); }
    @Override @Test public void irix_uuid_4_nt()          { testDft (uuid04, Lang.NT, 0, 1); }

    @Override @Test public void irix_uuid_6_nt()          { testDft (uuid06, Lang.NT, 0, 1); }
    @Override @Test public void irix_uuid_6_nt_check()    { testLang(uuid06, Lang.NT, UNSET, TRUE, 0, 1); }
    @Override @Test public void irix_uuid_6_ttl()         { testDft (uuid06, Lang.TTL, 0, 1); }

    // jena-iri is (too) lenient for uuid:
    @Override @Test public void irix_uuid_7_nt()          { testDft (urnuuid07, Lang.NT, 0, 0); }
    @Override @Test public void irix_uuid_7_nt_check()    { testLang(urnuuid07, Lang.NT, UNSET, TRUE, 0, 0); }
    @Override @Test public void irix_uuid_7_ttl()         { testDft (urnuuid07, Lang.TTL, 0, 0); }

    //@Override @Test public void irix_uuid_2_nt()          { testDft (urnuuid02, Lang.NT, 0, 1); }
    @Override @Test public void irix_uuid_8_nt()          { testDft (urnuuid07, Lang.NT, 0, 0); }
}
