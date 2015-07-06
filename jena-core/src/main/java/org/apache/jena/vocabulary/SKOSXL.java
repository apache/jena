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

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;

/**
 * Vocabulary definition for the 
 * <a href="http://www.w3.org/TR/skos-reference/skos-xl.html">W3C SKOS-XL Recommendation</a>.
 */
public class SKOSXL {
	/**
	 * The RDF model that holds the SKOS-XL entities
	 */
	public static Model m = ModelFactory.createDefaultModel();
	/**
	 * The namespace of the SKOS-XL vocabulary as a string
	 */
	public static final String uri = "http://www.w3.org/2008/05/skos-xl#";
	/**
	 * Returns the namespace of the SKOS-XL schema as a string
	 * @return the namespace of the SKOS-XL schema
	 */
	public static String getURI() {
		return uri;
	}
	/**
	 * The namespace of the SKOS-XL vocabulary
	 */
	public static final Resource NAMESPACE = m.createResource( uri );
	/* ##########################################################
	 * Defines SKOS-XL Classes
	   ########################################################## */
	public static final Resource Label = m.createResource( uri + "Label");
	/* ##########################################################
	 * Defines SKOS-XL Properties
	   ########################################################## */
	public static final Property prefLabel = m.createProperty( uri + "prefLabel");
	public static final Property altLabel = m.createProperty( uri + "altLabel");
	public static final Property hiddenLabel = m.createProperty( uri + "hiddenLabel");
	public static final Property labelRelation = m.createProperty( uri + "labelRelation");
	public static final Property literalForm = m.createProperty( uri + "literalForm");
}
