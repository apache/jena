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

// Package
///////////////////////////////////////
package com.hp.hpl.jena.vocabulary;


// Imports
///////////////////////////////////////
import com.hp.hpl.jena.rdf.model.*;




/**
 * Vocabulary definitions from file:vocabularies/ont-manager.rdf
 */
public class OntDocManagerVocab {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string ({@value})</p> */
    public static final String NS = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    

    // Vocabulary properties
    ///////////////////////////

    /** <p>The representation language used by the ontology document</p> */
    public static final Property language = m_model.createProperty( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#language" );
    
    /** <p>The public URI that is used to refer to the ontology document</p> */
    public static final Property publicURI = m_model.createProperty( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#publicURI" );
    
    /** <p>The prefix string that is used when writing qnames in the ontology's namespace</p> */
    public static final Property prefix = m_model.createProperty( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#prefix" );
    
    /** <p>Boolean flag for whether new ontology models will include the pre-declared 
     *  namespace prefixes</p>
     */
    public static final Property useDeclaredNsPrefixes = m_model.createProperty( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#useDeclaredNsPrefixes" );
    
    /** <p>Specifies URL that will never be loaded as the result of processing an imports 
     *  statement</p>
     */
    public static final Property ignoreImport = m_model.createProperty( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#ignoreImport" );
    
    /** <p>If true, this property denotes that the document manager should process the 
     *  imports closure of documents</p>
     */
    public static final Property processImports = m_model.createProperty( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#processImports" );
    
    /** <p>If true, this property denotes that loaded models should be cached for re-use</p> */
    public static final Property cacheModels = m_model.createProperty( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#cacheModels" );
    
    /** <p>The resolvable URL that an alternative copy of the ontology document may be 
     *  fetched from</p>
     */
    public static final Property altURL = m_model.createProperty( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#altURL" );
    

    // Vocabulary classes
    ///////////////////////////

    /** <p>A class of node that specifies document metadata for the DocumentManager</p> */
    public static final Resource OntologySpec = m_model.createResource( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#OntologySpec" );
    
    /** <p>A node that specifies behavioural options for the document manager</p> */
    public static final Resource DocumentManagerPolicy = m_model.createResource( "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#DocumentManagerPolicy" );
    

    // Vocabulary individuals
    ///////////////////////////

}
