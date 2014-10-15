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
package org.apache.jena.security.contract.model;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.AbstractTestPackage;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileUtils;


import java.lang.reflect.InvocationTargetException;

import junit.framework.TestSuite;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.system.stream.Locator;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.riot.system.stream.LocatorZip;
import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator;

/**
 * Test package to test Model implementation.
 */
//@RunWith(ModelTestSuite.class)
public class SecTestPackage extends AbstractTestPackage
{
	static public TestSuite suite() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		return new SecTestPackage();
	}
	
	public SecTestPackage() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		super("SecuredModel", new PlainModelFactory() );
		// register a jar reader here
		StreamManager sm =StreamManager.get();
		sm.addLocator( new LocatorJarURL()  );
	}

	/* package private */static class PlainModelFactory implements TestingModelFactory
	{
		private final SecurityEvaluator eval;
		
		public PlainModelFactory()
		{
			eval = new MockSecurityEvaluator(true, true, true, true, true, true);
		}
		
		@Override
		public Model createModel()
		{
			// Graph graph = Factory.createDefaultGraph( style );
			final Model model = ModelFactory.createDefaultModel();
			return org.apache.jena.security.Factory.getInstance(eval, "testModel",
					model);
		}
		
		@Override
		public PrefixMapping getPrefixMapping()
		{
			return createModel().getGraph().getPrefixMapping();
		}
		
		@Override
		public Model createModel( Graph base )
		{
			return ModelFactory.createModelForGraph(base);
		}
	}
	
	public static class LocatorJarURL implements Locator {

		@Override
		public TypedInputStream open(String uri) {
			 String uriSchemeName = FileUtils.getScheme(uri) ;
			 if ( ! "jar".equalsIgnoreCase(uriSchemeName))
			 {
				 return null;
			 }
			 
			 String[] parts = uri.substring( 4 ).split("!");
			 if (parts.length != 2)
			 {
				 return null;
			 } 
			 if (parts[0].toLowerCase().startsWith("file:"))
			 {
				 parts[0] = parts[0].substring( 5 );
			 }
			 if (parts[1].startsWith( "/"))
			 {
				 parts[1] = parts[1].substring(1);
			 }
			 LocatorZip zl = new LocatorZip( parts[0] );
			 return zl.open(parts[1] );
		}

		@Override
		public String getName() {
			return "JarURLLocator";
		}
		
	}
}
