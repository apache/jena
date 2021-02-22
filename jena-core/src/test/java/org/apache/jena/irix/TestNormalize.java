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
import static org.junit.Assume.assumeFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestNormalize extends AbstractTestIRIx {

    public TestNormalize(String name, IRIProvider provider) {
        super(name, provider);
    }

    @Test
    public void normalize_01() {
        testNormalize("http://host/a/b/c?q=1#2", "http://host/a/b/c?q=1#2");
    }

    @Test
    public void normalize_02() {
        testNormalize("HtTp://host/a/b/c?q=1#2", "http://host/a/b/c?q=1#2");
    }

    @Test
    public void normalize_03() {
        testNormalize("HTTP://HOST/a/b/c?q=1#2", "http://host/a/b/c?q=1#2");
    }

    @Test
    public void normalize_04() {
        testNormalize("HTTP://HOST:/a/b/c?q=1#2", "http://host/a/b/c?q=1#2");
    }

    @Test
    public void normalize_05() {
        testNormalize("HTTP://HOST:80/a/b/c?q=1#2", "http://host/a/b/c?q=1#2");
    }

    @Test
    public void normalize_06() {
        testNormalize("HTTPs://HOST:443/a/b/c?q=1#2", "https://host/a/b/c?q=1#2");
    }

    @Test
    public void normalize_07() {
        testNormalize("http://host", "http://host/");
    }

    @Test
    public void normalize_08() {
        testNormalize("http://host#frag", "http://host/#frag");
    }

    @Test
    public void normalize_09() {
        testNormalize("http://host?q=s", "http://host/?q=s");
    }

    @Test
    public void normalize_10() {
        testNormalize("http://host/?q=s", "http://host/?q=s");
    }

    @Test
    public void normalize_11() {
        testNormalize("http://host%20/?q=s", "http://host%20/?q=s");
    }

    @Test
    public void normalize_12() {
        testNormalize("http://hOSt%20/?q=s", "http://host%20/?q=s");
    }

    @Test
    public void normalize_13() {
        testNormalize("http://hOSt%20/foo%62ar?q=s", "http://host%20/foobar?q=s");
    }

    @Test
    public void normalize_14() {
        testNormalize("http://host/foobar?q=s%74", "http://host/foobar?q=st");
    }

    @Test
    public void normalize_15() {
        testNormalize("http://host/foobar#%7E", "http://host/foobar#~");
    }

    private void testNormalize(String input, String expected) {
        // jena-iri does not implement normalization.
        assumeFalse("jena-iri does not implement normalization", SystemIRIx.getProvider() instanceof IRIProviderJenaIRI );
//        if ( SystemIRIx.getProvider() instanceof IRIProviderJenaIRI )
//            return;
        IRIx iri = IRIx.create(input);
        IRIx iri2 = iri.normalize();
        String s = iri2.toString();
        assertEquals(expected, s);
    }
}
