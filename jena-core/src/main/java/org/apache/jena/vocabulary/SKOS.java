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
 * <a href="http://www.w3.org/2009/08/skos-reference/skos.html">W3C SKOS Recommendation</a>.
 */
public class SKOS {
	/**
	 * The RDF model that holds the SKOS entities
	 */
	public static Model m = ModelFactory.createDefaultModel();
	/**
	 * The namespace of the SKOS vocabulary as a string
	 */
	public static final String uri = "http://www.w3.org/2004/02/skos/core#";
	/**
	 * Returns the namespace of the SKOS schema as a string
	 * @return the namespace of the SKOS schema
	 */
	public static String getURI() {
		return uri;
	}
	/**
	 * The namespace of the SKOS vocabulary
	 */
	public static final Resource NAMESPACE = m.createResource( uri );
	/* ##########################################################
	 * Defines SKOS Classes
	   ########################################################## */
	public static final Resource Concept = m.createResource( uri + "Concept");
	public static final Resource ConceptScheme = m.createResource( uri + "ConceptScheme");
	public static final Resource Collection = m.createResource(uri + "Collection");
	public static final Resource OrderedCollection = m.createResource( uri + "OrderedCollection");
	/* ##########################################################
	 * Defines SKOS Properties
	   ########################################################## */
	// SKOS lexical label properties
	public static final Property prefLabel = m.createProperty( uri + "prefLabel");
	public static final Property altLabel = m.createProperty( uri + "altLabel");
	public static final Property hiddenLabel = m.createProperty( uri + "hiddenLabel");
	// SKOS documentation properties
	public static final Property definition = m.createProperty( uri + "definition");
	public static final Property note = m.createProperty( uri + "note");
	public static final Property scopeNote = m.createProperty( uri + "scopeNote");
	public static final Property historyNote = m.createProperty( uri + "historyNote");
	public static final Property changeNote = m.createProperty( uri + "changeNote");
	public static final Property editorialNote = m.createProperty( uri + "editorialNote");
	public static final Property example = m.createProperty( uri + "example");
	// SKOS notation properties
	public static final Property notation = m.createProperty( uri + "notation");
	// SKOS semantic relations properties
	public static final Property semanticRelation = m.createProperty( uri + "semanticRelation");
	public static final Property broaderTransitive = m.createProperty( uri + "broaderTransitive");
	public static final Property broader = m.createProperty( uri + "broader");
	public static final Property narrowerTransitive = m.createProperty( uri + "narrowerTransitive");
	public static final Property narrower = m.createProperty( uri + "narrower");
	public static final Property related = m.createProperty( uri + "related");
	// SKOS mapping properties
	public static final Property mappingRelation = m.createProperty( uri + "mappingRelation");
	public static final Property exactMatch = m.createProperty( uri + "exactMatch");
	public static final Property closeMatch = m.createProperty( uri + "closeMatch");
	public static final Property broadMatch = m.createProperty( uri + "broadMatch");
	public static final Property narrowMatch = m.createProperty( uri + "narrowMatch");
	public static final Property relatedMatch = m.createProperty( uri + "relatedMatch");
	// SKOS concept scheme properties
	public static final Property inScheme = m.createProperty( uri + "inScheme");
	public static final Property hasTopConcept = m.createProperty( uri + "hasTopConcept");
	public static final Property topConceptOf = m.createProperty( uri + "topConceptOf");
	// SKOS collection properties
	public static final Property member = m.createProperty( uri + "member");
	public static final Property memberList = m.createProperty( uri + "memberList");
}
