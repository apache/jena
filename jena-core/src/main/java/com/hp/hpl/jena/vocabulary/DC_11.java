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

/* CVS $Id: DC_11.java,v 1.1 2009-06-29 08:55:36 castagna Exp $ */
package com.hp.hpl.jena.vocabulary;
 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from file:vocabularies/dublin-core_11.xml
 */
public class DC_11 {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://purl.org/dc/elements/1.1/";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>A name given to the resource.</p> */
    public static final Property title = m_model.createProperty( "http://purl.org/dc/elements/1.1/title" );
    
    /** <p>An entity primarily responsible for making the content of the resource.</p> */
    public static final Property creator = m_model.createProperty( "http://purl.org/dc/elements/1.1/creator" );
    
    /** <p>The topic of the content of the resource.</p> */
    public static final Property subject = m_model.createProperty( "http://purl.org/dc/elements/1.1/subject" );
    
    /** <p>An account of the content of the resource.</p> */
    public static final Property description = m_model.createProperty( "http://purl.org/dc/elements/1.1/description" );
    
    /** <p>An entity responsible for making the resource available</p> */
    public static final Property publisher = m_model.createProperty( "http://purl.org/dc/elements/1.1/publisher" );
    
    /** <p>An entity responsible for making contributions to the content of the resource.</p> */
    public static final Property contributor = m_model.createProperty( "http://purl.org/dc/elements/1.1/contributor" );
    
    /** <p>A date associated with an event in the life cycle of the resource.</p> */
    public static final Property date = m_model.createProperty( "http://purl.org/dc/elements/1.1/date" );
    
    /** <p>The nature or genre of the content of the resource.</p> */
    public static final Property type = m_model.createProperty( "http://purl.org/dc/elements/1.1/type" );
    
    /** <p>The physical or digital manifestation of the resource.</p> */
    public static final Property format = m_model.createProperty( "http://purl.org/dc/elements/1.1/format" );
    
    /** <p>An unambiguous reference to the resource within a given context.</p> */
    public static final Property identifier = m_model.createProperty( "http://purl.org/dc/elements/1.1/identifier" );
    
    /** <p>A reference to a resource from which the present resource is derived.</p> */
    public static final Property source = m_model.createProperty( "http://purl.org/dc/elements/1.1/source" );
    
    /** <p>A language of the intellectual content of the resource.</p> */
    public static final Property language = m_model.createProperty( "http://purl.org/dc/elements/1.1/language" );
    
    /** <p>A reference to a related resource.</p> */
    public static final Property relation = m_model.createProperty( "http://purl.org/dc/elements/1.1/relation" );
    
    /** <p>The extent or scope of the content of the resource.</p> */
    public static final Property coverage = m_model.createProperty( "http://purl.org/dc/elements/1.1/coverage" );
    
    /** <p>Information about rights held in and over the resource.</p> */
    public static final Property rights = m_model.createProperty( "http://purl.org/dc/elements/1.1/rights" );
    
}
