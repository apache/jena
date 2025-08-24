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

package org.apache.jena.langtagx;

import org.junit.Test;

import static org.junit.Assert.*;

import org.apache.jena.langtag.LangTag;
import org.apache.jena.shared.JenaException;

/**
 * Test of the LangTagX adapter to language tag implementation. This is not a
 * comprehensive set of test for language tags.
 */
public class TestLangTagX {

    @Test
    public void check_langtag_01() {
        assertTrue(LangTagX.checkLanguageTag("en-gb"));
    }

    @Test
    public void check_langtag_02() {
        assertTrue(LangTagX.checkLanguageTag("en-gb-oed"));
    }

    @Test
    public void check_langtag_03() {
        assertFalse(LangTagX.checkLanguageTag("en-ab-xy"));
    }

    @Test
    public void require_langtag_01() {
        LangTagX.requireValidLanguageTag("en-gb");
    }

    @Test(expected = JenaException.class)
    public void require_langtag_02() {
        LangTagX.requireValidLanguageTag("en-ab-xy");
    }

    @Test
    public void langtag_01() {
        LangTag langTag = LangTagX.createLanguageTag("en-gb");
        assertNotNull(langTag);
    }

    @Test(expected = JenaException.class)
    public void langtag_02() {
        LangTag langTag = LangTagX.createLanguageTag("en-ab-xy");
        assertNotNull(langTag);
    }

    @Test
    public void langtag_format_01() {
        String fmt = LangTagX.formatLanguageTag("en-gb");
        assertEquals("en-GB", fmt);
    }

    @Test
    public void langtag_format_02() {
        String fmt = LangTagX.formatLanguageTag("en-latn-illegalSubTag");
        // Falls back to the "by part" formatting
        assertEquals("en-Latn-illegalsubtag", fmt);
    }

    @Test
    public void langtag_format_03() {
        String fmt = LangTagX.formatLanguageTag("");
        assertEquals("", fmt);
    }

    @Test(expected = JenaException.class)
    public void langtag_format_04() {
        String fmt = LangTagX.formatLanguageTag("   ");
        assertEquals("", fmt);
    }
}
