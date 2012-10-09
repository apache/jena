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
///////////////
package com.hp.hpl.jena.vocabulary;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;




/**
 * <p>
 * A marker interface for a DAML vocabulary, that will, in future, migrate towards
 * providing support for versioning DAML and RDF namespaces, and, specifically,
 * supporting multiple DAML vocabularies for DAML terms.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:ian_dickinson@users.sourceforge.net">email</a>)
 * @version CVS info: $Id: DAMLVocabulary.java,v 1.2 2009-10-06 13:04:43 ian_dickinson Exp $
 * @deprecated This vocabulary will be removed from future versions of Jena because it is obsolete.
 */
public interface DAMLVocabulary
{
    /* URI's */

    /** DAML namespace URI for the March 2001 release */
    public static final String NAMESPACE_DAML_2001_03_URI = "http://www.daml.org/2001/03/daml+oil#";

    /** DAML namespace URI for the December 2000 release */
    public static final String NAMESPACE_DAML_2000_12_URI = "http://www.daml.org/2000/12/daml+oil#";


    /* Resources */

    /** Answer the DAML namespace resource for the current release */
    public Resource NAMESPACE_DAML();

    /** Answer the RDF resource for DAML List class.   */
    public Resource List();

    /** Answer the RDF resource for DAML UniqueProperty class */
    public Resource UniqueProperty();

    /** Answer the RDF resource for DAML TransitiveProperty class */
    public Resource TransitiveProperty();

    /** Answer the RDF resource for DAML UnambiguousProperty class */
    public Resource UnambiguousProperty();

    /** Answer the RDF resource for DAML Restriction class */
    public Resource Restriction();

    /** Answer the RDF resource for DAML Ontology class */
    public Resource Ontology();

    /** Answer the RDF resource for the nil (empty) list.  */
    public Resource nil();

    /** Answer the RDF resource for the top type (i.e. the super-type of all types).  */
    public Resource Thing();

    /** Answer the RDF resource for the bottom type (i.e. the super-type of no types).  */
    public Resource Nothing();

    /** Answer the Alias for rdfs:Literal in the daml namespace.  */
    public Resource Literal();

    /** Answer the RDF resource for DAML Class class (a DAML sub-class of rdfs:Class).   */
    public Resource Class();

    /** Answer the RDF resource for DAML Datatype class (a DAML sub-class of rdfs:Class).   */
    public Resource Datatype();

    /** Answer the RDF resource for DAML DatatypeProperty class (a DAML sub-class of rdf:Property).   */
    public Resource DatatypeProperty();

    /** Answer the RDF resource for DAML ObjectProperty class (a DAML sub-class of rdf:Property).   */
    public Resource ObjectProperty();

    /** Answer the Alias for rdfs:Property in the daml namespace. */
    public Resource Property();


    /* Properties */

    /** Answer the RDF Property for the DAML versionInfo property */
    public Property versionInfo();

    /** Answer the RDF Property for the DAML imports property on Ontologies */
    public Property imports();

    /** Answer the RDF Property for the DAML disjointWith property on Classes */
    public Property disjointWith();

    /** Answer the RDF Property for the DAML disjointUnionOf property on Classes */
    public Property disjointUnionOf();

    /** Answer the RDF Property for the DAML sameClassAs property on Classes */
    public Property sameClassAs();

    /** Answer the RDF Property for the DAML samePropertyAs property on Properties */
    public Property samePropertyAs();

    /** Answer the RDF Property for the oneOf property on DAML class expressions */
    public Property oneOf();

    /** Answer the RDF Property for the intersectionOf property on class expressions */
    public Property intersectionOf();

    /** Answer the RDF Property for the unionOf property on class expressions  */
    public Property unionOf();

    /** Answer the RDF Property for the complementOf property on class expressions */
    public Property complementOf();

    /** Answer the RDF Property for the equivalentTo property on DAML values */
    public Property equivalentTo();

    /** Answer the RDF Property for the DAML onProperty property on Restrictions */
    public Property onProperty();

    /** Answer the RDF Property for the DAML toClass property on Restrictions */
    public Property toClass();

    /** Answer the RDF Property for the DAML hasValue property on Restrictions */
    public Property hasValue();

    /** Answer the RDF Property for the DAML hasClass property on Restrictions */
    public Property hasClass();

    /** Answer the RDF Property for the DAML hasClassQ property on Restrictions */
    public Property hasClassQ();

    /** Answer the RDF Property for the DAML cardinality property on Restrictions */
    public Property cardinality();

    /** Answer the RDF Property for the DAML minCardinality property on Restrictions */
    public Property minCardinality();

    /** Answer the RDF Property for the DAML maxCardinality property on Restrictions */
    public Property maxCardinality();

    /** Answer the RDF Property for the DAML cardinalityQ property on Restrictions */
    public Property cardinalityQ();

    /** Answer the RDF Property for the DAML minCardinalityQ property on Restrictions */
    public Property minCardinalityQ();

    /** Answer the RDF Property for the DAML maxCardinalityQ property on Restrictions */
    public Property maxCardinalityQ();

    /** Answer the RDF Property for the DAML inverseOf property on Properties */
    public Property inverseOf();

    /** Answer the RDF Property for the DAML first property on Lists */
    public Property first();

    /** Answer the RDF Property for the DAML rest property on Lists */
    public Property rest();

    /** Answer the RDF Property for the DAML item property on Lists */
    public Property item();

    /** Answer the Alias for rdfs:subPropertyOf in daml namespace */
    public Property subPropertyOf();

    /** Answer the Alias for rdf:type in daml namespace */
    public Property type();

    /** Answer the Alias for rdf:value in daml namespace */
    public Property value();

    /** Answer the Alias for rdfs:subClassOf in daml namespace */
    public Property subClassOf();

    /** Answer the Alias for rdfs:domain in daml namespace */
    public Property domain();

    /** Answer the Alias for rdfs:range in daml namespace */
    public Property range();

    /** Answer the Alias for rdfs:label in daml namespace */
    public Property label();

    /** Answer the Alias for rdfs:comment in daml namespace */
    public Property comment();

    /** Answer the Alias for rdfs:seeAlso in daml namespace */
    public Property seeAlso();

    /** Answer the Alias for rdfs:isDefinedBy in daml namespace */
    public Property isDefinedBy();

    /** Answer the RDF Property for the DAML sameIndividualAs property on instances */
    public Property sameIndividualAs();

    /** Answer the RDF Property for the DAML differentIndvidualFrom property on instances */
    public Property differentIndividualFrom();


}
