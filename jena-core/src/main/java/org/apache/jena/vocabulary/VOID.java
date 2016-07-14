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
 * Constants for the W3C Vocabulary of Interlinked Datasets.
 *
 * @see <a href="https://www.w3.org/TR/void/">Vocabulary of Interlinked Datasets</a>
 */
public class VOID {
	private static final Model m = ModelFactory.createDefaultModel();

	public static final String NS = "http://rdfs.org/ns/void#";
	public static final Resource NAMESPACE = m.createResource(NS);
	
	/**
	 * Returns the URI for this schema
	 * @return URI
	 */
	public static String getURI() {
		return NS;
	}
	
	// Classes
	public static final Resource Dataset = m.createResource(NS + "Dataset");
	public static final Resource DatasetDescription = m.createResource(NS + "DatasetDescription");
	public static final Resource Linkset = m.createResource(NS + "Linkset");
	public static final Resource TechnicalFeature = m.createResource(NS + "TechnicalFeature");

	// Properties
	public static final Property _class = m.createProperty(NS + "class");
	public static final Property classPartition = m.createProperty(NS + "classPartition");
	public static final Property classes = m.createProperty(NS + "classes");
	public static final Property dataDump = m.createProperty(NS + "dataDump");
	public static final Property distinctObjects = m.createProperty(NS + "distinctObjects");
	public static final Property distinctSubjects = m.createProperty(NS + "distinctSubjects");
	public static final Property documents = m.createProperty(NS + "documents");
	public static final Property entities = m.createProperty(NS + "entities");
	public static final Property exampleResource = m.createProperty(NS + "exampleResource");
	public static final Property feature = m.createProperty(NS + "feature");
	public static final Property inDataset = m.createProperty(NS + "inDataset");
	public static final Property linkPredicate = m.createProperty(NS + "linkPredicate");
	public static final Property objectsTarget = m.createProperty(NS + "objectsTarget");
	public static final Property openSearchDescription = m.createProperty(NS + "openSearchDescription");
	public static final Property properties = m.createProperty(NS + "properties");
	public static final Property property = m.createProperty(NS + "property");
	public static final Property propertyPartition = m.createProperty(NS + "propertyPartition");
	public static final Property rootResource = m.createProperty(NS + "rootResource");
	public static final Property sparqlEndpoint = m.createProperty(NS + "sparqlEndpoint");
	public static final Property subjectsTarget = m.createProperty(NS + "subjectsTarget");
	public static final Property subset = m.createProperty(NS + "subset");
	public static final Property target = m.createProperty(NS + "target");
	public static final Property triples = m.createProperty(NS + "triples");
	public static final Property uriLookupEndpoint = m.createProperty(NS + "uriLookupEndpoint");
	public static final Property uriRegexPattern = m.createProperty(NS + "uriRegexPattern");
	public static final Property uriSpace = m.createProperty(NS + "uriSpace");
	public static final Property vocabulary = m.createProperty(NS + "vocabulary");
}
