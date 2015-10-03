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

import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.model.SecuredReifiedStatement;
import org.apache.jena.permissions.model.impl.SecuredReifiedStatementImpl;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.shared.ReadDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredReifiedStatementTest extends SecuredResourceTest {

	public SecuredReifiedStatementTest(
			final MockSecurityEvaluator securityEvaluator) {
		super(securityEvaluator);
	}

	private SecuredReifiedStatement getSecuredReifiedStatement() {
		return (SecuredReifiedStatement) getSecuredRDFNode();
	}

	@Override
	@Before
	public void setup() {
		super.setup();
		final ReifiedStatement stmt = baseModel.listStatements().next()
				.createReifiedStatement();
		setSecuredRDFNode(
				SecuredReifiedStatementImpl.getInstance(securedModel, stmt),
				stmt);
	}

	@Test
	public void testGetStatement() {
		try {
			getSecuredReifiedStatement().getStatement();
			if (!securityEvaluator.evaluate(Action.Read)) {
				Assert.fail("Should have thrown ReadDeniedException Exception");
			}
		} catch (final ReadDeniedException e) {
			if (securityEvaluator.evaluate(Action.Read)) {
				Assert.fail(String
						.format("Should not have thrown ReadDeniedException Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}
}
