/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            16 Jan 2001
 * Filename           $RCSfile: DAML_OIL.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:21:27 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.vocabulary;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;

/**
 * <p>
 * Defines a vocabulary of RDF constants used by the latest release of DAML+oil
 * for class and property names.  The contract maintained by the DAML_OIL class
 * is that it will be periodically updated to reflect the latest release of DAML+OIL.
 * Currently this is the March 2001 release.  Older versions of the DAML+OIL
 * vocabulary, for compatability with older ontology documents, are maintained in
 * classes named <code>DAML_OIL_YYYY_MM</code>, for example DAML_OIL_2000_12.
 * </p>
 * <p>
 * <b>Note</b> that rudimentary support for multiple versions of DAML namespaces is
 * included in this release, by the mechanism of providing methods with the same
 * names as the static constants in the {@link DAMLVocabulary} interface.  This mechanism
 * is still under design review, and may change in future versions of Jena.
 * </p>
 * <p>
 * Note also that the capitalisation of
 * the constants herein is designed to follow normal practice in the RDF community, rather
 * than normal practice in the Java community. This is consistent with the capitalisation
 * of constants in Jena.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAML_OIL.java,v 1.10 2005-02-21 12:21:27 andy_seaborne Exp $
 */
public class DAML_OIL
    implements DAMLVocabulary
{
    // Constants
    //////////////////////////////////

    /** Singleton instance reference */
    private static DAML_OIL s_instance = new DAML_OIL();

    /** Model to hold the vocab resoures */
    private static Model s_model = ModelFactory.createDefaultModel();
    
    /* Resources */

    /** DAML namespace resource for the current release */
    public static final Resource NAMESPACE_DAML = s_model.createResource( NAMESPACE_DAML_2001_03_URI );

    /** DAML namespace resource for daml:collection. Note: strictly
     * daml:collection is only a string, not a resource in the DAML namespace. */
    public static final Resource collection = s_model.createResource( NAMESPACE_DAML_2001_03_URI+"collection" );

    /** RDF resource for DAML List class.   */
    public static final Resource List = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "List" );

    /** RDF resource for DAML UniqueProperty class */
    public static final Resource UniqueProperty = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "UniqueProperty" );

    /** RDF resource for DAML TransitiveProperty class */
    public static final Resource TransitiveProperty = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "TransitiveProperty" );

    /** RDF resource for DAML UnambiguousProperty class */
    public static final Resource UnambiguousProperty = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "UnambiguousProperty" );

    /** RDF resource for DAML Restriction class */
    public static final Resource Restriction = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "Restriction" );

    /** RDF resource for DAML Ontology class */
    public static final Resource Ontology = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "Ontology" );

    /** RDF resource for the nil (empty) list.  */
    public static final Resource nil = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "nil" );

    /** RDF resource for the top type (i.e. the super-type of all types).  */
    public static final Resource Thing = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "Thing" );

    /** RDF resource for the bottom type (i.e. the super-type of no types).  */
    public static final Resource Nothing = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "Nothing" );

    /** Alias for rdfs:Literal in the daml namespace.  */
    public static final Resource Literal = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "Literal" );

    /** RDF resource for DAML Class class (a DAML sub-class of rdfs:Class).   */
    public static final Resource Class = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "Class" );

    /** RDF resource for DAML Datatype class (a DAML sub-class of rdfs:Class).   */
    public static final Resource Datatype = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "Datatype" );

    /** RDF resource for DAML DatatypeProperty class (a DAML sub-class of rdf:Property).   */
    public static final Resource DatatypeProperty = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "DatatypeProperty" );

    /** RDF resource for DAML ObjectProperty class (a DAML sub-class of rdf:Property).   */
    public static final Resource ObjectProperty = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "ObjectProperty" );

    /** Alias for rdfs:Property in the daml namespace.  From 2001/03 onwards, use of ObjectProperty or DatatypeProperty is suggested. */
    public static final Resource Property = s_model.createResource( NAMESPACE_DAML_2001_03_URI + "Property" );


    /* Properties */

    /** RDF Property for the DAML versionInfo property */
    public static Property versionInfo = null;

    /** RDF Property for the DAML imports property on Ontologies */
    public static Property imports = null;

    /** RDF Property for the DAML disjointWith property on Classes */
    public static Property disjointWith = null;

    /** RDF Property for the DAML disjointUnionOf property on Classes */
    public static Property disjointUnionOf = null;

    /** RDF Property for the DAML sameClassAs property on Classes */
    public static Property sameClassAs = null;

    /** RDF Property for the DAML samePropertyAs property on Properties */
    public static Property samePropertyAs = null;

    /** RDF Property for the oneOf property on DAML class expressions */
    public static Property oneOf = null;

    /** RDF Property for the intersectionOf property on class expressions */
    public static Property intersectionOf = null;

    /** RDF Property for the unionOf property on class expressions  */
    public static Property unionOf = null;

    /** RDF Property for the complementOf property on class expressions */
    public static Property complementOf = null;

    /** RDF Property for the equivalentTo property on DAML values */
    public static Property equivalentTo = null;

    /** RDF Property for the DAML onProperty property on Restrictions */
    public static Property onProperty = null;

    /** RDF Property for the DAML toClass property on Restrictions */
    public static Property toClass = null;

    /** RDF Property for the DAML hasValue property on Restrictions */
    public static Property hasValue = null;

    /** RDF Property for the DAML hasClass property on Restrictions */
    public static Property hasClass = null;

    /** RDF Property for the DAML hasClassQ property on Restrictions */
    public static Property hasClassQ = null;

    /** RDF Property for the DAML cardinality property on Restrictions */
    public static Property cardinality = null;

    /** RDF Property for the DAML minCardinality property on Restrictions */
    public static Property minCardinality = null;

    /** RDF Property for the DAML maxCardinality property on Restrictions */
    public static Property maxCardinality = null;

    /** RDF Property for the DAML cardinalityQ property on Restrictions */
    public static Property cardinalityQ = null;

    /** RDF Property for the DAML minCardinalityQ property on Restrictions */
    public static Property minCardinalityQ = null;

    /** RDF Property for the DAML maxCardinalityQ property on Restrictions */
    public static Property maxCardinalityQ = null;

    /** RDF Property for the DAML inverseOf property on Properties */
    public static Property inverseOf = null;

    /** RDF Property for the DAML first property on Lists */
    public static Property first = null;

    /** RDF Property for the DAML rest property on Lists */
    public static Property rest = null;

    /** RDF Property for the DAML item property on Lists */
    public static Property item = null;

    /** Alias for rdfs:subPropertyOf in daml namespace */
    public static Property subPropertyOf = null;

    /** Alias for rdf:type in daml namespace */
    public static Property type = null;

    /** Alias for rdf:value in daml namespace */
    public static Property value = null;

    /** Alias for rdfs:subClassOf in daml namespace */
    public static Property subClassOf = null;

    /** Alias for rdfs:domain in daml namespace */
    public static Property domain = null;

    /** Alias for rdfs:range in daml namespace */
    public static Property range = null;

    /** Alias for rdfs:label in daml namespace */
    public static Property label = null;

    /** Alias for rdfs:comment in daml namespace */
    public static Property comment = null;

    /** Alias for rdfs:seeAlso in daml namespace */
    public static Property seeAlso = null;

    /** Alias for rdfs:isDefinedBy in daml namespace */
    public static Property isDefinedBy = null;

    /** RDF Property for the DAML sameIndividualAs property on instances */
    public static Property sameIndividualAs = null;

    /** RDF Property for the DAML differentIndvidualFrom property on instances */
    public static Property differentIndividualFrom = null;


    // Static variables
    //////////////////////////////////

    static {
            // properties:
            versionInfo       = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "versionInfo" );
            imports           = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "imports" );
            disjointWith      = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "disjointWith" );
            disjointUnionOf   = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "disjointUnionOf" );
            sameClassAs       = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "sameClassAs" );
            samePropertyAs    = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "samePropertyAs" );
            equivalentTo      = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "equivalentTo" );
            oneOf             = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "oneOf" );
            intersectionOf    = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "intersectionOf" );
            unionOf           = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "unionOf" );
            complementOf      = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "complementOf" );
            onProperty        = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "onProperty" );
            toClass           = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "toClass" );
            hasValue          = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "hasValue" );
            hasClass          = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "hasClass" );
            hasClassQ         = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "hasClassQ" );
            cardinality       = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "cardinality" );
            cardinalityQ      = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "cardinalityQ" );
            minCardinality    = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "minCardinality" );
            minCardinalityQ   = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "minCardinalityQ" );
            maxCardinality    = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "maxCardinality" );
            maxCardinalityQ   = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "maxCardinalityQ" );
            inverseOf         = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "inverseOf" );
            first             = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "first" );
            rest              = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "rest" );
            item              = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "item" );
            subPropertyOf     = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "subPropertyOf" );
            type              = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "type" );
            value             = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "value" );
            subClassOf        = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "subClassOf" );
            domain            = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "domain" );
            range             = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "range" );
            label             = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "label" );
            comment           = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "comment" );
            seeAlso           = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "seeAlso" );
            isDefinedBy       = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "isDefinedBy" );

            sameIndividualAs        = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "sameIndividualAs" );
            differentIndividualFrom = s_model.createProperty( NAMESPACE_DAML_2001_03_URI, "differentIndividualFrom" );
    }


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    /**
     * Constructor is private to ensure that only a single instance is available.
     */
    private DAML_OIL() {
    }


    // External signature methods
    //////////////////////////////////

    /**
     * Answer the namespace URI for a the current vocabulary. Note that this value is used to
     * construct the constants in the vocabulary, assuring that they are in the correct namespace
     * for this release.
     *
     * @return the namespace URI as a string.
     */
    public String getNamespace() {
        return NAMESPACE_DAML_2001_03_URI;
    }


    /**
     * Answer the singleton instance of this vocabulary
     *
     * @return The singleton instance
     */
    public static DAMLVocabulary getInstance() {
        return s_instance;
    }


    // The following methods provide the implementations for the DAMLVocabulary interface


    /** Answer the DAML namespace resource for the current release */
    public Resource NAMESPACE_DAML() { return NAMESPACE_DAML; }

    /** Answer the RDF resource for DAML List class.   */
    public Resource List() { return List; }

    /** Answer the RDF resource for DAML UniqueProperty class */
    public Resource UniqueProperty() { return UniqueProperty; }

    /** Answer the RDF resource for DAML TransitiveProperty class */
    public Resource TransitiveProperty() { return TransitiveProperty; }

    /** Answer the RDF resource for DAML UnambiguousProperty class */
    public Resource UnambiguousProperty() { return UnambiguousProperty; }

    /** Answer the RDF resource for DAML Restriction class */
    public Resource Restriction() { return Restriction; }

    /** Answer the RDF resource for DAML Ontology class */
    public Resource Ontology() { return Ontology; }

    /** Answer the RDF resource for the nil (empty) list.  */
    public Resource nil() { return nil; }

    /** Answer the RDF resource for the top type (i.e. the super-type of all types).  */
    public Resource Thing() { return Thing; }

    /** Answer the RDF resource for the bottom type (i.e. the super-type of no types).  */
    public Resource Nothing() { return Nothing; }

    /** Answer the Alias for rdfs:Literal in the daml namespace.  */
    public Resource Literal() { return Literal; }

    /** Answer the RDF resource for DAML Class class (a DAML sub-class of rdfs:Class).   */
    public Resource Class() { return Class; }

    /** Answer the RDF resource for DAML Datatype class (a DAML sub-class of rdfs:Class).   */
    public Resource Datatype() { return Datatype; }

    /** Answer the RDF resource for DAML DatatypeProperty class (a DAML sub-class of rdf:Property).   */
    public Resource DatatypeProperty() { return DatatypeProperty; }

    /** Answer the RDF resource for DAML ObjectProperty class (a DAML sub-class of rdf:Property).   */
    public Resource ObjectProperty() { return ObjectProperty; }


    /* Properties */

    /** Answer the RDF Property for the DAML versionInfo property */
    public Property versionInfo() { return versionInfo; }

    /** Answer the RDF Property for the DAML imports property on Ontologies */
    public Property imports() { return imports; }

    /** Answer the RDF Property for the DAML disjointWith property on Classes */
    public Property disjointWith() { return disjointWith; }

    /** Answer the RDF Property for the DAML disjointUnionOf property on Classes */
    public Property disjointUnionOf() { return disjointUnionOf; }

    /** Answer the RDF Property for the DAML sameClassAs property on Classes */
    public Property sameClassAs() { return sameClassAs; }

    /** Answer the RDF Property for the DAML samePropertyAs property on Properties */
    public Property samePropertyAs() { return samePropertyAs; }

    /** Answer the RDF Property for the oneOf property on DAML class expressions */
    public Property oneOf() { return oneOf; }

    /** Answer the RDF Property for the intersectionOf property on class expressions */
    public Property intersectionOf() { return intersectionOf; }

    /** Answer the RDF Property for the unionOf property on class expressions  */
    public Property unionOf() { return unionOf; }

    /** Answer the RDF Property for the complementOf property on class expressions */
    public Property complementOf() { return complementOf; }

    /** Answer the RDF Property for the equivalentTo property on DAML values */
    public Property equivalentTo() { return equivalentTo; }

    /** Answer the RDF Property for the DAML onProperty property on Restrictions */
    public Property onProperty() { return onProperty; }

    /** Answer the RDF Property for the DAML toClass property on Restrictions */
    public Property toClass() { return toClass; }

    /** Answer the RDF Property for the DAML hasValue property on Restrictions */
    public Property hasValue() { return hasValue; }

    /** Answer the RDF Property for the DAML hasClass property on Restrictions */
    public Property hasClass() { return hasClass; }

    /** Answer the RDF Property for the DAML hasClassQ property on Restrictions */
    public Property hasClassQ() { return hasClassQ; }

    /** Answer the RDF Property for the DAML cardinality property on Restrictions */
    public Property cardinality() { return cardinality; }

    /** Answer the RDF Property for the DAML minCardinality property on Restrictions */
    public Property minCardinality() { return minCardinality; }

    /** Answer the RDF Property for the DAML maxCardinality property on Restrictions */
    public Property maxCardinality() { return maxCardinality; }

    /** Answer the RDF Property for the DAML cardinalityQ property on Restrictions */
    public Property cardinalityQ() { return cardinalityQ; }

    /** Answer the RDF Property for the DAML minCardinalityQ property on Restrictions */
    public Property minCardinalityQ() { return minCardinalityQ; }

    /** Answer the RDF Property for the DAML maxCardinalityQ property on Restrictions */
    public Property maxCardinalityQ() { return maxCardinalityQ; }

    /** Answer the RDF Property for the DAML inverseOf property on Properties */
    public Property inverseOf() { return inverseOf; }

    /** Answer the RDF Property for the DAML first property on Lists */
    public Property first() { return first; }

    /** Answer the RDF Property for the DAML rest property on Lists */
    public Property rest() { return rest; }

    /** Answer the RDF Property for the DAML item property on Lists */
    public Property item() { return item; }

    /** Answer the alias for rdfs:subPropertyOf in daml namespace */
    public Property subPropertyOf() { return subPropertyOf; }

    /** Answer the alias for rdf:type in daml namespace */
    public Property type() { return type; }

    /** Answer the alias for rdf:value in daml namespace */
    public Property value() { return value; }

    /** Answer the alias for rdfs:subClassOf in daml namespace */
    public Property subClassOf() { return subClassOf; }

    /** Answer the alias for rdfs:domain in daml namespace */
    public Property domain() { return domain; }

    /** Answer the alias for rdfs:range in daml namespace */
    public Property range() { return range; }

    /** Answer the alias for rdfs:label in daml namespace */
    public Property label() { return label; }

    /** Answer the alias for rdfs:comment in daml namespace */
    public Property comment() { return comment; }

    /** Answer the alias for rdfs:seeAlso in daml namespace */
    public Property seeAlso() { return seeAlso; }

    /** Answer the alias for rdfs:isDefinedBy in daml namespace */
    public Property isDefinedBy() { return isDefinedBy; }

    /** Answer the RDF Property for the DAML sameIndividualAs property on instances */
    public Property sameIndividualAs() { return sameIndividualAs; }

    /** Answer the RDF Property for the DAML differentIndvidualFrom property on instances */
    public Property differentIndividualFrom() { return differentIndividualFrom; }

    /** Answer the alias for rdfs:Property in the daml namespace.  From 2001/03 onwards, it is preferable to use either DatatypeProperty or ObjectProperty. */
    public Resource Property() { return Property; }



}



/*
    (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

