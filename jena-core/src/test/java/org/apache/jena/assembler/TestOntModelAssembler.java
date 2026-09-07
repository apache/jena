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

import java.util.List;

import org.apache.jena.assembler.assemblers.*;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.test.JenaTestLib;

@SuppressWarnings("removal")
public class TestOntModelAssembler extends AssemblerTestBase {

    @Override
    protected Class<? extends Assembler> getAssemblerClass() {
        return OntModelAssembler.class;
    }

    @Test
    public void testOntModelAssemblerType() {
        testDemandsMinimalType(new OntModelAssembler(), JA.OntModel);
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
    public void testBuiltinSpec(OntModelSpec spec, String name) {
        Assembler a = new OntModelAssembler();
        Model m = (Model)a.open(new FixedObjectAssembler(spec),
                                resourceInModel("x rdf:type ja:OntModel; x ja:ontModelSpec ja:" + name));
        JenaTestLib.assertInstanceOf(OntModel.class, m);
        OntModel om = (OntModel)m;
        assertSame(spec, om.getSpecification());
    }

    @Test
    public void testAllDefaults() {
        Assembler a = new OntModelAssembler();
        Model m = a.openModel(resourceInModel("x rdf:type ja:OntModel"));
        JenaTestLib.assertInstanceOf(OntModel.class, m);
        OntModel om = (OntModel)m;
        assertSame(OntModelSpec.OWL_MEM_RDFS_INF, om.getSpecification());
    }

    @Test
    public void testBaseModel() {
        final Model baseModel = model("a P b");
        Assembler a = new OntModelAssembler();
        Assembler aa = new ModelAssembler() {
            @Override
            protected Model openEmptyModel(Assembler a, Resource root, Mode irrelevant) {
                assertEquals(ModelTestLib.resource("y"), root);
                return baseModel;
            }
        };
        Object m = a.open(aa, resourceInModel("x rdf:type ja:OntModel; x ja:baseModel y"));
        JenaTestLib.assertInstanceOf(OntModel.class, m);
        OntModel om = (OntModel)m;
        assertSame(baseModel.getGraph(), om.getBaseModel().getGraph());
    }

    @Test
    public void testSubModels() {
        final Model baseModel = model("a P b");
        Assembler a = new OntModelAssembler();
        Assembler aa = new ModelAssembler() {
            @Override
            protected Model openEmptyModel(Assembler a, Resource root, Mode irrelevant) {
                assertEquals(ModelTestLib.resource("y"), root);
                return baseModel;
            }
        };
        Object m = a.open(aa, resourceInModel("x rdf:type ja:OntModel; x ja:subModel y"));
        JenaTestLib.assertInstanceOf(OntModel.class, m);
        OntModel om = (OntModel)m;
        List<OntModel> subModels = om.listSubModels().toList();
        assertEquals(1, subModels.size());
        assertSame(baseModel.getGraph(), subModels.get(0).getBaseModel().getGraph());
    }

    @Test
    public void testDefaultDocumentManager() {
        Assembler a = new OntModelAssembler();
        Resource root = resourceInModel("x rdf:type ja:OntModel");
        OntModel om = (OntModel)a.openModel(root);
        assertSame(OntDocumentManager.getInstance(), om.getDocumentManager());
    }

    @Test
    public void testUsesOntModelSpec() {
        Assembler a = new OntModelAssembler();
        Resource root = resourceInModel("x rdf:type ja:OntModel; x ja:ontModelSpec y");
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        Assembler mock = new NamedObjectAssembler(ModelTestLib.resource("y"), spec);
        OntModel om = (OntModel)a.open(mock, root);
        assertSame(spec, om.getSpecification());
    }
}
