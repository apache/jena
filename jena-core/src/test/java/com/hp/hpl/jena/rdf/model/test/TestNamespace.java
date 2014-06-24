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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.AbstractTestPrefixMapping;
import com.hp.hpl.jena.util.CollectionFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.junit.Assert;

public class TestNamespace extends AbstractModelTestBase
{
	public TestNamespace( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	/**
	 * turn a semi-separated set of P=U definitions into a namespace map.
	 */
	private Map<String, Set<String>> makePrefixes( final String prefixes )
	{
		final Map<String, Set<String>> result = new HashMap<>();
		final StringTokenizer st = new StringTokenizer(prefixes, ";");
		while (st.hasMoreTokens())
		{
			final String def = st.nextToken();
			// System.err.println( "| def is " + def );
			final int eq = def.indexOf('=');
			result.put(def.substring(0, eq), set(def.substring(eq + 1)));
		}
		// result.put( "spoo", set( "http://spoo.net/" ) );
		return result;
	}

	/**
	 * make a single-element set.
	 * 
	 * @param element
	 *            the single element to contain
	 * @return a set whose only element == element
	 */
	private Set<String> set( final String element )
	{
		final Set<String> s = CollectionFactory.createHashedSet();
		s.add(element);
		return s;
	}

	/**
	 * a simple test of the prefix reader on a known file. test0014.rdf is known
	 * to
	 * have a namespace definition for eg and rdf, and not for spoo so we see if
	 * we
	 * can extract them (or not, for spoo).
	 */
	public void testReadPrefixes()
	{
		model.read(getFileName( "wg/rdf-ns-prefix-confusion/test0014.rdf" ));
		final Map<String, String> ns = model.getNsPrefixMap();
		// System.err.println( ">> " + ns );
		Assert.assertEquals("namespace eg", "http://example.org/", ns.get("eg"));
		Assert.assertEquals("namespace rdf",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", ns.get("rdf"));
		Assert.assertEquals("not present", null, ns.get("spoo"));
	}

	public void testUseEasyPrefix()
	{
		AbstractTestPrefixMapping.testUseEasyPrefix("default model",
				ModelFactory.createDefaultModel());
	}

	/**
	 * a horridly written test to write out a model with some known namespace
	 * prefixes and see if they can be read back in again.
	 * 
	 * TODO tidy and abstract this - we want some more tests.
	 * 
	 * TODO there's a problem: namespaces that aren't used on properties
	 * don't reliably get used. Maybe they shouldn't be - but it seems odd.
	 */
	public void testWritePrefixes() throws IOException
	{
		ModelCom.addNamespaces(model,
				makePrefixes("fred=ftp://net.fred.org/;spoo=http://spoo.net/"));
		final File f = File.createTempFile("hedgehog", ".rdf");
		model.add(ModelHelper.statement(model,
				"http://spoo.net/S http://spoo.net/P http://spoo.net/O"));
		model.add(ModelHelper.statement(model,
				"http://spoo.net/S ftp://net.fred.org/P http://spoo.net/O"));
		model.write(new FileOutputStream(f));
		/* */
		final Model m2 = ModelFactory.createDefaultModel();
		m2.read("file:" + f.getAbsolutePath());
		final Map<String, String> ns = m2.getNsPrefixMap();
		Assert.assertEquals("namespace spoo", "http://spoo.net/",
				ns.get("spoo"));
		Assert.assertEquals("namespace fred", "ftp://net.fred.org/",
				ns.get("fred"));
		/* */
		f.deleteOnExit();
	}

}
