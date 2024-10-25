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

package org.apache.jena.irix;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests that don't easily go in other TestIRIx suites.
 */
@RunWith(Parameterized.class)
public class TestIRIxOther extends AbstractTestIRIx_3986 {

    public TestIRIxOther(String name, IRIProvider provider) {
        super(name, provider);
    }

    @Test public void scheme_unknown_1() {
        good("mysteryScheme://authority/path/file");
    }

    @Test public void scheme_unknown_2() {
        good("mysteryScheme:ABC");
    }

    @Test public void strict_1() {
        strict("urn", ()->bad("urn:x:abc"));
    }

    @Test public void strict_2() {
        notStrict("urn", ()->bad("urn:x:abc"));
    }

    // Jena rules uses urn:x-hp in a way that is exposed to user code.
    @Test public void irix_jena_1() {
        good("urn:x-hp:abc");
    }

    // Full check
    private void good(String string) {
        IRIx iri = test_create(string);
        assertNotNull(iri);
        if ( iri.hasViolations() ) {
            iri.handleViolations((isError, message)->{});
        }
        assertFalse(iri.hasViolations());
    }

    // RFC 3986 syntax only, not URi scheme.
    private void goodNoIRICheck(String string) {
        IRIx iri = test_create(string);
    }

    // Expect an IRIParseException
    private void bad(String string) {
        try {
            IRIx iri = test_create(string);
            if ( ! iri.isReference())
                fail("Did not fail: "+string);
        } catch (IRIException ex) {}
    }
}

