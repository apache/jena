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

public class TestVocabRSS {
    @Test
    public void testRSS() {
        String ns = "http://purl.org/rss/1.0/";
        VocabTestLib.assertResource(ns + "channel", RSS.channel);
        VocabTestLib.assertResource(ns + "item", RSS.item);
        VocabTestLib.assertProperty(ns + "description", RSS.description);
        VocabTestLib.assertProperty(ns + "image", RSS.image);
        VocabTestLib.assertProperty(ns + "items", RSS.items);
        VocabTestLib.assertProperty(ns + "link", RSS.link);
        VocabTestLib.assertProperty(ns + "name", RSS.name);
        VocabTestLib.assertProperty(ns + "textinput", RSS.textinput);
        VocabTestLib.assertProperty(ns + "title", RSS.title);
        VocabTestLib.assertProperty(ns + "url", RSS.url);
    }
}
