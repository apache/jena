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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestModelRead - test that the model.read operation(s) exist.
 */
@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestModelRead extends AbstractModelTestBase {
    protected static Logger logger = LoggerFactory.getLogger(TestModelRead.class);

    @Test
    public void testDefaultLangXML() {
        final Model model = ModelFactory.createDefaultModel();
        model.read(getFileName("modelReading/plain.rdf"), null, null);
    }

    @Test
    public void testLoadsSimpleModel() {
        final Model expected = createModel();
        expected.read(getFileName("modelReading/simple.n3"), "N3");
        assertSame(model, model.read(getFileName("modelReading/simple.n3"), "base", "N3"));
        ModelHelper.assertIsoModels(expected, model);
    }

    @Test
    public void testReturnsSelf() {

        assertSame(model, model.read(getFileName("modelReading/empty.n3"), "base", "N3"));
        assertTrue(model.isEmpty());
    }

    @Test
    public void testSimpleLoadExplicitBase() {
        final Model mBasedExplicit = createModel();
        mBasedExplicit.read(getFileName("modelReading/based.n3"), "http://example/", "N3");
        ModelHelper.assertIsoModels(modelWithStatements("http://example/ ja:predicate ja:object"), mBasedExplicit);
    }
}
