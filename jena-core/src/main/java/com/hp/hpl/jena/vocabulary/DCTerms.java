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

package com.hp.hpl.jena.vocabulary; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from vocabularies/dublin-core_terms.xml
 */
public class DCTerms {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/dc/terms/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>A summary of the resource.</p> */
    public static final Property abstract_ = m_model.createProperty( "http://purl.org/dc/terms/abstract" );
    
    /** <p>Information about who can access the resource or an indication of its security 
     *  status.</p>
     */
    public static final Property accessRights = m_model.createProperty( "http://purl.org/dc/terms/accessRights" );
    
    /** <p>The method by which items are added to a collection.</p> */
    public static final Property accrualMethod = m_model.createProperty( "http://purl.org/dc/terms/accrualMethod" );
    
    /** <p>The frequency with which items are added to a collection.</p> */
    public static final Property accrualPeriodicity = m_model.createProperty( "http://purl.org/dc/terms/accrualPeriodicity" );
    
    /** <p>The policy governing the addition of items to a collection.</p> */
    public static final Property accrualPolicy = m_model.createProperty( "http://purl.org/dc/terms/accrualPolicy" );
    
    /** <p>An alternative name for the resource.</p> */
    public static final Property alternative = m_model.createProperty( "http://purl.org/dc/terms/alternative" );
    
    /** <p>A class of entity for whom the resource is intended or useful.</p> */
    public static final Property audience = m_model.createProperty( "http://purl.org/dc/terms/audience" );
    
    /** <p>Date (often a range) that the resource became or will become available.</p> */
    public static final Property available = m_model.createProperty( "http://purl.org/dc/terms/available" );
    
    /** <p>A bibliographic reference for the resource.</p> */
    public static final Property bibliographicCitation = m_model.createProperty( "http://purl.org/dc/terms/bibliographicCitation" );
    
    /** <p>An established standard to which the described resource conforms.</p> */
    public static final Property conformsTo = m_model.createProperty( "http://purl.org/dc/terms/conformsTo" );
    
    /** <p>An entity responsible for making contributions to the resource.</p> */
    public static final Property contributor = m_model.createProperty( "http://purl.org/dc/terms/contributor" );
    
    /** <p>The spatial or temporal topic of the resource, the spatial applicability of 
     *  the resource, or the jurisdiction under which the resource is relevant.</p>
     */
    public static final Property coverage = m_model.createProperty( "http://purl.org/dc/terms/coverage" );
    
    /** <p>Date of creation of the resource.</p> */
    public static final Property created = m_model.createProperty( "http://purl.org/dc/terms/created" );
    
    /** <p>An entity primarily responsible for making the resource.</p> */
    public static final Property creator = m_model.createProperty( "http://purl.org/dc/terms/creator" );
    
    /** <p>A point or period of time associated with an event in the lifecycle of the 
     *  resource.</p>
     */
    public static final Property date = m_model.createProperty( "http://purl.org/dc/terms/date" );
    
    /** <p>Date of acceptance of the resource.</p> */
    public static final Property dateAccepted = m_model.createProperty( "http://purl.org/dc/terms/dateAccepted" );
    
    /** <p>Date of copyright.</p> */
    public static final Property dateCopyrighted = m_model.createProperty( "http://purl.org/dc/terms/dateCopyrighted" );
    
    /** <p>Date of submission of the resource.</p> */
    public static final Property dateSubmitted = m_model.createProperty( "http://purl.org/dc/terms/dateSubmitted" );
    
    /** <p>An account of the resource.</p> */
    public static final Property description = m_model.createProperty( "http://purl.org/dc/terms/description" );
    
    /** <p>A class of entity, defined in terms of progression through an educational 
     *  or training context, for which the described resource is intended.</p>
     */
    public static final Property educationLevel = m_model.createProperty( "http://purl.org/dc/terms/educationLevel" );
    
    /** <p>The size or duration of the resource.</p> */
    public static final Property extent = m_model.createProperty( "http://purl.org/dc/terms/extent" );
    
    /** <p>The file format, physical medium, or dimensions of the resource.</p> */
    public static final Property format = m_model.createProperty( "http://purl.org/dc/terms/format" );
    
    /** <p>A related resource that is substantially the same as the pre-existing described 
     *  resource, but in another format.</p>
     */
    public static final Property hasFormat = m_model.createProperty( "http://purl.org/dc/terms/hasFormat" );
    
    /** <p>A related resource that is included either physically or logically in the 
     *  described resource.</p>
     */
    public static final Property hasPart = m_model.createProperty( "http://purl.org/dc/terms/hasPart" );
    
    /** <p>A related resource that is a version, edition, or adaptation of the described 
     *  resource.</p>
     */
    public static final Property hasVersion = m_model.createProperty( "http://purl.org/dc/terms/hasVersion" );
    
