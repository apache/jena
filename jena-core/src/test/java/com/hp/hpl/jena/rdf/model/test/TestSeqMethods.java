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

import com.hp.hpl.jena.rdf.model.Alt;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.SeqIndexBoundsException;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import junit.framework.TestSuite;

public class TestSeqMethods extends AbstractContainerMethods
{
	public static TestSuite suite()
	{
		return new TestSuite(TestSeqMethods.class);
	}

	protected LitTestObj aLitTestObj;

	protected Literal tvLiteral;

	protected Resource tvResource;

	// protected Resource tvResObj;
	protected Object anObject;

	protected Bag tvBag;
	protected Alt tvAlt;
	protected Seq tvSeq;
	protected static final String lang = "fr";
	protected static final int num = 10;

	public TestSeqMethods( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	protected boolean[] bools( final String s )
	{
		final boolean[] result = new boolean[s.length()];
		for (int i = 0; i < s.length(); i += 1)
		{
			result[i] = s.charAt(i) == 't';
		}
		return result;
	}

	@Override
	protected Container createContainer()
	{
		return model.createSeq();
	}

	public void error( final String test, final int n )
	{
		Assert.fail(test + " -- " + n);
	}

	@Override
	protected Resource getContainerType()
	{
		return RDF.Seq;
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		aLitTestObj = new LitTestObj(12345);
		tvLiteral = model.createLiteral("test 12 string 2");
		tvResource = model.createResource();
		// tvResObj = model.createResource( new ResTestObjF() );
		anObject = new LitTestObj(1234);
		tvBag = model.createBag();
		tvAlt = model.createAlt();
		tvSeq = model.createSeq();
	}

	public void testMoreIndexing()
	{
		final int num = 10;
		final Seq seq = model.createSeq();
		for (int i = 0; i < num; i += 1)
		{
			seq.add(i);
		}

		try
		{
			seq.add(0, false);
			Assert.fail("cannot at at position 0");
		}
		catch (final SeqIndexBoundsException e)
		{
			JenaTestBase.pass();
		}

		seq.add(num + 1, false);
		Assert.assertEquals(num + 1, seq.size());

		seq.remove(num + 1);
		try
		{
			seq.add(num + 2, false);
			Assert.fail("cannot add past the end");
		}
		catch (final SeqIndexBoundsException e)
		{
			JenaTestBase.pass();
		}

		final int size = seq.size();
		for (int i = 1; i <= (num - 1); i += 1)
		{
			seq.add(i, 1000 + i);
			Assert.assertEquals(1000 + i, seq.getInt(i));
			Assert.assertEquals(0, seq.getInt(i + 1));
			Assert.assertEquals(size + i, seq.size());
			Assert.assertEquals(num - i - 1, seq.getInt(size));
		}
	}

	protected void testRemove( final boolean[] retain )
	{
		final int num = retain.length;
		final Seq seq = model.createSeq();
		for (int i = 0; i < num; i += 1)
		{
			seq.add(i);
		}
		//
		final List<RDFNode> retained = new ArrayList<>();
		//
		final NodeIterator nIter = seq.iterator();
        for ( boolean aRetain : retain )
        {
            final RDFNode x = nIter.nextNode();
            if ( aRetain )
            {
                retained.add( x );
            }
            else
            {
                nIter.remove();
            }
        }
		//
		Assert.assertFalse(nIter.hasNext());
		Assert.assertEquals(retained, seq.iterator().toList());
	}

	public void testRemoveA()
	{
		testRemove(bools("tttffffftt"));
	}

	public void testRemoveB()
	{
		testRemove(bools("ftftttttft"));
	}

	public void testRemoveC()
	{
		testRemove(bools("ffffffffff"));
	}

	public void testSeq4()
	{
		final String test = "temp";
		int n = 58305;
		final Seq seq4 = model.createSeq();
		n = ((n / 100) * 100) + 100;
		n++;
		seq4.add(AbstractModelTestBase.tvBoolean);
		n++;
		if (!(seq4.getBoolean(1) == AbstractModelTestBase.tvBoolean))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvByte);
		n++;
		if (!(seq4.getByte(2) == AbstractModelTestBase.tvByte))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvShort);
		n++;
		if (!(seq4.getShort(3) == AbstractModelTestBase.tvShort))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvInt);
		n++;
		if (!(seq4.getInt(4) == AbstractModelTestBase.tvInt))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvLong);
		n++;
		if (!(seq4.getLong(5) == AbstractModelTestBase.tvLong))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvChar);
		n++;
		if (!(seq4.getChar(6) == AbstractModelTestBase.tvChar))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvFloat);
		n++;
		if (!(seq4.getFloat(7) == AbstractModelTestBase.tvFloat))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvDouble);
		n++;
		if (!(seq4.getDouble(8) == AbstractModelTestBase.tvDouble))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvString);
		n++;
		if (!(seq4.getString(9).equals(AbstractModelTestBase.tvString)))
		{
			error(test, n);
		}
		n++;
		if (!(seq4.getLanguage(9).equals("")))
		{
			error(test, n);
		}
		n++;
		seq4.add(AbstractModelTestBase.tvString, TestSeqMethods.lang);
		n++;
		if (!(seq4.getString(10).equals(AbstractModelTestBase.tvString)))
		{
			error(test, n);
		}
		n++;
		if (!(seq4.getLanguage(10).equals(TestSeqMethods.lang)))
		{
			error(test, n);
		}
		n++;
		seq4.add(anObject);
		n++;
		// if (!(seq4.getObject( 11, new LitTestObjF() ).equals( anObject )))
		// error(
		// test, n );
		n++;
		seq4.add(tvResource);
		n++;
		if (!(seq4.getResource(12).equals(tvResource)))
		{
			error(test, n);
		}
		n++;
		seq4.add(tvLiteral);
		n++;
		if (!(seq4.getLiteral(13).equals(tvLiteral)))
		{
			error(test, n);
		}
		n++;
		// seq4.add( tvResObj );
		// n++;
		// if (!(seq4.getResource( 14, new ResTestObjF() ).equals( tvResObj )))
		// error(
		// test, n );
		n++;
		seq4.add(tvBag);
		n++;
		if (!(seq4.getBag(14).equals(tvBag)))
		{
			error(test, n);
		}
		n++;
		seq4.add(tvAlt);
		n++;
		if (!(seq4.getAlt(15).equals(tvAlt)))
		{
			error(test, n);
		}
		n++;
		seq4.add(tvSeq);
		n++;
		if (!(seq4.getSeq(16).equals(tvSeq)))
		{
			error(test, n);
		}
		n++;
		try
		{
			seq4.getInt(17);
			error(test, n);
		}
		catch (final SeqIndexBoundsException e)
		{
			// as required
		}
		n++;
		try
		{
			seq4.getInt(0);
			error(test, n);
		}
		catch (final SeqIndexBoundsException e)
		{
			// as required
		}
	}

	public void testSeq5()
	{
		final Seq seq5 = model.createSeq();
		final String test = "seq5";
		int n = 0;
		for (int i = 0; i < TestSeqMethods.num; i++)
		{
			seq5.add(i);
		}

		try
		{
			n++;
			seq5.add(0, false);
			error(test, n);
		}
		catch (final SeqIndexBoundsException e)
		{
			// as required
		}
		seq5.add(TestSeqMethods.num + 1, false);
		if (seq5.size() != (TestSeqMethods.num + 1))
		{
			error(test, n);
		}
		seq5.remove(TestSeqMethods.num + 1);
		try
		{
			n++;
			seq5.add(TestSeqMethods.num + 2, false);
			error(test, n);
		}
		catch (final SeqIndexBoundsException e)
		{
			// as required
		}

		n = ((n / 100) * 100) + 100;
		final int size = seq5.size();
		for (int i = 1; i <= (TestSeqMethods.num - 1); i++)
		{
			n++;
			seq5.add(i, 1000 + i);
			n++;
			if (!(seq5.getInt(i) == (1000 + i)))
			{
				error(test, n);
			}
			n++;
			if (!(seq5.getInt(i + 1) == 0))
			{
				error(test, n);
			}
			n++;
			if (!(seq5.size() == (size + i)))
			{
				error(test, n);
			}
			n++;
			if (!(seq5.getInt(size) == (TestSeqMethods.num - i - 1)))
			{
				error(test, n);
			}
		}
	}

	public void testSeq6()
	{
		final String test = "seq6";
		int n = 0;
		final Seq seq6 = model.createSeq();
		seq6.add(model.createResource());
		seq6.add(1, AbstractModelTestBase.tvBoolean);
		n++;
		if (!(seq6.getBoolean(1) == AbstractModelTestBase.tvBoolean))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvByte);
		n++;
		if (!(seq6.getByte(1) == AbstractModelTestBase.tvByte))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvShort);
		n++;
		if (!(seq6.getShort(1) == AbstractModelTestBase.tvShort))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvInt);
		n++;
		if (!(seq6.getInt(1) == AbstractModelTestBase.tvInt))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvLong);
		n++;
		if (!(seq6.getLong(1) == AbstractModelTestBase.tvLong))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvChar);
		n++;
		if (!(seq6.getChar(1) == AbstractModelTestBase.tvChar))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvFloat);
		n++;
		if (!(seq6.getFloat(1) == AbstractModelTestBase.tvFloat))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvDouble);
		n++;
		if (!(seq6.getDouble(1) == AbstractModelTestBase.tvDouble))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvString);
		n++;
		if (!(seq6.getString(1).equals(AbstractModelTestBase.tvString)))
		{
			error(test, n);
		}
		seq6.add(1, AbstractModelTestBase.tvString, TestSeqMethods.lang);
		n++;
		if (!(seq6.getString(1).equals(AbstractModelTestBase.tvString)))
		{
			error(test, n);
		}
		seq6.add(1, tvResource);
		n++;
		if (!(seq6.getResource(1).equals(tvResource)))
		{
			error(test, n);
		}
		seq6.add(1, tvLiteral);
		n++;
		if (!(seq6.getLiteral(1).equals(tvLiteral)))
		{
			error(test, n);
		}
		seq6.add(1, anObject);
		n++;
		// if (!(seq6.getObject( 1, new LitTestObjF() ).equals( anObject )))
		// error(
		// test, n );

		n = ((n / 100) * 100) + 100;
		n++;
		if (!(seq6.indexOf(anObject) == 1))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(tvLiteral) == 2))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(tvResource) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvString, TestSeqMethods.lang) == 4))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvString) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvDouble) == 6))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvFloat) == 7))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvChar) == 8))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvLong) == 9))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvInt) == 10))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvShort) == 11))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvByte) == 12))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(AbstractModelTestBase.tvBoolean) == 13))
		{
			error(test, n);
		}
		n++;
		if (!(seq6.indexOf(1234543) == 0))
		{
			error(test, n);
		}
	}

	public void testSeq7()
	{
		final Seq seq7 = model.createSeq();
		final String test = "seq7";
		int n = 0;
		n = ((n / 100) * 100) + 100;
		for (int i = 0; i < TestSeqMethods.num; i++)
		{
			seq7.add(i);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvBoolean);
		n++;
		if (!(seq7.getBoolean(5) == AbstractModelTestBase.tvBoolean))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvByte);
		n++;
		if (!(seq7.getByte(5) == AbstractModelTestBase.tvByte))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvShort);
		n++;
		if (!(seq7.getShort(5) == AbstractModelTestBase.tvShort))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvInt);
		n++;
		if (!(seq7.getInt(5) == AbstractModelTestBase.tvInt))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvLong);
		n++;
		if (!(seq7.getLong(5) == AbstractModelTestBase.tvLong))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvChar);
		n++;
		if (!(seq7.getChar(5) == AbstractModelTestBase.tvChar))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvFloat);
		n++;
		if (!(seq7.getFloat(5) == AbstractModelTestBase.tvFloat))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvDouble);
		n++;
		if (!(seq7.getDouble(5) == AbstractModelTestBase.tvDouble))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, AbstractModelTestBase.tvString);
		n++;
		if (!(seq7.getString(5).equals(AbstractModelTestBase.tvString)))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getLanguage(5).equals("")))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		seq7.set(5, AbstractModelTestBase.tvString, TestSeqMethods.lang);
		n++;
		if (!(seq7.getString(5).equals(AbstractModelTestBase.tvString)))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getLanguage(5).equals(TestSeqMethods.lang)))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, tvLiteral);
		n++;
		if (!(seq7.getLiteral(5).equals(tvLiteral)))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, tvResource);
		n++;
		if (!(seq7.getResource(5).equals(tvResource)))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		seq7.set(5, anObject);
		n++;
		// if (!(seq7.getObject( 5, new LitTestObjF() )).equals( anObject ))
		// error(
		// test, n );
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
		n = ((n / 100) * 100) + 100;
		// seq7.set( 5, tvResObj );
		// n++;
		// if (!(seq7.getResource( 5, new ResTestObjF() ).equals( tvResObj )))
		// error(
		// test, n );
		n++;
		if (!(seq7.getInt(4) == 3))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.getInt(6) == 5))
		{
			error(test, n);
		}
		n++;
		if (!(seq7.size() == TestSeqMethods.num))
		{
			error(test, n);
		}
	}

	public void testSeqAccessByIndexing()
	{
		// LitTestObj tvObject = new LitTestObj(12345);
		final Literal tvLiteral = model.createLiteral("test 12 string 2");
		final Resource tvResource = model.createResource();
		// Resource tvResObj = model.createResource(new ResTestObjF());
		final Object tvLitObj = new LitTestObj(1234);
		final Bag tvBag = model.createBag();
		final Alt tvAlt = model.createAlt();
		final Seq tvSeq = model.createSeq();
		//
		final Seq seq = model.createSeq();
		seq.add(true);
		Assert.assertEquals(true, seq.getBoolean(1));
		seq.add((byte) 1);
		Assert.assertEquals((byte) 1, seq.getByte(2));
		seq.add((short) 2);
		Assert.assertEquals((short) 2, seq.getShort(3));
		seq.add(-1);
		Assert.assertEquals(-1, seq.getInt(4));
		seq.add(-2);
		Assert.assertEquals(-2, seq.getLong(5));
		seq.add('!');
		Assert.assertEquals('!', seq.getChar(6));
		seq.add(123.456f);
		Assert.assertEquals(123.456f, seq.getFloat(7), 0.00005);
		seq.add(12345.67890);
		Assert.assertEquals(12345.67890, seq.getDouble(8), 0.00000005);
		seq.add("some string");
		Assert.assertEquals("some string", seq.getString(9));
		seq.add(tvLitObj);
		// assertEquals( tvLitObj, seq.getObject( 10, new LitTestObjF() ) );
		seq.add(tvResource);
		Assert.assertEquals(tvResource, seq.getResource(11));
		// seq.add( tvResObj );
		// assertEquals( tvResObj, seq.getResource( 12, new ResTestObjF() ) );
		seq.add(tvLiteral);
		Assert.assertEquals(tvLiteral, seq.getLiteral(12));
		seq.add(tvBag);
		Assert.assertEquals(tvBag, seq.getBag(13));
		seq.add(tvAlt);
		Assert.assertEquals(tvAlt, seq.getAlt(14));
		seq.add(tvSeq);
		Assert.assertEquals(tvSeq, seq.getSeq(15));
		//
		try
		{
			seq.getInt(16);
			Assert.fail("there is no element 16");
		}
		catch (final SeqIndexBoundsException e)
		{
			JenaTestBase.pass();
		}
		try
		{
			seq.getInt(0);
			Assert.fail("there is no element 0");
		}
		catch (final SeqIndexBoundsException e)
		{
			JenaTestBase.pass();
		}
	}

	public void testSeqAdd()
	{
		final Seq seq = model.createSeq();
		Assert.assertEquals(0, seq.size());
		Assert.assertTrue(model.contains(seq, RDF.type, RDF.Seq));
		//
		seq.add(AbstractModelTestBase.tvBoolean);
		Assert.assertTrue(seq.contains(AbstractModelTestBase.tvBoolean));
		Assert.assertFalse(seq.contains(!AbstractModelTestBase.tvBoolean));
		//
		seq.add(AbstractModelTestBase.tvByte);
		Assert.assertTrue(seq.contains(AbstractModelTestBase.tvByte));
		Assert.assertFalse(seq.contains((byte) 101));
		//
		seq.add(AbstractModelTestBase.tvShort);
		Assert.assertTrue(seq.contains(AbstractModelTestBase.tvShort));
		Assert.assertFalse(seq.contains((short) 102));
		//
		seq.add(AbstractModelTestBase.tvInt);
		Assert.assertTrue(seq.contains(AbstractModelTestBase.tvInt));
		Assert.assertFalse(seq.contains(-101));
		//
		seq.add(AbstractModelTestBase.tvLong);
		Assert.assertTrue(seq.contains(AbstractModelTestBase.tvLong));
		Assert.assertFalse(seq.contains(-102));
		//
		seq.add(AbstractModelTestBase.tvChar);
		Assert.assertTrue(seq.contains(AbstractModelTestBase.tvChar));
		Assert.assertFalse(seq.contains('?'));
		//
		seq.add(123.456f);
		Assert.assertTrue(seq.contains(123.456f));
		Assert.assertFalse(seq.contains(456.123f));
		//
		seq.add(-123.456d);
		Assert.assertTrue(seq.contains(-123.456d));
		Assert.assertFalse(seq.contains(-456.123d));
		//
		seq.add("a string");
		Assert.assertTrue(seq.contains("a string"));
		Assert.assertFalse(seq.contains("a necklace"));
		//
		seq.add(model.createLiteral("another string"));
		Assert.assertTrue(seq.contains("another string"));
		Assert.assertFalse(seq.contains("another necklace"));
		//
		seq.add(new LitTestObj(12345));
		Assert.assertTrue(seq.contains(new LitTestObj(12345)));
		Assert.assertFalse(seq.contains(new LitTestObj(54321)));
		//
		// Resource present = model.createResource( new ResTestObjF() );
		// Resource absent = model.createResource( new ResTestObjF() );
		// seq.add( present );
		// assertTrue( seq.contains( present ) );
		// assertFalse( seq.contains( absent ) );
		//
		Assert.assertEquals(11, seq.size());
	}

	public void testSeqAddInts()
	{
		final int num = 10;
		final Seq seq = model.createSeq();
		for (int i = 0; i < num; i += 1)
		{
			seq.add(i);
		}
		Assert.assertEquals(num, seq.size());
		final List<RDFNode> L = seq.iterator().toList();
		Assert.assertEquals(num, L.size());
		for (int i = 0; i < num; i += 1)
		{
			Assert.assertEquals(i, ((Literal) L.get(i)).getInt());
		}
	}

	public void testSeqInsertByIndexing()
	{
		// LitTestObj tvObject = new LitTestObj(12345);
		final Literal tvLiteral = model.createLiteral("test 12 string 2");
		final Resource tvResource = model.createResource();
		// Resource tvResObj = model.createResource(new ResTestObjF());
		final Object tvLitObj = new LitTestObj(1234);
		final Bag tvBag = model.createBag();
		final Alt tvAlt = model.createAlt();
		final Seq tvSeq = model.createSeq();

		final Seq seq = model.createSeq();
		seq.add(model.createResource());
		seq.add(1, true);
		Assert.assertEquals(true, seq.getBoolean(1));
		seq.add(1, (byte) 1);
		Assert.assertEquals((byte) 1, seq.getByte(1));
		seq.add(1, (short) 2);
		Assert.assertEquals((short) 2, seq.getShort(1));
		seq.add(1, -1);
		Assert.assertEquals(-1, seq.getInt(1));
		seq.add(1, -2);
		Assert.assertEquals(-2, seq.getLong(1));
		seq.add(1, '!');
		Assert.assertEquals('!', seq.getChar(1));
		seq.add(1, 123.456f);
		Assert.assertEquals(123.456f, seq.getFloat(1), 0.00005);
		seq.add(1, 12345.67890);
		Assert.assertEquals(12345.67890, seq.getDouble(1), 0.00000005);
		seq.add(1, "some string");
		Assert.assertEquals("some string", seq.getString(1));
		seq.add(1, tvLitObj);
		// assertEquals( tvLitObj, seq.getObject( 1, new LitTestObjF() ) );
		seq.add(1, tvResource);
		Assert.assertEquals(tvResource, seq.getResource(1));
		// seq.add( 1, tvResObj );
		// assertEquals( tvResObj, seq.getResource( 1, new ResTestObjF() ) );
		seq.add(1, tvLiteral);
		Assert.assertEquals(tvLiteral, seq.getLiteral(1));
		seq.add(1, tvBag);
		Assert.assertEquals(tvBag, seq.getBag(1));
		seq.add(1, tvAlt);
		Assert.assertEquals(tvAlt, seq.getAlt(1));
		seq.add(1, tvSeq);
		Assert.assertEquals(tvSeq, seq.getSeq(1));
		//
		Assert.assertEquals(0, seq.indexOf(1234543));
		Assert.assertEquals(1, seq.indexOf(tvSeq));
		Assert.assertEquals(2, seq.indexOf(tvAlt));
		Assert.assertEquals(3, seq.indexOf(tvBag));
		Assert.assertEquals(4, seq.indexOf(tvLiteral));
		Assert.assertEquals(5, seq.indexOf(tvResource));
		Assert.assertEquals(6, seq.indexOf(tvLitObj));
		Assert.assertEquals(7, seq.indexOf("some string"));
		Assert.assertEquals(8, seq.indexOf(12345.67890));
		Assert.assertEquals(9, seq.indexOf(123.456f));
		Assert.assertEquals(10, seq.indexOf('!'));
		Assert.assertEquals(11, seq.indexOf(-2));
		Assert.assertEquals(12, seq.indexOf(-1));
		Assert.assertEquals(13, seq.indexOf((short) 2));
		Assert.assertEquals(14, seq.indexOf((byte) 1));
		Assert.assertEquals(15, seq.indexOf(true));
	}

	public void testSet()
	{
		// NodeIterator nIter;
		// StmtIterator sIter;
		final Literal tvLiteral = model.createLiteral("test 12 string 2");
		final Resource tvResource = model.createResource();
		// Resource tvResObj = model.createResource(new ResTestObjF());
		// Bag tvBag = model.createBag();
		// Alt tvAlt = model.createAlt();
		// Seq tvSeq = model.createSeq();
		final int num = 10;
		final Seq seq = model.createSeq();

		for (int i = 0; i < num; i++)
		{
			seq.add(i);
		}

		seq.set(5, AbstractModelTestBase.tvBoolean);
		Assert.assertEquals(AbstractModelTestBase.tvBoolean, seq.getBoolean(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvByte);
		Assert.assertEquals(AbstractModelTestBase.tvByte, seq.getByte(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvShort);
		Assert.assertEquals(AbstractModelTestBase.tvShort, seq.getShort(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvInt);
		Assert.assertEquals(AbstractModelTestBase.tvInt, seq.getInt(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvLong);
		Assert.assertEquals(AbstractModelTestBase.tvLong, seq.getLong(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvString);
		Assert.assertEquals(AbstractModelTestBase.tvString, seq.getString(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvBoolean);
		Assert.assertEquals(AbstractModelTestBase.tvBoolean, seq.getBoolean(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvFloat);
		Assert.assertEquals(AbstractModelTestBase.tvFloat, seq.getFloat(5),
				0.00005);
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvDouble);
		Assert.assertEquals(AbstractModelTestBase.tvDouble, seq.getDouble(5),
				0.000000005);
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, tvLiteral);
		Assert.assertEquals(tvLiteral, seq.getLiteral(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, tvResource);
		Assert.assertEquals(tvResource, seq.getResource(5));
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		seq.set(5, AbstractModelTestBase.tvLitObj);
		// assertEquals( tvLitObj, seq.getObject( 5, new LitTestObjF() ) );
		Assert.assertEquals(3, seq.getInt(4));
		Assert.assertEquals(5, seq.getInt(6));
		Assert.assertEquals(num, seq.size());

		// seq.set( 5, tvResObj );
		// assertEquals( tvResObj, seq.getResource( 5, new ResTestObjF() ) );
		// assertEquals( 3, seq.getInt( 4 ) );
		// assertEquals( 5, seq.getInt( 6 ) );
		// assertEquals( num, seq.size() );
	}

}
