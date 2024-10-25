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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestIRIxReference extends AbstractTestIRIx_3986 {

    public TestIRIxReference(String name, IRIProvider provider) {
        super(name, provider);
    }

    // --- Use in RDF

    @Test public void reference_01() { reference("http://example/", true); }

    @Test public void reference_02() { reference("http://example/abcd", true); }

    @Test public void reference_03() { reference("//example/", false); }

    @Test public void reference_04() { reference("relative-uri", false); }

    @Test public void reference_05() { reference("http://example/", true); }

    @Test public void reference_06() { reference("http://example/", true); }

    @Test public void reference_07() { reference("http://example/", true); }

    @Test public void reference_08() { reference("file:///a:/~jena/file", true); }

    @Test public void reference_09() { reference("http://example/abcd#frag", true); }

    @Test public void reference_10() { reference("wm:/abc", true); }

    private void reference(String uriStr, boolean expected) {
        IRIx iri = test_create(uriStr);
        assertEquals("IRI = "+uriStr, expected, iri.isReference());
    }


}
