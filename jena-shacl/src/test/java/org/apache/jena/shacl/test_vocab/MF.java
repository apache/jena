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

public class MF {

	public static final String BASE_URI = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest";
	
	public static final String NS = BASE_URI + "#";
			
			
	public final static Property action = ResourceFactory.createProperty(NS + "action");

	public final static Property entries = ResourceFactory.createProperty(NS + "entries");

	public final static Property include = ResourceFactory.createProperty(NS + "include");
	
	public final static Resource Manifest = ResourceFactory.createResource(NS + "Manifest");
	
	public final static Property result = ResourceFactory.createProperty(NS + "result");
	
	public final static Property status = ResourceFactory.createProperty(NS + "status");
}
