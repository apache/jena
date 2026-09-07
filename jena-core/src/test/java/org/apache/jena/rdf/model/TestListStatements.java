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

package org.apache.jena.rdf.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestListStatements {
    private static Model m;
    private static Resource s;
    private static Property p;

    @BeforeAll
    public static void setUpBeforeClass() {
        TestListStatements.m = ModelFactory.createDefaultModel();
        s = TestListStatements.m.createResource("http://www.a.com/s");
        p = TestListStatements.m.createProperty("http://www.a.com/p");

        TestListStatements.m.add(s, p, TestListStatements.m.createResource("http://www.a.com/o"));
        TestListStatements.m.add(s, p, "texte", "fr");
        TestListStatements.m.add(s, p, "text", "en");
        TestListStatements.m.add(s, p, "text");
        TestListStatements.m.add(TestListStatements.m.createLiteralStatement(s, p, 1789));
    }

    @AfterAll
    public static void tearDownAfterClass() {
        TestListStatements.m = null;
        TestListStatements.s = null;
        TestListStatements.p = null;
    }

    @Test
    public final void thereAre2LitsWoLang() {
        final StmtIterator it = TestListStatements.m.listStatements(TestListStatements.s, TestListStatements.p, null, "");
        assertTrue(it.toList().size() == 2);
    }

    @Test
    public final void thereAre4Literals() {
        final StmtIterator it = TestListStatements.m.listStatements(TestListStatements.s, TestListStatements.p, null, null);
        assertTrue(it.toList().size() == 4);
    }

    @Test
    public final void thereIsOneFrench() {
        final StmtIterator it = TestListStatements.m.listStatements(TestListStatements.s, TestListStatements.p, null, "fr");
        final List<Statement> lis = it.toList();
        assertTrue(lis.size() == 1);
        assertTrue(lis.get(0).getObject().toString().equals("\"texte\"@fr"));
    }

    @Test
    public final void theresAreTwoText() {
        final StmtIterator it = TestListStatements.m.listStatements(TestListStatements.s, TestListStatements.p, "text", null);
        final List<Statement> lis = it.toList();
        assertTrue(lis.size() == 2);
    }

    @Test
    public final void theresOneTextEN() {
        final StmtIterator it = TestListStatements.m.listStatements(TestListStatements.s, TestListStatements.p, "text", "en");
        final List<Statement> lis = it.toList();
        assertTrue(lis.size() == 1);
        assertTrue(lis.get(0).getObject().toString().equals("\"text\"@en"));
    }

    @Test
    public final void theresOneTextWoLang() {
        final StmtIterator it = TestListStatements.m.listStatements(TestListStatements.s, TestListStatements.p, "text", "");
        final List<Statement> lis = it.toList();
        assertTrue(lis.size() == 1);
    }

    @Test
    public final void theresOneWithABNodeObject() {
        Model m = ModelFactory.createDefaultModel();
        Resource anon = m.createResource();
        m.createResource("http://example").addProperty(RDF.type, anon);

        StmtIterator it = m.listStatements(null, null, anon);
        final List<Statement> lis = it.toList();
        assertTrue(lis.size() == 1);

    }
}
