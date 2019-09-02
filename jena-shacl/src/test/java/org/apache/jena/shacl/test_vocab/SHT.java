/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.apache.jena.shacl.test_vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SHT {

	public static final String BASE_URI = "http://www.w3.org/ns/shacl-test";
	
	public static final String NS = BASE_URI + "#";
	
	public final static Resource CoreOnly = ResourceFactory.createResource(NS + "CoreOnly");
	
	public final static Property dataGraph = ResourceFactory.createProperty(NS + "dataGraph");
	
	public final static Resource Failure = ResourceFactory.createResource(NS + "Failure");
	
	public final static Resource proposed = ResourceFactory.createResource(NS + "proposed");
	
	public final static Property shapesGraph = ResourceFactory.createProperty(NS + "shapesGraph");
	
	public final static Resource Validate = ResourceFactory.createResource(NS + "Validate");
}
