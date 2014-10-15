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
package org.apache.jena.security.model;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PropertyNotFoundException;

import java.util.Set;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.Factory;
import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluatorParameters;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.model.SecuredStatement;
import org.apache.jena.security.model.impl.SecuredStatementImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( value = SecurityEvaluatorParameters.class )
public class SecuredStatementTest
{
	private final MockSecurityEvaluator securityEvaluator;
	private Statement baseStatement;
	private SecuredStatement securedStatement;
	private Model baseModel;
	private SecuredModel securedModel;
	private Property property;

	public SecuredStatementTest( final MockSecurityEvaluator securityEvaluator )
	{
		this.securityEvaluator = securityEvaluator;
	}

	protected Model createModel()
	{
		return ModelFactory.createDefaultModel();
	}

	@Before
	public void setup()
	{
		baseModel = createModel();
		property = ResourceFactory
				.createProperty("http://example.com/property");
		baseModel.add(ResourceFactory.createResource(), property,
				ResourceFactory.createResource());
		baseStatement = baseModel.listStatements().next();
		securedModel = Factory.getInstance(securityEvaluator,
				"http://example.com/securedModel", baseModel);
		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement);
	}

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Test
	public void testChangeLiteralObject()
	{
		final Set<Action> perms = SecurityEvaluator.Util
				.asSet(new Action[] { Action.Update });
		try
		{
			securedStatement.changeLiteralObject(true);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeLiteralObject('c');
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeLiteralObject(3.14d);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeLiteralObject(3.14F);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeLiteralObject(2);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeLiteralObject(2L);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeObject(ResourceFactory.createResource());
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeObject("Waaa hooo");
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			final Literal l = ResourceFactory
					.createTypedLiteral(Integer.MAX_VALUE);
			securedStatement.changeObject(l.getLexicalForm(), true);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeObject("dos", "es");
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.changeObject("dos", "es", false);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testCreateReifiedStatement()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });

		try
		{
			securedStatement.createReifiedStatement();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedStatement.createReifiedStatement("http://example.com/rsURI");
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetProperty()
	{
		// get property of the object
		baseModel.add(baseStatement.getObject().asResource(), property,
				ResourceFactory.createResource());
		try
		{
			securedStatement.getProperty(property);
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown PropertyNotFound Exception");
			}
		}
		catch (final PropertyNotFoundException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown PropertyNotFound Exception: %s - %s",
								e, securityEvaluator));
			}
		}
	}

	@Test
	public void testGets()
	{
		final Set<Action> perms = SecurityEvaluator.Util
				.asSet(new Action[] { Action.Read });

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject(true));
		try
		{
			securedStatement.getBoolean();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject(Byte.MAX_VALUE));
		try
		{
			securedStatement.getByte();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject('c'));
		try
		{
			securedStatement.getChar();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject(3.14d));
		try
		{
			securedStatement.getDouble();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject(3.14F));
		try
		{
			securedStatement.getFloat();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject(2));
		try
		{
			securedStatement.getInt();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeObject("dos", "es"));
		try
		{
			securedStatement.getLanguage();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject(2L));
		try
		{
			securedStatement.getLong();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject(Short.MAX_VALUE));
		try
		{
			securedStatement.getShort();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeObject("who hoo"));
		try
		{
			securedStatement.getString();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeObject("who hoo"));
		try
		{
			securedStatement.hasWellFormedXML();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetStatementProperty()
	{
		// get property of the subject
		final ReifiedStatement s = baseStatement.createReifiedStatement();
		s.addLiteral(property, "yee haw");
		securedStatement.getStatementProperty(property);

	}

	@Test
	public void testIsReified()
	{
		final Set<Action> perms = SecurityEvaluator.Util
				.asSet(new Action[] { Action.Read });

		try
		{
			securedStatement.isReified();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testListReifiedStatements()
	{
		final Set<Action> perms = SecurityEvaluator.Util
				.asSet(new Action[] { Action.Read });

		try
		{
			securedStatement.listReifiedStatements();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testRemove()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Delete });

		try
		{
			securedStatement.remove();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testRemoveReification()
	{
		baseStatement.createReifiedStatement();
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Delete });

		try
		{
			securedStatement.removeReification();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testUnsecuredGets()
	{
		securedStatement.getAlt();
		securedStatement.getBag();
		securedStatement.getSeq();

		securedStatement.getResource();
		// securedStatement.getResource( ResourceF f );
		securedStatement.getSubject();

		securedStatement = SecuredStatementImpl.getInstance(securedModel,
				baseStatement.changeLiteralObject(true));
		securedStatement.getLiteral();
	}

}