    /** <p>An unambiguous reference to the resource within a given context.</p> */
    public static final Property identifier = m_model.createProperty( "http://purl.org/dc/terms/identifier" );
    
    /** <p>A process, used to engender knowledge, attitudes and skills, that the described 
     *  resource is designed to support.</p>
     */
    public static final Property instructionalMethod = m_model.createProperty( "http://purl.org/dc/terms/instructionalMethod" );
    
    /** <p>A related resource that is substantially the same as the described resource, 
     *  but in another format.</p>
     */
    public static final Property isFormatOf = m_model.createProperty( "http://purl.org/dc/terms/isFormatOf" );
    
    /** <p>A related resource in which the described resource is physically or logically 
     *  included.</p>
     */
    public static final Property isPartOf = m_model.createProperty( "http://purl.org/dc/terms/isPartOf" );
    
    /** <p>A related resource that references, cites, or otherwise points to the described 
     *  resource.</p>
     */
    public static final Property isReferencedBy = m_model.createProperty( "http://purl.org/dc/terms/isReferencedBy" );
    
    /** <p>A related resource that supplants, displaces, or supersedes the described 
     *  resource.</p>
     */
    public static final Property isReplacedBy = m_model.createProperty( "http://purl.org/dc/terms/isReplacedBy" );
    
    /** <p>A related resource that requires the described resource to support its function, 
     *  delivery, or coherence.</p>
     */
    public static final Property isRequiredBy = m_model.createProperty( "http://purl.org/dc/terms/isRequiredBy" );
    
    /** <p>A related resource of which the described resource is a version, edition, 
     *  or adaptation.</p>
     */
    public static final Property isVersionOf = m_model.createProperty( "http://purl.org/dc/terms/isVersionOf" );
    
    /** <p>Date of formal issuance (e.g., publication) of the resource.</p> */
    public static final Property issued = m_model.createProperty( "http://purl.org/dc/terms/issued" );
    
    /** <p>A language of the resource.</p> */
    public static final Property language = m_model.createProperty( "http://purl.org/dc/terms/language" );
    
    /** <p>A legal document giving official permission to do something with the resource.</p> */
    public static final Property license = m_model.createProperty( "http://purl.org/dc/terms/license" );
    
    /** <p>An entity that mediates access to the resource and for whom the resource is 
     *  intended or useful.</p>
     */
    public static final Property mediator = m_model.createProperty( "http://purl.org/dc/terms/mediator" );
    
    /** <p>The material or physical carrier of the resource.</p> */
    public static final Property medium = m_model.createProperty( "http://purl.org/dc/terms/medium" );
    
    /** <p>Date on which the resource was changed.</p> */
    public static final Property modified = m_model.createProperty( "http://purl.org/dc/terms/modified" );
    
    /** <p>A statement of any changes in ownership and custody of the resource since 
     *  its creation that are significant for its authenticity, integrity, and interpretation.</p>
     */
    public static final Property provenance = m_model.createProperty( "http://purl.org/dc/terms/provenance" );
    
    /** <p>An entity responsible for making the resource available.</p> */
    public static final Property publisher = m_model.createProperty( "http://purl.org/dc/terms/publisher" );
    
    /** <p>A related resource that is referenced, cited, or otherwise pointed to by the 
     *  described resource.</p>
     */
    public static final Property references = m_model.createProperty( "http://purl.org/dc/terms/references" );
    
    /** <p>A related resource.</p> */
    public static final Property relation = m_model.createProperty( "http://purl.org/dc/terms/relation" );
    
    /** <p>A related resource that is supplanted, displaced, or superseded by the described 
     *  resource.</p>
     */
    public static final Property replaces = m_model.createProperty( "http://purl.org/dc/terms/replaces" );
    
    /** <p>A related resource that is required by the described resource to support its 
     *  function, delivery, or coherence.</p>
     */
    public static final Property requires = m_model.createProperty( "http://purl.org/dc/terms/requires" );
    
    /** <p>Information about rights held in and over the resource.</p> */
    public static final Property rights = m_model.createProperty( "http://purl.org/dc/terms/rights" );
    
    /** <p>A person or organization owning or managing rights over the resource.</p> */
    public static final Property rightsHolder = m_model.createProperty( "http://purl.org/dc/terms/rightsHolder" );
    
    /** <p>A related resource from which the described resource is derived.</p> */
    public static final Property source = m_model.createProperty( "http://purl.org/dc/terms/source" );
    
    /** <p>Spatial characteristics of the resource.</p> */
    public static final Property spatial = m_model.createProperty( "http://purl.org/dc/terms/spatial" );
    
    /** <p>The topic of the resource.</p> */
    public static final Property subject = m_model.createProperty( "http://purl.org/dc/terms/subject" );
    
    /** <p>A list of subunits of the resource.</p> */
    public static final Property tableOfContents = m_model.createProperty( "http://purl.org/dc/terms/tableOfContents" );
    
