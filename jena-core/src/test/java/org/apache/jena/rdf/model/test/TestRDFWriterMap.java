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

package org.apache.jena.rdf.model.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.Jena ;
import org.apache.jena.rdf.model.RDFWriterI ;
import org.apache.jena.rdf.model.RDFWriterF ;
import org.apache.jena.rdf.model.impl.NTripleWriter ;
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Abbrev ;
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Basic ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.NoWriterForLangException ;
import org.apache.jena.test.JenaTestBase ;
import org.junit.Assert;

public class TestRDFWriterMap extends JenaTestBase
{
	public static class RDFWriterMap implements RDFWriterF
	{
		protected final Map<String, Class<RDFWriterI>> map = new HashMap<>();

		public RDFWriterMap( final boolean preloadDefaults )
		{
			if (preloadDefaults)
			{
				loadDefaults();
			}
		}

		@Override
		public RDFWriterI getWriter()
		{
			return getWriter("RDF/XML");
		}

		@Override
		public RDFWriterI getWriter( final String lang )
		{
			final Class<RDFWriterI> result = map.get(lang);
			if (result == null)
			{
				throw new NoWriterForLangException(lang);
			}
			try
			{
				return result.getConstructor().newInstance();
			}
			catch (final Exception e)
			{
				throw new JenaException(e);
			}
		}

		private void loadDefaults()
		{
            setWriterClassName(TestRDFWriterMap.RDF_XML,        Jena.PATH + ".rdfxml.xmloutput.impl.RDFXML_Basic");
            setWriterClassName(TestRDFWriterMap.RDF_XML_ABBREV, Jena.PATH + ".rdfxml.xmloutput.impl.RDFXML_Abbrev");
            setWriterClassName(TestRDFWriterMap.NTRIPLE,        Jena.PATH + ".rdf.model.impl.NTripleWriter");
            setWriterClassName(TestRDFWriterMap.NTRIPLES,       Jena.PATH + ".rdf.model.impl.NTripleWriter");
		}

		private String setWriterClassName( final String lang, String className )
		{
			try
			{
				final Class<RDFWriterI> old = map.get(lang);
				final Class<?> c = Class.forName(className);
				if (RDFWriterI.class.isAssignableFrom(c))
				{
					@SuppressWarnings( "unchecked" )
					final Class<RDFWriterI> x = (Class<RDFWriterI>) c;
					map.put(lang, x);
				}
				return old == null ? null : old.getName();
			}
			catch (final ClassNotFoundException e)
			{
				throw new JenaException(e);
			}
		}
	}

	public static final String RDF_XML = "RDF/XML";
	public static final String RDF_XML_ABBREV = "RDF/XML-ABBREV";
	public static final String NTRIPLE = "N-TRIPLE";
	public static final String NTRIPLES = "N-TRIPLES";

	public TestRDFWriterMap( final String name )
	{
		super(name);
	}

	public void testDefaultWriter()
	{
		final RDFWriterF x = new RDFWriterMap(true);
		Assert.assertEquals(x.getWriter("RDF/XML").getClass(),
		                    x.getWriter().getClass());
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
		testWriterAbsent(TestRDFWriterMap.RDF_XML);
		testWriterAbsent(TestRDFWriterMap.RDF_XML_ABBREV);
		testWriterAbsent(TestRDFWriterMap.NTRIPLE);
		testWriterAbsent(TestRDFWriterMap.NTRIPLES);
	}

	public void testWritersPresent()
	{
		final RDFWriterF x = new RDFWriterMap(true);
		Assert.assertEquals(RDFXML_Basic.class,
		                    x.getWriter(TestRDFWriterMap.RDF_XML).getClass());
		Assert.assertEquals(RDFXML_Abbrev.class,
		                    x.getWriter(TestRDFWriterMap.RDF_XML_ABBREV).getClass());
		Assert.assertEquals(NTripleWriter.class,
		                    x.getWriter(TestRDFWriterMap.NTRIPLE).getClass());
		Assert.assertEquals(NTripleWriter.class,
		                    x.getWriter(TestRDFWriterMap.NTRIPLES).getClass());
	}
}
