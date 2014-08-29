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

import com.hp.hpl.jena.Jena;
import com.hp.hpl.jena.n3.N3JenaWriter;
import com.hp.hpl.jena.n3.N3JenaWriterPP;
import com.hp.hpl.jena.n3.N3JenaWriterPlain;
import com.hp.hpl.jena.n3.N3JenaWriterTriples;
import com.hp.hpl.jena.n3.N3TurtleJenaWriter;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.RDFWriterF;
import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;
import com.hp.hpl.jena.rdfxml.xmloutput.impl.Abbreviated ;
import com.hp.hpl.jena.rdfxml.xmloutput.impl.Basic ;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NoWriterForLangException;
import com.hp.hpl.jena.test.JenaTestBase;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

public class TestRDFWriterMap extends JenaTestBase
{
	public static class RDFWriterMap implements RDFWriterF
	{
		protected final Map<String, Class<RDFWriter>> map = new HashMap<>();

		public RDFWriterMap( final boolean preloadDefaults )
		{
			if (preloadDefaults)
			{
				loadDefaults();
			}
		}

		@Override
		public RDFWriter getWriter()
		{
			return getWriter("RDF/XML");
		}

		@Override
		public RDFWriter getWriter( final String lang )
		{
			final Class<RDFWriter> result = map.get(lang);
			if (result == null)
			{
				throw new NoWriterForLangException(lang);
			}
			try
			{
				return result.newInstance();
			}
			catch (final Exception e)
			{
				throw new JenaException(e);
			}
		}

		private void loadDefaults()
		{
			setWriterClassName(TestRDFWriterMap.TURTLE_WRITER, Jena.PATH
					+ ".n3.N3TurtleJenaWriter");
			setWriterClassName(TestRDFWriterMap.TURTLE_WRITER_ALT1, Jena.PATH
					+ ".n3.N3TurtleJenaWriter");
			setWriterClassName(TestRDFWriterMap.TURTLE_WRITER_ALT2, Jena.PATH
					+ ".n3.N3TurtleJenaWriter");
			setWriterClassName(TestRDFWriterMap.RDF_XML, Jena.PATH
					+ ".xmloutput.impl.Basic");
			setWriterClassName(TestRDFWriterMap.RDF_XML_ABBREV, Jena.PATH
					+ ".xmloutput.impl.Abbreviated");
			setWriterClassName(TestRDFWriterMap.N3, Jena.PATH
					+ ".n3.N3JenaWriter");
			setWriterClassName(TestRDFWriterMap.N3_PLAIN, Jena.PATH
					+ ".n3.N3JenaWriterPlain");
			setWriterClassName(TestRDFWriterMap.N3_PP, Jena.PATH
					+ ".n3.N3JenaWriterPP");
			setWriterClassName(TestRDFWriterMap.N3_TRIPLE, Jena.PATH
					+ ".n3.N3JenaWriterTriples");
			setWriterClassName(TestRDFWriterMap.N3_TRIPLES, Jena.PATH
					+ ".n3.N3JenaWriterTriples");
			setWriterClassName(TestRDFWriterMap.NTRIPLE, Jena.PATH
					+ ".rdf.model.impl.NTripleWriter");
			setWriterClassName(TestRDFWriterMap.NTRIPLES, Jena.PATH
					+ ".rdf.model.impl.NTripleWriter");
		}

		@Override
		public String setWriterClassName( final String lang,
				final String className )
		{
			try
			{
				final Class<RDFWriter> old = map.get(lang);
				final Class<?> c = Class.forName(className);
				if (RDFWriter.class.isAssignableFrom(c))
				{
					@SuppressWarnings( "unchecked" )
					final Class<RDFWriter> x = (Class<RDFWriter>) c;
					map.put(lang, x);
				}
				return old == null ? null : old.getName();
			}
			catch (final ClassNotFoundException e)
			{
				throw new JenaException(e);
			}
		}

		@Override
		public void resetRDFWriterF() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String removeWriter(String lang) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static final String TURTLE_WRITER_ALT2 = N3JenaWriter.turtleWriterAlt2;
	public static final String TURTLE_WRITER_ALT1 = N3JenaWriter.turtleWriterAlt1;

