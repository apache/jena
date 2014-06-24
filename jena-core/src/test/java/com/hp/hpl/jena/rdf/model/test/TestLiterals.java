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

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.test.JenaTestBase;

import org.junit.Assert;

public class TestLiterals extends AbstractModelTestBase
{

	public TestLiterals( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	protected void assertInRange( final long min, final long x, final long max )
	{
		if ((min <= x) && (x <= max))
		{
			return;
		}
		else
		{
			Assert.fail("outside range: " + x + " min: " + min + " max: " + max);
		}
	}

	protected void assertOutsideRange( final long min, final long x,
			final long max )
	{
		if ((min <= x) && (x <= max))
		{
			Assert.fail("inside range: " + x + " min: " + min + " max: " + max);
		}
	}

	public void testBooleans()
	{
		Assert.assertTrue(model.createTypedLiteral(true).getBoolean());
		Assert.assertFalse(model.createTypedLiteral(false).getBoolean());
	}

	protected void testByte( final Model model, final byte tv )
	{
		final Literal l = model.createTypedLiteral(tv);
		Assert.assertEquals(tv, l.getByte());
		Assert.assertEquals(tv, l.getShort());
		Assert.assertEquals(tv, l.getInt());
		Assert.assertEquals(tv, l.getLong());
	}

	public void testByteLiterals()
	{
		testByte(model, (byte) 0);
		testByte(model, (byte) -1);
		testByte(model, Byte.MIN_VALUE);
		testByte(model, Byte.MAX_VALUE);
	}

	protected void testCharacter( final Model model, final char tv )
	{
		Assert.assertEquals(tv, model.createTypedLiteral(tv).getChar());
	}

	public void testCharacterLiterals()
	{
		testCharacter(model, 'A');
		testCharacter(model, 'a');
		testCharacter(model, '#');
		testCharacter(model, '@');
		testCharacter(model, '0');
		testCharacter(model, '9');
		testCharacter(model, '\u1234');
		testCharacter(model, '\u5678');
	}

	protected void testDouble( final Model model, final double tv )
	{
		Assert.assertEquals(tv, model.createTypedLiteral(tv).getDouble(),
				AbstractModelTestBase.dDelta);
	}

	public void testDoubleLiterals()
	{
		testDouble(model, 0.0);
		testDouble(model, 1.0);
		testDouble(model, -1.0);
		testDouble(model, 12345.678901);
		testDouble(model, Double.MIN_VALUE);
		testDouble(model, Double.MAX_VALUE);
	}

	protected void testFloat( final Model model, final float tv )
	{
		Assert.assertEquals(tv, model.createTypedLiteral(tv).getFloat(),
				AbstractModelTestBase.fDelta);
	}

	public void testFloatLiterals()
	{
		testFloat(model, 0.0f);
		testFloat(model, 1.0f);
		testFloat(model, -1.0f);
		testFloat(model, 12345.6789f);
		testFloat(model, Float.MIN_VALUE);
		testFloat(model, Float.MAX_VALUE);
	}

	// public void testLiteralObjects()
	// {
	// // testLiteralObject( model, 0 );
	// // testLiteralObject( model, 12345 );
	// // testLiteralObject( model, -67890 );
	// }

	protected void testInt( final Model model, final int tv )
	{
		final Literal l = model.createTypedLiteral(tv);
		try
		{
			Assert.assertEquals(tv, l.getByte());
			assertInRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
		}
        catch (final IllegalArgumentException e)
		{
			assertOutsideRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
		}
		try
		{
			Assert.assertEquals(tv, l.getShort());
			assertInRange(Short.MIN_VALUE, tv, Short.MAX_VALUE);
		}
        catch (final IllegalArgumentException e)
		{
			assertOutsideRange(Short.MIN_VALUE, tv, Short.MAX_VALUE);
		}
		Assert.assertEquals(tv, l.getInt());
		Assert.assertEquals(tv, l.getLong());
	}

	public void testIntLiterals()
	{
		testInt(model, 0);
		testInt(model, -1);
		testInt(model, Integer.MIN_VALUE);
		testInt(model, Integer.MAX_VALUE);
	}

	protected void testLanguagedString( final Model model, final String tv,
			final String lang )
	{
		final Literal l = model.createLiteral(tv, lang);
		Assert.assertEquals(tv, l.getString());
		Assert.assertEquals(tv, l.getLexicalForm());
		Assert.assertEquals(lang, l.getLanguage());
	}

	public void testLanguagedStringLiterals()
	{
		testLanguagedString(model, "", "en");
		testLanguagedString(model, "chat", "fr");
	}

	protected void testLong( final Model model, final long tv )
	{
		final Literal l = model.createTypedLiteral(tv);
		try
		{
			Assert.assertEquals(tv, l.getByte());
			assertInRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
		}
        catch (final IllegalArgumentException e)
		{
			assertOutsideRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
		}
		try
		{
			Assert.assertEquals(tv, l.getShort());
			assertInRange(Short.MIN_VALUE, tv, Short.MAX_VALUE);
		}
        catch (final IllegalArgumentException e)
		{
			assertOutsideRange(Short.MIN_VALUE, tv, Short.MAX_VALUE);
		}
		try
		{
			Assert.assertEquals(tv, l.getInt());
			assertInRange(Integer.MIN_VALUE, tv, Integer.MAX_VALUE);
		}
        catch (final IllegalArgumentException e)
		{
			assertOutsideRange(Integer.MIN_VALUE, tv, Integer.MAX_VALUE);
		}
		Assert.assertEquals(tv, l.getLong());
	}

	public void testLongLiterals()
	{
		testLong(model, 0);
		testLong(model, -1);
		testLong(model, Long.MIN_VALUE);
		testLong(model, Long.MAX_VALUE);
	}

	protected void testPlainString( final Model model, final String tv )
	{
		final Literal l = model.createLiteral(tv);
		Assert.assertEquals(tv, l.getString());
		Assert.assertEquals(tv, l.getLexicalForm());
		Assert.assertEquals("", l.getLanguage());
	}

	public void testPlainStringLiterals()
	{
		testPlainString(model, "");
		testPlainString(model, "A test string");
		testPlainString(model, "Another test string");
	}

	protected void testShort( final Model model, final short tv )
	{
		final Literal l = model.createTypedLiteral(tv);
		try
		{
			Assert.assertEquals(tv, l.getByte());
			assertInRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
		}
        catch (final IllegalArgumentException e)
		{
			assertOutsideRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
		}
		Assert.assertEquals(tv, l.getShort());
		Assert.assertEquals(tv, l.getInt());
		Assert.assertEquals(tv, l.getLong());
	}

	public void testShortLiterals()
	{
		testShort(model, (short) 0);
		testShort(model, (short) -1);
		testShort(model, Short.MIN_VALUE);
		testShort(model, Short.MAX_VALUE);
	}

	public void testStringLiteralEquality()
	{
		Assert.assertEquals(model.createLiteral("A"), model.createLiteral("A"));
		Assert.assertEquals(model.createLiteral("Alpha"),
				model.createLiteral("Alpha"));
		JenaTestBase.assertDiffer(model.createLiteral("Alpha"),
				model.createLiteral("Beta"));
		JenaTestBase.assertDiffer(model.createLiteral("A", "en"),
				model.createLiteral("A"));
		JenaTestBase.assertDiffer(model.createLiteral("A"),
				model.createLiteral("A", "en"));
		JenaTestBase.assertDiffer(model.createLiteral("A", "en"),
				model.createLiteral("A", "fr"));
		Assert.assertEquals(model.createLiteral("A", "en"),
				model.createLiteral("A", "en"));
	}

	// protected void testLiteralObject( Model model, int x )
	// {
	// LitTestObj tv = new LitTestObj( x );
	// LitTestObjF factory = new LitTestObjF();
	// assertEquals( tv, model.createTypedLiteral( tv ).getObject( factory ) );
	// }
}
