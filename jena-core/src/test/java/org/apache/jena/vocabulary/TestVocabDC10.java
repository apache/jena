/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.vocabulary;

import org.junit.jupiter.api.Test;

public class TestVocabDC10 {
    @Test
    public void testDC10() {
        String ns = "http://purl.org/dc/elements/1.0/";
        VocabTestLib.assertProperty(ns + "contributor", DC_10.contributor);
        VocabTestLib.assertProperty(ns + "coverage", DC_10.coverage);
        VocabTestLib.assertProperty(ns + "creator", DC_10.creator);
        VocabTestLib.assertProperty(ns + "date", DC_10.date);
        VocabTestLib.assertProperty(ns + "description", DC_10.description);
        VocabTestLib.assertProperty(ns + "format", DC_10.format);
        VocabTestLib.assertProperty(ns + "identifier", DC_10.identifier);
        VocabTestLib.assertProperty(ns + "language", DC_10.language);
        VocabTestLib.assertProperty(ns + "publisher", DC_10.publisher);
        VocabTestLib.assertProperty(ns + "relation", DC_10.relation);
        VocabTestLib.assertProperty(ns + "rights", DC_10.rights);
        VocabTestLib.assertProperty(ns + "source", DC_10.source);
        VocabTestLib.assertProperty(ns + "subject", DC_10.subject);
        VocabTestLib.assertProperty(ns + "title", DC_10.title);
        VocabTestLib.assertProperty(ns + "type", DC_10.type);
    }
}
