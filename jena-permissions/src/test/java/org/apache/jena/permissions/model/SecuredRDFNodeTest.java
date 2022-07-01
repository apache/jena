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
package org.apache.jena.permissions.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.jena.permissions.Factory;
import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.model.impl.SecuredRDFNodeImpl;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.ReadDeniedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredRDFNodeTest {
    protected final MockSecurityEvaluator securityEvaluator;
    protected Model baseModel;
    protected SecuredModel securedModel;
    private SecuredRDFNode securedRDFNode;
    private RDFNode baseRDFNode;

    public static Resource s = ResourceFactory.createResource("http://example.com/graph/s");
    public static Property p = ResourceFactory.createProperty("http://example.com/graph/p");
    public static Property p2 = ResourceFactory.createProperty("http://example.com/graph/p2");
    public static Resource o = ResourceFactory.createResource("http://example.com/graph/o");

    public SecuredRDFNodeTest(final MockSecurityEvaluator securityEvaluator) {
        this.securityEvaluator = securityEvaluator;
    }

    protected Model createModel() {
        return ModelFactory.createDefaultModel();
    }

    protected RDFNode getBaseRDFNode() {
        return baseRDFNode.inModel(baseModel);
    }

    protected SecuredRDFNode getSecuredRDFNode() {
        return securedRDFNode;
    }

    protected void setSecuredRDFNode(final SecuredRDFNode securedRDFNode, final RDFNode baseRDFNode) {
        this.securedRDFNode = securedRDFNode;
        this.baseRDFNode = baseRDFNode;
    }

    protected boolean shouldRead() {
        return securityEvaluator.evaluate(Action.Read) || !securityEvaluator.isHardReadError();
    }

    protected void addSPO(Resource node) {
        baseModel.add(node, SecuredRDFNodeTest.p, SecuredRDFNodeTest.o);
        baseModel.add(node, SecuredRDFNodeTest.p2, "yeehaw");
        baseModel.add(node, SecuredRDFNodeTest.p2, "yeehaw yall", "us");
        baseModel.add(node, SecuredRDFNodeTest.p2, "whohoo", "uk");
    }

    @Before
    public void setup() {
        baseModel = createModel();
        baseModel.removeAll();
        addSPO(SecuredRDFNodeTest.s);
        securedModel = Factory.getInstance(securityEvaluator, "http://example.com/securedGraph", baseModel);
        baseRDFNode = baseModel.getResource(SecuredRDFNodeTest.o.getURI());
        securedRDFNode = SecuredRDFNodeImpl.getInstance(securedModel, baseRDFNode);
    }

    @After
    public void teardown() {
        securedModel.close();
        securedModel = null;
    }

    @Test
    public void testAsNode() {
        try {
            securedRDFNode.asNode();
            if (!securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testCanAs() {
        boolean actual = securedRDFNode.canAs(Resource.class);
        if (!securityEvaluator.evaluate(Action.Read)) {
            assertFalse(actual);
        } else {
            assertEquals(baseRDFNode.canAs(Resource.class), actual);
        }
    }

    @Test
    public void testGetModel() {
        final Model m2 = securedRDFNode.getModel();
        Assert.assertTrue("Model should have been secured", m2 instanceof SecuredModel);
    }

    @Test
    public void testInModel() {
        final Model m2 = ModelFactory.createDefaultModel();
        try {
            final RDFNode n2 = securedRDFNode.inModel(m2);
            if (!securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            Assert.assertFalse("RDFNode should not have been secured", n2 instanceof SecuredRDFNode);
            Assert.assertEquals("Wrong securedModel returned", n2.getModel(), m2);
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        m2.removeAll();
        final SecuredModel m3 = Factory.getInstance(securityEvaluator, "http://example.com/securedGraph2", m2);

        try {
            final RDFNode n2 = securedRDFNode.inModel(m3);
            if (!securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            Assert.assertTrue("RDFNode should have been secured", n2 instanceof SecuredRDFNode);
            Assert.assertEquals("Wrong securedModel returned", n2.getModel(), m3);
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

}
