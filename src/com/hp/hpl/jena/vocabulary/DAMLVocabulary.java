/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            16 Jan 2001
 * Filename           $RCSfile: DAMLVocabulary.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-01-23 15:13:20 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright Hewlett-Packard Company 2001
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.vocabulary;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.ontology.daml.DAMLClass;
import com.hp.hpl.jena.ontology.daml.DAMLList;



/**
 * <p>
 * A marker interface for a DAML vocabulary, that will, in future, migrate towards
 * providing support for versioning DAML and RDF namespaces, and, specifically,
 * supporting multiple DAML vocabularies for DAML terms.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLVocabulary.java,v 1.2 2003-01-23 15:13:20 ian_dickinson Exp $
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
    public DAMLClass List();

    /** Answer the RDF resource for DAML UniqueProperty class */
    public DAMLClass UniqueProperty();

    /** Answer the RDF resource for DAML TransitiveProperty class */
    public DAMLClass TransitiveProperty();

    /** Answer the RDF resource for DAML UnambiguousProperty class */
    public DAMLClass UnambiguousProperty();

    /** Answer the RDF resource for DAML Restriction class */
    public DAMLClass Restriction();

    /** Answer the RDF resource for DAML Ontology class */
    public DAMLClass Ontology();

    /** Answer the RDF resource for the nil (empty) list.  */
    public DAMLList nil();

    /** Answer the RDF resource for the top type (i.e. the super-type of all types).  */
    public DAMLClass Thing();

    /** Answer the RDF resource for the bottom type (i.e. the super-type of no types).  */
    public DAMLClass Nothing();

    /** Answer the Alias for rdfs:Literal in the daml namespace.  */
    public DAMLClass Literal();

    /** Answer the RDF resource for DAML Class class (a DAML sub-class of rdfs:Class).   */
    public DAMLClass Class();

    /** Answer the RDF resource for DAML Datatype class (a DAML sub-class of rdfs:Class).   */
    public DAMLClass Datatype();

    /** Answer the RDF resource for DAML DatatypeProperty class (a DAML sub-class of rdf:Property).   */
    public DAMLClass DatatypeProperty();

    /** Answer the RDF resource for DAML ObjectProperty class (a DAML sub-class of rdf:Property).   */
    public DAMLClass ObjectProperty();

    /** Answer the RDF resource for DAML Disjoint class. */
    public DAMLClass Disjoint();

    /** Answer the Alias for rdfs:Property in the daml namespace. */
    public DAMLClass Property();


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

