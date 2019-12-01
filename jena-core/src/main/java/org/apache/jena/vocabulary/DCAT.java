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
 * Constants for the W3C Data Catalog Vocabulary.
 *
 * @see <a href="https://www.w3.org/TR/vocab-dcat/">Data Catalog Vocabulary</a>
 */
public class DCAT {
	private static final Model m = ModelFactory.createDefaultModel();

	public static final String NS = "http://www.w3.org/ns/dcat#";
	public static final Resource NAMESPACE = m.createResource(NS);
	
	/**
	 * Returns the URI for this schema
	 * @return URI
	 */
	public static String getURI() {
		return NS;
	}
	
	// Classes
	public static final Resource Catalog = m.createResource(NS + "Catalog");
	public static final Resource CatalogRecord = m.createResource(NS + "CatalogRecord");
	public static final Resource Dataset = m.createResource(NS + "Dataset");
	public static final Resource Distribution = m.createResource(NS + "Distribution");
	
	// Classes added in DCAT version 2
	public static final Resource DataService = m.createResource(NS + "DataService");
	public static final Resource Relationship = m.createResource(NS + "Relationship");
	public static final Resource Role = m.createResource(NS + "Role");
	
	// Properties
	public static final Property accessURL = m.createProperty(NS + "accessURL");
	public static final Property byteSize = m.createProperty(NS + "byteSize");
	public static final Property contactPoint = m.createProperty(NS + "contactPoint");
	public static final Property dataset = m.createProperty(NS + "dataset");
	public static final Property distribution = m.createProperty(NS + "distribution");
	public static final Property downloadURL = m.createProperty(NS + "downloadURL");
	public static final Property keyword = m.createProperty(NS + "keyword");	
	public static final Property landingPage = m.createProperty(NS + "landingPage");
	public static final Property mediaType = m.createProperty(NS + "mediaType");
	public static final Property record = m.createProperty(NS + "record");
	public static final Property theme = m.createProperty(NS + "theme");
	public static final Property themeTaxonomy = m.createProperty(NS + "themeTaxonomy");
	
	// Properties added in DCAT version 2
	public static final Property accessService = m.createProperty(NS + "accessService");
	public static final Property bbox = m.createProperty(NS + "bbox");
	public static final Property catalog = m.createProperty(NS + "catalog");
	public static final Property centroid = m.createProperty(NS + "centroid");
	public static final Property compressFormat = m.createProperty(NS + "compressFormat");
	public static final Property endDate = m.createProperty(NS + "endDate");
	public static final Property endpointDescription = m.createProperty(NS + "endpointDescription");
	public static final Property endpointURL = m.createProperty(NS + "endpointURL");
	public static final Property hadRole = m.createProperty(NS + "hadRole");
	public static final Property packageFormat = m.createProperty(NS + "packageFormat");
	public static final Property qualifiedRelation = m.createProperty(NS + "qualifiedRelation");
	public static final Property servesDataset = m.createProperty(NS + "servesDataset");
	public static final Property service = m.createProperty(NS + "service");
	public static final Property spatialResolutionInMeters = m.createProperty(NS + "spatialResolutionInMeters");
	public static final Property startDate = m.createProperty(NS + "startDate");
	public static final Property temporalResolution = m.createProperty(NS + "temporalResolution");
}