	public static final String TURTLE_WRITER = N3JenaWriter.turtleWriter;
	public static final String RDF_XML = "RDF/XML";
	public static final String RDF_XML_ABBREV = "RDF/XML-ABBREV";
	public static final String NTRIPLE = "N-TRIPLE";
	public static final String NTRIPLES = "N-TRIPLES";
	public static final String N3 = "N3";
	public static final String N3_PLAIN = "N3-PLAIN";
	public static final String N3_PP = "N3-PP";
	public static final String N3_TRIPLE = "N3-TRIPLE";

	public static final String N3_TRIPLES = "N3-TRIPLES";

	public TestRDFWriterMap( final String name )
	{
		super(name);
	}

	public void testDefaultWriter()
	{
		final RDFWriterF x = new RDFWriterMap(true);
		Assert.assertEquals(x.getWriter("RDF/XML").getClass(), x.getWriter()
				.getClass());
	}

	/*
	 * public void testMe()
	 * {
	 * Assert.fail("SPOO");
	 * }
	 */

	private void testWriterAbsent( final String w )
	{
		final RDFWriterF x = new RDFWriterMap(false);
		try
		{
			x.getWriter(w);
		}
		catch (final NoWriterForLangException e)
		{
			Assert.assertEquals(w, e.getMessage());
		}
	}

	public void testWritersAbsent()
	{
		testWriterAbsent(TestRDFWriterMap.TURTLE_WRITER);
		testWriterAbsent(TestRDFWriterMap.TURTLE_WRITER_ALT1);
		testWriterAbsent(TestRDFWriterMap.TURTLE_WRITER_ALT2);
		testWriterAbsent(TestRDFWriterMap.RDF_XML);
		testWriterAbsent(TestRDFWriterMap.RDF_XML_ABBREV);
		testWriterAbsent(TestRDFWriterMap.NTRIPLE);
		testWriterAbsent(TestRDFWriterMap.NTRIPLES);
		testWriterAbsent(TestRDFWriterMap.N3);
		testWriterAbsent(TestRDFWriterMap.N3_PP);
		testWriterAbsent(TestRDFWriterMap.N3_PLAIN);
		testWriterAbsent(TestRDFWriterMap.N3_TRIPLE);
		testWriterAbsent(TestRDFWriterMap.N3_TRIPLES);
	}

	public void testWritersPresent()
	{
		final RDFWriterF x = new RDFWriterMap(true);
		Assert.assertEquals(N3TurtleJenaWriter.class,
				x.getWriter(TestRDFWriterMap.TURTLE_WRITER).getClass());
		Assert.assertEquals(N3TurtleJenaWriter.class,
				x.getWriter(TestRDFWriterMap.TURTLE_WRITER_ALT1).getClass());
		Assert.assertEquals(N3TurtleJenaWriter.class,
				x.getWriter(TestRDFWriterMap.TURTLE_WRITER_ALT2).getClass());
		Assert.assertEquals(Basic.class, x.getWriter(TestRDFWriterMap.RDF_XML)
				.getClass());
		Assert.assertEquals(Abbreviated.class,
				x.getWriter(TestRDFWriterMap.RDF_XML_ABBREV).getClass());
		Assert.assertEquals(NTripleWriter.class,
				x.getWriter(TestRDFWriterMap.NTRIPLE).getClass());
		Assert.assertEquals(NTripleWriter.class,
				x.getWriter(TestRDFWriterMap.NTRIPLES).getClass());
		Assert.assertEquals(N3JenaWriter.class, x
				.getWriter(TestRDFWriterMap.N3).getClass());
		Assert.assertEquals(N3JenaWriterPP.class,
				x.getWriter(TestRDFWriterMap.N3_PP).getClass());
		Assert.assertEquals(N3JenaWriterPlain.class,
				x.getWriter(TestRDFWriterMap.N3_PLAIN).getClass());
		Assert.assertEquals(N3JenaWriterTriples.class,
				x.getWriter(TestRDFWriterMap.N3_TRIPLE).getClass());
		Assert.assertEquals(N3JenaWriterTriples.class,
				x.getWriter(TestRDFWriterMap.N3_TRIPLES).getClass());
	}
}
