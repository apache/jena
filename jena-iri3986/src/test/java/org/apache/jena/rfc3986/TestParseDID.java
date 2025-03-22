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

package org.apache.jena.rfc3986;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

import org.apache.jena.rfc3986.ParseDID.DIDParseException;

/** Test the class ParseDID */
public class TestParseDID {
    @Test public void parseDID_01() { goodDID("did:method:specific"); }
    @Test public void parseDID_02() { goodDID("did:method:SPECIFIC"); }
    @Test public void parseDID_03() { goodDID("did:method:1:2:3:4"); }
    @Test public void parseDID_04() { goodDID("did:method:1:2::4"); }
    @Test public void parseDID_05() { goodDID("did:method:1"); }

    @Test public void parseDID_06() { goodDID("did:method:abc%41"); }   // %41 == uppercase 'A'

    @Test public void parseDID_bad_01() { badDID("did::specific"); }
    @Test public void parseDID_bad_02() { badDID("did:method:"); }
    @Test public void parseDID_bad_03() { badDID("did::"); }
    @Test public void parseDID_bad_04() { badDID("did:CASE:specific"); }

    @Test public void parseDID_bad_06() { badDID("did:abc345:specific"); }
    @Test public void parseDID_bad_07() { badDID("did:method:specifc:"); }
    @Test public void parseDID_bad_08() { badDID("did:method:1:2:"); }

    @Test public void parseDID_bad_09() { badDID("urn:%61:NSS"); } // %61 == lowercase 'a'
    @Test public void parseDID_bad_20() { badDID("urn:NID:NSS"); }

    private void goodDID(String string) {
        ParseDID.parse(string, false);
    }

    private void badDID(String string) {
        assertThrowsExactly(DIDParseException.class,
                            ()->ParseDID.parse(string, false)
                );
    }
}
