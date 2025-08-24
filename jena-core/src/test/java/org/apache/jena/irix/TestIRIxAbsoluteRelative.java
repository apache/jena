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
public class TestIRIxAbsoluteRelative extends AbstractTestIRIx_3986 {

    public TestIRIxAbsoluteRelative(String name, IRIProvider provider) {
        super(name, provider);
    }

    // -- isAbsolute, isRelative : These are not opposites in RFC 3986. (String, isAbsolute, isRelative)

    @Test public void abs_rel_01()   { test_abs_rel("http://example/abc", true, false); }

    @Test public void abs_rel_02()   { test_abs_rel("abc", false, true); }

    @Test public void abs_rel_03()   { test_abs_rel("http://example/abc#def", false, false); }

    @Test public void abs_rel_04()   { test_abs_rel("abc#def", false, true); }

    // Create - is it suitable for an RDF reference?
    private void test_abs_rel(String uriStr, boolean isAbsolute, boolean isRelative) {
        IRIx iri = test_create(uriStr);
        assertEquals("Absolute test: IRI = "+uriStr, isAbsolute, iri.isAbsolute());
        assertEquals("Relative test: IRI = "+uriStr, isRelative, iri.isRelative());
    }
}
