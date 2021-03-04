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
package org.apache.jena.permissions;

import java.net.URL;

import org.junit.Assert;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.Test;

public class SecuredAssemblerTest {
    private Assembler assembler;
    private Model model;

    public SecuredAssemblerTest() {
        assembler = Assembler.general;
    }

    @Before
    public void setUp() throws Exception {
        model = ModelFactory.createDefaultModel();
        URL url = SecuredAssemblerTest.class.getClassLoader()
                .getResource(SecuredAssemblerTest.class.getName().replace(".", "/") + ".ttl");
        model.read(url.toURI().toString(), "TURTLE");
    }

    @Test
    public void testCreation() throws Exception {

        Resource r = model.createResource("http://apache.org/jena/permissions/test#secModel");
        Object o = assembler.open(r);
        Assert.assertTrue(o instanceof Model);
        Assert.assertTrue(o instanceof SecuredModel);
    }

    @Test
    public void testCreationWithArgs() throws Exception {

        Resource r = model.createResource("http://apache.org/jena/permissions/test#secModel2");
        Object o = assembler.open(r);
        Assert.assertTrue(o instanceof Model);
        Assert.assertTrue(o instanceof SecuredModel);
    }

    @Test
    public void testSecurityEvaluatorWithStringArgs() throws Exception {

        Resource r = model.createResource("http://apache.org/jena/permissions/test#secEvaluator");
        Object o = assembler.open(r);
        Assert.assertTrue(o instanceof SecurityEvaluator);
        Assert.assertTrue(o instanceof StaticSecurityEvaluator);
    }

    @Test
    public void testSecurityEvaluatorWithModelArgs() throws Exception {

        Resource r = model.createResource("http://apache.org/jena/permissions/test#secEvaluator2");
        Object o = assembler.open(r);
        Assert.assertTrue(o instanceof SecurityEvaluator);
        Assert.assertTrue(o instanceof ModelBasedSecurityEvaluator);
    }
}
