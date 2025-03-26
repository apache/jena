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

package org.apache.jena.langtag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestLangTagsOps {
    @Test
    public void sameLangTag_01() {
        LangTag langTag1 = LangTag.of("en-GB");
        LangTag langTag2 = LangTag.of("en-GB");
        sameLangTag(langTag1, langTag2, true,  true, true);
    }

    @Test
    public void sameLangTag_02() {
        LangTag langTag1 = LangTag.of("en-GB");
        LangTag langTag2 = LangTag.of("en-gb");
        sameLangTag(langTag1, langTag2, true,  false, false);
    }

    @Test
    public void sameLangTag_03() {
        LangTag langTag1 = LangTag.of("en-GB-Latn");
        LangTag langTag2 = LangTag.of("en-gb");
        sameLangTag(langTag1, langTag2, false,  false, false);
    }

    private static void sameLangTag(LangTag langTag1, LangTag langTag2, boolean sameAs, boolean equals, boolean sameHash) {
        if ( sameAs )
            assertTrue(LangTags.sameLangTagAs(langTag1, langTag2));
        else
            assertFalse(LangTags.sameLangTagAs(langTag1, langTag2));
        if ( equals )
            assertTrue(langTag1.equals(langTag2));
        else
            assertFalse(langTag1.equals(langTag2));
        if ( sameHash )
            assertEquals(langTag1.hashCode(), langTag2.hashCode());
        // No "hash must be different"
    }
}
