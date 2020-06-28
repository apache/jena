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

import java.util.Set;

import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.model.impl.SecuredContainerImpl;
import org.apache.jena.rdf.model.Container;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public abstract class SecuredContainerTest extends SecuredResourceTest {

	public SecuredContainerTest(final MockSecurityEvaluator securityEvaluator) {
		super(securityEvaluator);
	}

	private SecuredContainer getSecuredContainer() {
		return (SecuredContainer) getSecuredRDFNode();
	}

	@Override
	protected final boolean hasP() {
		return false;
	}

	@Override
	protected final boolean hasP2() {
		return false;
	}

	@Override
	@Before
	public void setup() {
		super.setup();
		final Container container = baseModel.getBag("http://example.com/testContainer");
		container.add("SomeDummyItem");
		setSecuredRDFNode(SecuredContainerImpl.getInstance(securedModel, container), container);
	}

	@Test
	public void test() {
		try {
			getSecuredContainer().size();
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

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 */
	@Test
	public void testAdd() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
		try {
			getSecuredContainer().add(true);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
			}
		}

		try {
			getSecuredContainer().add('c');
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
			getSecuredContainer().add(3.14D);
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
			getSecuredContainer().add(3.14F);
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
			getSecuredContainer().add(2L);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException Exception");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		final Object o = Integer.valueOf("1234");
		try {
			getSecuredContainer().add(o);
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
			getSecuredContainer().add(ResourceFactory.createResource("http://example.com/testResource"));
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
			getSecuredContainer().add("foo");
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
			getSecuredContainer().add("dos", "esp");
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
	public void testContains() {
		try {
			getSecuredContainer().contains(true);
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
			getSecuredContainer().contains('c');
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
			getSecuredContainer().contains(3.14D);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown AccessDenied Exception: %s - %s", e, e.getTriple()));
			}
		}

		try {
			getSecuredContainer().contains(3.14F);
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
			getSecuredContainer().contains(2L);
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
						e.getTriple()));
			}
		}

		final Object o = Integer.valueOf("1234");
		try {
			getSecuredContainer().contains(o);
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
			getSecuredContainer().contains(ResourceFactory.createResource("http://example.com/testResource"));
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String.format("Should not have thrown AccessDenied Exception: %s - %s", e, e.getTriple()));
			}
		}

		try {
			getSecuredContainer().contains("foo");
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
			getSecuredContainer().contains("dos", "esp");
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
	public void testIterator() {
		try {
			getSecuredContainer().iterator();
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
	public void testRemove() {
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });
		final Statement s = baseModel.listStatements().next();
		try {
			getSecuredContainer().remove(s);
			if (!securityEvaluator.evaluate(perms)) {
				Assert.fail("Should have thrown AccessDeniedException");
			}
		} catch (final AccessDeniedException e) {
			if (securityEvaluator.evaluate(perms)) {
				Assert.fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
			}
		}

	}

}
