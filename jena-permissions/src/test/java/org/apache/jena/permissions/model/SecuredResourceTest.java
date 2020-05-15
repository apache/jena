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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.model.impl.SecuredResourceImpl;
import org.apache.jena.permissions.model.impl.SecuredStatementIterator;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.shared.ReadDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredResourceTest extends SecuredRDFNodeTest {

	public SecuredResourceTest(final MockSecurityEvaluator securityEvaluator) {
		super(securityEvaluator);
	}

	private SecuredResource getSecuredResource() {
		return (SecuredResource) getSecuredRDFNode();
	}

	@Override
	@Before
	public void setup() {
		super.setup();
		setSecuredRDFNode(SecuredResourceImpl.getInstance(securedModel, SecuredRDFNodeTest.s), SecuredRDFNodeTest.s);
	}

	/**
	 * True if the P property should exist
	 * 
	 * @return
	 */
	protected boolean hasP() {
		return true;
	}

	protected boolean hasP2() {
		return true;
	}

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 */
	@Test
	public void testAddLiteralBoolean() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, true);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
			}
		}
	}

	public void testAddLiteralChar() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, 'c');
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	public void testAddLiteralDouble() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, 3.14D);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	public void testAddLiteralFloat() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, 3.14F);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	public void testAddLiteral() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, ResourceFactory.createTypedLiteral("Yee haw"));
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	public void testAddLiteralLong() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, 1L);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	public void testAddLiteralObject() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

		final Object o = Integer.valueOf("1234");
		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, o);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

	}

	@Test
	public void testAddProperty() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

		final RDFNode rdfNode = ResourceFactory.createResource("http://example.com/newResource");
		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, rdfNode);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().addLiteral(SecuredRDFNodeTest.p, "string");
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		final Literal l = ResourceFactory.createTypedLiteral(3.14F);
		try {
			getSecuredResource().addProperty(SecuredRDFNodeTest.p, l.getLexicalForm(), l.getDatatype());
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().addProperty(SecuredRDFNodeTest.p, "dos", "sp");
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	@Test
	public void testAnonFuncs() {

		final SecuredResource anonResource = securedModel.createResource();
		setSecuredRDFNode(anonResource, null);

		try {
			getSecuredResource().getId();
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
	public void testAsResource() {
		getSecuredResource().asResource();
	}

	@Test
	public void testEquals() {
		try {
			getSecuredResource().equals(SecuredRDFNodeTest.s);
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

	public void testGetLocalName() {
		try {
			getSecuredResource().getLocalName();
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

	public void testGetNameSpace() {
		try {
			getSecuredResource().getNameSpace();
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
	public void testGetProperty() {
		try {
			getSecuredResource().getProperty(SecuredRDFNodeTest.p);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().getPropertyResourceValue(SecuredRDFNodeTest.p);
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
	public void testGetRequiredProperty() {
		try {
			getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		} catch (final PropertyNotFoundException e) {
			// expected if (this, "p", ANY) is not in the base securedModel.
			final StmtIterator iter = baseModel.listStatements(getSecuredResource(), SecuredRDFNodeTest.p,
					(RDFNode) null);
			try {
				if (iter.hasNext()) {
					throw e;
				}
			} finally {
				iter.close();
			}
		}

		try {
			getSecuredResource().getRequiredProperty(ResourceFactory.createProperty("http://example.com/graph/p3"));
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		} catch (final PropertyNotFoundException e) {
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown PropertyNotFoundException Exception: %s", e));
			}
		}
	}

	@Test
	public void testGetPropertyWithLang() {

		// baseModel.add(SecuredRDFNodeTest.s, SecuredRDFNodeTest.p2, "yeehaw");
		// baseModel.add(SecuredRDFNodeTest.s, SecuredRDFNodeTest.p2, "yeehaw
		// yall", "us");
		// baseModel.add(SecuredRDFNodeTest.s, SecuredRDFNodeTest.p2, "whohoo",
		// "uk");

		try {
			SecuredStatement result = getSecuredResource().getProperty(SecuredRDFNodeTest.p2, "");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			if (hasP2()) {
				assertEquals("yeehaw", result.getObject().asLiteral().getString());
			} else {
				assertNull(result);
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			SecuredStatement result = getSecuredResource().getProperty(SecuredRDFNodeTest.p2, "us");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			if (hasP2()) {
				assertEquals("yeehaw yall", result.getObject().asLiteral().getString());
			} else {
				assertNull(result);
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			SecuredStatement result = getSecuredResource().getProperty(SecuredRDFNodeTest.p2, "uk");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			if (hasP2()) {
				assertEquals("whohoo", result.getObject().asLiteral().getString());
			} else {
				assertNull(result);
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			SecuredStatement result = getSecuredResource().getProperty(SecuredRDFNodeTest.p2, "non");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			assertNull("Should have been null", result);
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	@Test
	public void testGetRequiredPropertyWithLang() {
		try {
			SecuredStatement result = getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p2, "");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}

			assertEquals("yeehaw", result.getObject().asLiteral().getString());

		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		} catch (final PropertyNotFoundException e) {
			if (hasP2()) {
				Assert.fail("Should not have thrown PropertyNotFoundException");
				;
			}
		}

		try {
			SecuredStatement result = getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p2, "us");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			assertEquals("yeehaw yall", result.getObject().asLiteral().getString());
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		} catch (final PropertyNotFoundException e) {
			if (hasP2()) {
				Assert.fail("Should not have thrown PropertyNotFoundException");
				;
			}
		}

		try {
			SecuredStatement result = getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p2, "uk");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			assertEquals("whohoo", result.getObject().asLiteral().getString());
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		} catch (final PropertyNotFoundException e) {
			if (hasP2()) {
				Assert.fail("Should not have thrown PropertyNotFoundException");
				;
			}
		}

		try {
			getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p2, "non");
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		} catch (final PropertyNotFoundException e) {
			// expected if we can read
			if (!securityEvaluator.evaluate(Action.Read)) {
				throw e;
			}

		}
	}

	@Test
	public void testGetURI() {
		try {
			getSecuredResource().getURI();
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
	public void testHasLiteral() {
		try {
			getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, true);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 'c');
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 3.14d);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 3.14f);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 6l);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		final Object o = 6;
		try {
			getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, o);
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
	public void testHasProperty() {

		try {
			getSecuredResource().hasProperty(SecuredRDFNodeTest.p);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().hasProperty(SecuredRDFNodeTest.p, SecuredRDFNodeTest.o);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().hasProperty(SecuredRDFNodeTest.p, "yeee haw");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			getSecuredResource().hasProperty(SecuredRDFNodeTest.p, "dos", "sp");
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
	public void testHasURI() {
		try {
			getSecuredResource().hasURI("http://example.com/yeeHaw");
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
	public void testListProperties() {
		try {
			SecuredStatementIterator iter = getSecuredResource().listProperties();
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			assertTrue(iter.hasNext());
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			SecuredStatementIterator iter = getSecuredResource().listProperties(SecuredRDFNodeTest.p);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			if (hasP()) {
				assertTrue(iter.hasNext());
			} else {
				assertFalse(iter.hasNext());
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	@Test
	public void testListPropertiesWithLang() {
		try {
			SecuredStatementIterator iter = getSecuredResource().listProperties(SecuredRDFNodeTest.p2, "");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			if (hasP2()) {
				assertTrue(iter.hasNext());
				Statement stmt = iter.next();
				assertEquals("yeehaw", stmt.getObject().asLiteral().getString());
			}
			assertFalse(iter.hasNext());
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			SecuredStatementIterator iter = getSecuredResource().listProperties(SecuredRDFNodeTest.p2, "us");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			if (hasP2()) {
				assertTrue(iter.hasNext());
				Statement stmt = iter.next();
				assertEquals("yeehaw yall", stmt.getObject().asLiteral().getString());
			}
			assertFalse(iter.hasNext());
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		try {
			SecuredStatementIterator iter = getSecuredResource().listProperties(SecuredRDFNodeTest.p2, "uk");
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
			if (hasP2()) {
				assertTrue(iter.hasNext());
				Statement stmt = iter.next();
				assertEquals("whohoo", stmt.getObject().asLiteral().getString());
			}
			assertFalse(iter.hasNext());
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}
	}

	@Test
	public void testRemoveAll() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });
		final int count = baseModel.listStatements(getBaseRDFNode().asResource(), SecuredRDFNodeTest.p, (RDFNode) null)
				.toSet().size();

		try {
			getSecuredResource().removeAll(SecuredRDFNodeTest.p);
			// only throw on delete if count > 0
			if (!securityEvaluator.evaluate(Action.Update)
					|| ((count > 0) && !securityEvaluator.evaluate(Action.Delete))) {
				Assert.fail("Should have thrown AccessDeniedException");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
			}
		}
	}

	@Test
	public void testRemoveProperties() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });
		final int count = baseModel.listStatements(getBaseRDFNode().asResource(), SecuredRDFNodeTest.p, (RDFNode) null)
				.toSet().size();

		try {
			getSecuredResource().removeProperties();
			// only throw on delete if count > 0
			if (!securityEvaluator.evaluate(Action.Update)
					|| ((count > 0) && !securityEvaluator.evaluate(Action.Delete))) {
				Assert.fail("Should have thrown AccessDeniedException");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
			}
		}
	}
}
