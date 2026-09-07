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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.junit.jupiter.api.Test;

import org.apache.jena.assembler.assemblers.*;
import org.apache.jena.assembler.exceptions.ReasonerClashException;
import org.apache.jena.ontology.*;
import org.apache.jena.ontology.models.ModelGetter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.*;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.shared.CannotCreateException;
import org.apache.jena.test.JenaTestLib;

@SuppressWarnings("removal")
public class TestOntModelSpecAssembler extends AssemblerTestBase {

    @Override
    protected Class<? extends Assembler> getAssemblerClass() {
        return OntModelSpecAssembler.class;
    }

    @Test
    public void testOntModelSpecAssemblerType() {
        testDemandsMinimalType(new OntModelSpecAssembler(), JA.OntModelSpec);
    }

    /** One case per public {@code OntModelSpec} constant - was addParameterisedTests(). */
    static Stream<Arguments> builtinSpecs() {
        return Arrays.stream(OntModelSpec.class.getFields())
                     .filter(f->f.getType() == OntModelSpec.class)
                     .map(f->{
                         try {
                             return Arguments.of(Named.of(f.getName(), (OntModelSpec)f.get(null)), f.getName());
                         } catch (IllegalAccessException e) {
                             throw new RuntimeException(e);
                         }
                     });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("builtinSpecs")
    public void testBuiltinSpec(OntModelSpec ontModelSpec, String specName) {
        testBuiltinSpecAsRootName(ontModelSpec, specName);
        testBuiltinSpecAsLikeTarget(ontModelSpec, specName);
    }

    private void testBuiltinSpecAsLikeTarget(OntModelSpec ontModelSpec, String specName) {
        Resource rr = resourceInModel("_x rdf:type ja:OntModelSpec; _x ja:likeBuiltinSpec <OM>".replaceAll("<OM>", JA.getURI() + specName));
        assertEquals(ontModelSpec, new OntModelSpecAssembler().open(rr));
    }

    private void testBuiltinSpecAsRootName(OntModelSpec ontModelSpec, String specName) {
        Resource root = resourceInModel(JA.getURI() + specName + " rdf:type ja:OntModelSpec");
        assertEquals(ontModelSpec, new OntModelSpecAssembler().open(root));
    }

    @Test
    public void testOntModelSpecVocabulary() {
        assertDomain(JA.OntModelSpec, JA.ontLanguage);
        assertDomain(JA.OntModelSpec, JA.documentManager);
        assertDomain(JA.OntModelSpec, JA.likeBuiltinSpec);
    }

    @Test
    public void testCreateFreshDocumentManager() {
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel("x rdf:type ja:OntModelSpec; x ja:documentManager y");
        OntDocumentManager dm = new OntDocumentManager();
        NamedObjectAssembler mock = new NamedObjectAssembler(ModelTestLib.resource("y"), dm);
        OntModelSpec om = (OntModelSpec)a.open(mock, root);
        assertSame(dm, om.getDocumentManager());
    }

    @Test
    public void testUseSpecifiedReasoner() {
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel("x rdf:type ja:OntModelSpec; x ja:reasonerFactory R");
        ReasonerFactory rf = new FakeReasonerFactory();
        NamedObjectAssembler mock = new NamedObjectAssembler(ModelTestLib.resource("R"), rf);
        OntModelSpec om = (OntModelSpec)a.open(mock, root);
        assertSame(rf, om.getReasonerFactory());
    }

    @Test
    public void testUseSpecifiedImpliedReasoner() {
        testUsedSpecifiedImpliedReasoner(OWLFBRuleReasonerFactory.URI);
        testUsedSpecifiedImpliedReasoner(RDFSRuleReasonerFactory.URI);
    }

    private void testUsedSpecifiedImpliedReasoner(String R) {
        ReasonerFactory rf = ReasonerFactoryAssembler.getReasonerFactoryByURL(ModelTestLib.resource("x"), ModelTestLib.resource(R));
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel("x rdf:type ja:OntModelSpec; x ja:reasonerURL " + R);
        Assembler mock = new FixedObjectAssembler("(should not be this string)");
        OntModelSpec om = (OntModelSpec)a.open(mock, root);
        assertSame(rf, om.getReasonerFactory());
    }

    @Test
    public void testDetectsClashingImpliedAndExplicitReasoners() {
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel("x rdf:type ja:OntModelSpec; x ja:reasonerURL R; x ja:reasonerFactory F");
        Assembler mock = new FixedObjectAssembler("(should not be this string)");
        try {
            a.open(mock, root);
            fail("should detect reasoner clash");
        } catch (ReasonerClashException e) {
            JenaTestLib.pass();
        }
    }

    @Test
    public void testUseSpecifiedLanguage() {
        testSpecifiedLanguage(ProfileRegistry.OWL_DL_LANG);
        testSpecifiedLanguage(ProfileRegistry.OWL_LANG);
        testSpecifiedLanguage(ProfileRegistry.OWL_LITE_LANG);
        testSpecifiedLanguage(ProfileRegistry.RDFS_LANG);
    }

    private void testSpecifiedLanguage(String lang) {
        Assembler a = new OntModelSpecAssembler();
        Resource root = resourceInModel("x rdf:type ja:OntModelSpec; x ja:ontLanguage " + lang);
        OntModelSpec om = (OntModelSpec)a.open(root);
        assertEquals(lang, om.getLanguage());
    }

    @Test
    public void testSpecifiedModelGetter() {
        Assembler a = new OntModelSpecAssembler();
        ModelGetter getter = new ModelGetter() {
            @Override
            public Model getModel(String URL) {
                return null;
            }

            @Override
            public Model getModel(String URL, ModelReader loadIfAbsent) {
                throw new CannotCreateException(URL);
            }
        };
        NamedObjectAssembler mock = new NamedObjectAssembler(ModelTestLib.resource("source"), getter);
        Resource root = resourceInModel("x rdf:type ja:OntModelSpec; x ja:importSource source");
        OntModelSpec om = (OntModelSpec)a.open(mock, root);
        assertSame(getter, om.getImportModelGetter());
    }

    private static final class FakeReasonerFactory implements ReasonerFactory {
        @Override
        public Reasoner create(Resource configuration) {
            return null;
        }

        @Override
        public Model getCapabilities() {
            return null;
        }

        @Override
        public String getURI() {
            return null;
        }
    }
}
