/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.handlers;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Query;
import org.junit.Before;
import org.junit.Test;

public class PrologHandlerTest extends AbstractHandlerTest {

    private PrologHandler handler;
    private Query query;

    @Before
    public void setup() {
        query = new Query();
        handler = new PrologHandler(query);
    }

    @Test
    public void testAddPrefixString() {
        handler.addPrefix("pfx", "uri");
        String[] lst = byLine(query.toString());
        assertContainsRegex(PREFIX + "pfx:" + SPACE + uri("uri"), lst);
    }

    @Test
    public void testAddPrefixStringWithColon() {
        handler.addPrefix("pfx:", "uri");
        String[] lst = byLine(query.toString());
        assertContainsRegex(PREFIX + "pfx:" + SPACE + uri("uri"), lst);
    }

    @Test
    public void testAddPrefixHandler() {
        PrologHandler handler2 = new PrologHandler(new Query());
        handler2.addPrefix("pfx", "uri");
        handler.addAll(handler2);
        String[] lst = byLine(query.toString());
        assertContainsRegex("PREFIX\\s+pfx:\\s+\\<uri\\>", lst);
    }

    @Test
    public void testAddPrefixes() {
        Map<String, String> map = new HashMap<>();
        map.put("pfx", "uri");
        map.put("pfx2", "uri2");
        handler.addPrefixes(map);
        String[] lst = byLine(query.toString());
        assertContainsRegex(PREFIX + "pfx:" + SPACE + uri("uri"), lst);
        assertContainsRegex(PREFIX + "pfx2:" + SPACE + uri("uri2"), lst);
    }

    @Test
    public void testAddPrefixesWithColon() {
        Map<String, String> map = new HashMap<>();
        map.put("pfx:", "uri");
        map.put("pfx2", "uri2");
        handler.addPrefixes(map);
        String[] lst = byLine(query.toString());
        assertContainsRegex(PREFIX + "pfx:" + SPACE + uri("uri"), lst);
        assertContainsRegex(PREFIX + "pfx2:" + SPACE + uri("uri2"), lst);
    }

    @Test
    public void testAddDuplicatePrefix() {
        handler.addPrefix("pfx", "uri");
        handler.addPrefix("pfx", "uri");
        String[] lst = byLine(query.toString());
        assertContainsRegex("PREFIX\\s+pfx:\\s+\\<uri\\>", lst);
    }

    @Test
    public void testSetBaseString() {
        handler.setBase("foo");
        String[] lst = byLine(query.toString());
        assertContainsRegex("BASE\\s+\\<.+/foo\\>", lst);
    }

    @Test
    public void testBaseAndPrefix() {
        handler.setBase("foo");
        handler.addPrefix("pfx", "uri");
        String[] lst = byLine(query.toString());
        assertContainsRegex("PREFIX\\s+pfx:\\s+\\<uri\\>", lst);
        assertContainsRegex("BASE\\s+\\<.+/foo\\>", lst);
    }

}