    /** <p>Temporal characteristics of the resource.</p> */
    public static final Property temporal = m_model.createProperty( "http://purl.org/dc/terms/temporal" );
    
    public static final Property title = m_model.createProperty( "http://purl.org/dc/terms/title" );
    
    /** <p>The nature or genre of the resource.</p> */
    public static final Property type = m_model.createProperty( "http://purl.org/dc/terms/type" );
    
    /** <p>Date (often a range) of validity of a resource.</p> */
    public static final Property valid = m_model.createProperty( "http://purl.org/dc/terms/valid" );
    
    /** <p>A resource that acts or has the power to act.</p> */
    public static final Resource Agent = m_model.createResource( "http://purl.org/dc/terms/Agent" );
    
    /** <p>A group of agents.</p> */
    public static final Resource AgentClass = m_model.createResource( "http://purl.org/dc/terms/AgentClass" );
    
    /** <p>A book, article, or other documentary resource.</p> */
    public static final Resource BibliographicResource = m_model.createResource( "http://purl.org/dc/terms/BibliographicResource" );
    
    /** <p>A digital resource format.</p> */
    public static final Resource FileFormat = m_model.createResource( "http://purl.org/dc/terms/FileFormat" );
    
    /** <p>A rate at which something recurs.</p> */
    public static final Resource Frequency = m_model.createResource( "http://purl.org/dc/terms/Frequency" );
    
    /** <p>The extent or range of judicial, law enforcement, or other authority.</p> */
    public static final Resource Jurisdiction = m_model.createResource( "http://purl.org/dc/terms/Jurisdiction" );
    
    /** <p>A legal document giving official permission to do something with a Resource.</p> */
    public static final Resource LicenseDocument = m_model.createResource( "http://purl.org/dc/terms/LicenseDocument" );
    
    /** <p>A system of signs, symbols, sounds, gestures, or rules used in communication.</p> */
    public static final Resource LinguisticSystem = m_model.createResource( "http://purl.org/dc/terms/LinguisticSystem" );
    
    /** <p>A spatial region or named place.</p> */
    public static final Resource Location = m_model.createResource( "http://purl.org/dc/terms/Location" );
    
    /** <p>A location, period of time, or jurisdiction.</p> */
    public static final Resource LocationPeriodOrJurisdiction = m_model.createResource( "http://purl.org/dc/terms/LocationPeriodOrJurisdiction" );
    
    /** <p>A file format or physical medium.</p> */
    public static final Resource MediaType = m_model.createResource( "http://purl.org/dc/terms/MediaType" );
    
    /** <p>A media type or extent.</p> */
    public static final Resource MediaTypeOrExtent = m_model.createResource( "http://purl.org/dc/terms/MediaTypeOrExtent" );
    
    /** <p>A method by which resources are added to a collection.</p> */
    public static final Resource MethodOfAccrual = m_model.createResource( "http://purl.org/dc/terms/MethodOfAccrual" );
    
    /** <p>A process that is used to engender knowledge, attitudes, and skills.</p> */
    public static final Resource MethodOfInstruction = m_model.createResource( "http://purl.org/dc/terms/MethodOfInstruction" );
    
    /** <p>An interval of time that is named or defined by its start and end dates.</p> */
    public static final Resource PeriodOfTime = m_model.createResource( "http://purl.org/dc/terms/PeriodOfTime" );
    
    /** <p>A physical material or carrier.</p> */
    public static final Resource PhysicalMedium = m_model.createResource( "http://purl.org/dc/terms/PhysicalMedium" );
    
    /** <p>A material thing.</p> */
    public static final Resource PhysicalResource = m_model.createResource( "http://purl.org/dc/terms/PhysicalResource" );
    
    /** <p>A plan or course of action by an authority, intended to influence and determine 
     *  decisions, actions, and other matters.</p>
     */
    public static final Resource Policy = m_model.createResource( "http://purl.org/dc/terms/Policy" );
    
    /** <p>A statement of any changes in ownership and custody of a resource since its 
     *  creation that are significant for its authenticity, integrity, and interpretation.</p>
     */
    public static final Resource ProvenanceStatement = m_model.createResource( "http://purl.org/dc/terms/ProvenanceStatement" );
    
    /** <p>A statement about the intellectual property rights (IPR) held in or over a 
     *  Resource, a legal document giving official permission to do something with 
     *  a resource, or a statement about access rights.</p>
     */
    public static final Resource RightsStatement = m_model.createResource( "http://purl.org/dc/terms/RightsStatement" );
    
    /** <p>A dimension or extent, or a time taken to play or execute.</p> */
    public static final Resource SizeOrDuration = m_model.createResource( "http://purl.org/dc/terms/SizeOrDuration" );
    
    /** <p>A basis for comparison; a reference point against which other things can be 
     *  evaluated.</p>
     */
    public static final Resource Standard = m_model.createResource( "http://purl.org/dc/terms/Standard" );
    
}
