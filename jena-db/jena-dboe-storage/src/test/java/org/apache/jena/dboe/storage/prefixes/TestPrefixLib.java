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

package org.apache.jena.dboe.storage.prefixes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.jena.atlas.lib.Pair;
import org.junit.Test;

public class TestPrefixLib {
    // abbreviate

    private PrefixMapI create() {
        return PrefixesFactory.createMem();
    }
    protected String pref1  = "pref1";
    protected String pref1a = "pref1:";
    protected String pref2  = "pref2";

    @Test
    public void abbreviate_1() {
        PrefixMapI prefixes = create();
        prefixes.add(pref1, "http://example.net/ns#");

        String x = PrefixLib.abbreviate(prefixes, "http://example.net/ns#xyz");
        assertEquals("pref1:xyz", x);
    }

    @Test
    public void abbreviate_2() {
        PrefixMapI prefixes = create();
        prefixes.add(pref1, "http://example.net/ns#");

        String x = PrefixLib.abbreviate(prefixes, "http://other/ns#xyz");
        assertNull(x);
    }

    @Test
    public void abbrev_1() {
        PrefixMapI prefixes = create();
        prefixes.add(pref1, "http://example.net/ns#");

        Pair<String, String> x = PrefixLib.abbrev(prefixes, "http://example.net/ns#xyz");

        assertEquals("pref1", x.getLeft());
        assertEquals("xyz", x.getRight());
    }

    @Test
    public void abbrev_2() {
        PrefixMapI prefixes = create();
        prefixes.add(pref1, "http://example.net/ns#");
        Pair<String, String> x = PrefixLib.abbrev(prefixes, "http://other/ns#xyz");
        assertNull(x);
    }

    @Test
    public void expand_1() {
        PrefixMapI prefixes = create();
        prefixes.add(pref1, "http://example.net/ns#");
        String x = PrefixLib.expand(prefixes, "pref1:abc");
        assertEquals("http://example.net/ns#abc", x);
        String x2 = PrefixLib.expand(prefixes, "pref1z:abc");
        assertNull(x2);
    }

    @Test
    public void expand_2() {
        PrefixMapI prefixes = create();
        prefixes.add(pref1, "http://example.net/ns#");
        String x2 = PrefixLib.expand(prefixes, "pref1z:abc");
        assertNull(x2);
    }

}
