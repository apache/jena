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

package org.apache.jena.assembler;

import org.apache.jena.assembler.assemblers.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.test.JenaTestLib;

public class TestBuiltinAssemblerGroup extends AssemblerTestBase {
    public TestBuiltinAssemblerGroup(String name) {
        super(name);
    }

    @Override
    protected Class<? extends Assembler> getAssemblerClass() {
        return null;
    }

    @SuppressWarnings("removal")
    public void testGeneralRegistration() {
        assertAssemblerClass(JA.DefaultModel, DefaultModelAssembler.class);
        assertAssemblerClass(JA.PrefixMapping, PrefixMappingAssembler.class);
        assertAssemblerClass(JA.SinglePrefixMapping, PrefixMappingAssembler.class);
        assertAssemblerClass(JA.OntModel, OntModelAssembler.class);
        assertAssemblerClass(JA.OntModelSpec, OntModelSpecAssembler.class);
        assertAssemblerClass(JA.Content, ContentAssembler.class);
        assertAssemblerClass(JA.ContentItem, ContentAssembler.class);
        assertAssemblerClass(JA.ReasonerFactory, ReasonerFactoryAssembler.class);
        assertAssemblerClass(JA.InfModel, InfModelAssembler.class);
        assertAssemblerClass(JA.MemoryModel, MemoryModelAssembler.class);
        assertAssemblerClass(JA.RuleSet, RuleSetAssembler.class);
        assertAssemblerClass(JA.DocumentManager, DocumentManagerAssembler.class);
        assertAssemblerClass(JA.UnionModel, UnionModelAssembler.class);
    }

    @SuppressWarnings("removal")
    public void testVariables() {
        JenaTestLib.assertInstanceOf(DefaultModelAssembler.class, Assembler.defaultModel);
        JenaTestLib.assertInstanceOf(PrefixMappingAssembler.class, Assembler.prefixMapping);
        JenaTestLib.assertInstanceOf(OntModelAssembler.class, Assembler.ontModel);
        JenaTestLib.assertInstanceOf(OntModelSpecAssembler.class, Assembler.ontModelSpec);
        JenaTestLib.assertInstanceOf(ContentAssembler.class, Assembler.content);
        JenaTestLib.assertInstanceOf(ReasonerFactoryAssembler.class, Assembler.reasonerFactory);
        JenaTestLib.assertInstanceOf(InfModelAssembler.class, Assembler.infModel);
        JenaTestLib.assertInstanceOf(MemoryModelAssembler.class, Assembler.memoryModel);
        JenaTestLib.assertInstanceOf(RuleSetAssembler.class, Assembler.ruleSet);
        JenaTestLib.assertInstanceOf(DocumentManagerAssembler.class, Assembler.documentManager);
        JenaTestLib.assertInstanceOf(UnionModelAssembler.class, Assembler.unionModel);
    }

    public void testRecognisesAndAssemblesSinglePrefixMapping() {
        PrefixMapping wanted = PrefixMapping.Factory.create().setNsPrefix("P", "spoo:/");
        Resource r = resourceInModel("x ja:prefix 'P'; x ja:namespace 'spoo:/'");
        assertEquals(wanted, Assembler.general().open(r));
    }

    public void testRecognisesAndAssemblesMultiplePrefixMappings() {
        PrefixMapping wanted = PrefixMapping.Factory.create().setNsPrefix("P", "spoo:/").setNsPrefix("Q", "flarn:/");
        Resource r = resourceInModel("x ja:includes y; x ja:includes z; y ja:prefix 'P'; y ja:namespace 'spoo:/'; z ja:prefix 'Q'; z ja:namespace 'flarn:/'");
        assertEquals(wanted, Assembler.general().open(r));
    }

    public static void assertEquals(PrefixMapping wanted, Object got) {
        if ( got instanceof PrefixMapping && wanted.samePrefixMappingAs((PrefixMapping)got) )
            JenaTestLib.pass();
        else
            fail("expected " + wanted + " but was: " + got);
    }

    private void assertAssemblerClass(Resource type, Class<? > C) {
        JenaTestLib.assertInstanceOf(C, Assembler.general().assemblerFor(type));
    }
}
