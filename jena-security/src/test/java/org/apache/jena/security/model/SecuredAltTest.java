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

import com.hp.hpl.jena.rdf.model.Alt;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import java.util.Set;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluatorParameters;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.model.SecuredAlt;
import org.apache.jena.security.model.impl.SecuredAltImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( value = SecurityEvaluatorParameters.class )
public class SecuredAltTest extends SecuredContainerTest
{
	private Alt alt;

	public SecuredAltTest( final MockSecurityEvaluator securityEvaluator )
	{
		super(securityEvaluator);
	}

	private SecuredAlt getSecuredAlt()
	{
		return (SecuredAlt) getSecuredRDFNode();
	}

	@Override
	@Before
	public void setup()
	{
		super.setup();
		alt = baseModel.getAlt("http://example.com/testContainer");
		setSecuredRDFNode(SecuredAltImpl.getInstance(securedModel, alt), alt);
	}

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Test
	public void testGetDefault()
	{
		alt.add("SomeDummyItem");
		try
		{
			getSecuredAlt().getDefault();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			getSecuredAlt().getDefaultAlt();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			getSecuredAlt().getDefaultBag();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			getSecuredAlt().getDefaultSeq();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultBoolean()
	{
		alt.add(true);
		try
		{
			getSecuredAlt().getDefaultBoolean();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultByte()
	{
		alt.add(Byte.MAX_VALUE);
		try
		{
			getSecuredAlt().getDefaultByte();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultChar()
	{
		alt.add('c');
		try
		{
			getSecuredAlt().getDefaultChar();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultDouble()
	{
		alt.add(3.14d);
		try
		{
			getSecuredAlt().getDefaultDouble();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultFloat()
	{
		alt.add(3.14f);
		try
		{
			getSecuredAlt().getDefaultFloat();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultInt()
	{
		alt.add(2);
		try
		{
			getSecuredAlt().getDefaultInt();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultLanguage()
	{
		alt.add("SomeDummyItem");
		try
		{
			getSecuredAlt().getDefaultLanguage();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			getSecuredAlt().getDefaultLiteral();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testGetDefaultLong()
	{
		alt.add(3L);

		try
		{
			getSecuredAlt().getDefaultLong();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultResource()
	{
		alt.setDefault(ResourceFactory
				.createResource("http://example.com/exampleResourec"));
		try
		{
			getSecuredAlt().getDefaultResource();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	/*
	 * try
	 * {
	 * ResourceF f = ResourceFactory.getInstance();
	 * getSecuredAlt().getDefaultResource( f );
	 * if (!securityEvaluator.evaluate(Action.Read))
	 * {
	 * Assert.fail("Should have thrown AccessDenied Exception");
	 * }
	 * }
	 * catch (final AccessDeniedException e)
	 * {
	 * if (securityEvaluator.evaluate(Action.Read))
	 * {
	 * Assert.fail(String
	 * .format("Should not have thrown AccessDenied Exception: %s - %s",
	 * e, e.getTriple()));
	 * }
	 * }
	 */

	@Test
	public void testGetDefaultShort()
	{
		alt.setDefault(Short.MAX_VALUE);
		try
		{
			getSecuredAlt().getDefaultShort();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetDefaultString()
	{
		alt.setDefault("Hello World");
		try
		{
			getSecuredAlt().getDefaultString();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testSetDefaultBoolean()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredAlt().setDefault(true);
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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
	public void testSetDefaultChar()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredAlt().setDefault('c');
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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
	public void testSetDefaultDouble()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredAlt().setDefault(3.14d);
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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
	public void testSetDefaultFloat()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredAlt().setDefault(3.14f);
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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
	public void testSetDefaultLong()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredAlt().setDefault(2L);
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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
	public void testSetDefaultObject()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			final Object o = 2;
			getSecuredAlt().setDefault(o);
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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
	public void testSetDefaultResource()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredAlt().setDefault(
					ResourceFactory
							.createResource("http://example.com/resource"));
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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
	public void testSetDefaultString()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredAlt().setDefault("test");
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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
	public void testSetDefaultStringAndLang()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredAlt().setDefault("dos", "es");
			if (!securityEvaluator.evaluate(Action.Update) || (!securityEvaluator.evaluate(Action.Create) && !getSecuredAlt().iterator().hasNext() ))
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

}
