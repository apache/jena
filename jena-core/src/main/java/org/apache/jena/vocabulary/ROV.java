/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
 
/**
 * Constants for the W3C Registered Organization Vocabulary.
 *
 * @see <a href="https://www.w3.org/TR/vocab-regorg/">Registered Organization Vocabulary</a>
 */
public class ROV {
	private static final Model m = ModelFactory.createDefaultModel();

	public static final String NS = "http://www.w3.org/ns/regorg#";
	public static final Resource NAMESPACE = m.createResource(NS);
	
	/**
	 * Returns the URI for this schema
	 * @return URI
	 */
	public static String getURI() {
		return NS;
	}
	
	// Classes
	public static final Resource RegisteredOrganization = m.createResource(NS + "RegisteredOrganization");

	// Properties
	public static final Property hasRegisteredOrganization = m.createProperty(NS + "hasRegisteredOrganization");
	public static final Property legalName = m.createProperty(NS + "legalName");
	public static final Property orgActivity = m.createProperty(NS + "orgActivity");
	public static final Property orgStatus = m.createProperty(NS + "orgStatus");
	public static final Property orgType = m.createProperty(NS + "orgType");
	public static final Property registration = m.createProperty(NS + "registration");
}
