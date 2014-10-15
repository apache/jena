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
package org.apache.jena.security;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import java.net.URL;


import org.junit.Assert;

import org.apache.jena.security.model.SecuredModel;
import org.junit.Before;
import org.junit.Test;

public class SecuredAssemblerTest
{
	private Assembler assembler;
	private Model model;
	
	public SecuredAssemblerTest() 
	{
		assembler = Assembler.general;
	}
	
	@Before
	public void setUp() throws Exception {
		model = ModelFactory.createDefaultModel();
		URL url = SecuredAssemblerTest.class.getClassLoader().getResource( SecuredAssemblerTest.class.getName().replace(".", "/")+".ttl");
		model.read( url.toURI().toString(), "TURTLE" );
		//model.write( System.out, "TURTLE" );
	}
	
	@Test
	public void testCreation() throws Exception {
		
		Resource r = model.createResource( "http://apache.org/jena/security/test#secModel");
		Object o = assembler.open( r );
		Assert.assertTrue( o instanceof Model);
		Assert.assertTrue( o instanceof SecuredModel );
	}

}
