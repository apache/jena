/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            16 Jan 2001
 * Filename           $RCSfile: DAML_OIL.java,v $
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
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.util.Log;

import com.hp.hpl.jena.ontology.daml.common.DAMLClassImpl;
import com.hp.hpl.jena.ontology.daml.common.DAMLPropertyImpl;
import com.hp.hpl.jena.ontology.daml.common.DAMLListImpl;

import com.hp.hpl.jena.ontology.daml.DAMLClass;
import com.hp.hpl.jena.ontology.daml.DAMLList;



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
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAML_OIL.java,v 1.2 2003-01-23 15:13:20 ian_dickinson Exp $
 */
public class DAML_OIL
    implements DAMLVocabulary
{
    // Constants
    //////////////////////////////////

    /** Singleton instance reference */
    private static DAML_OIL s_instance = new DAML_OIL();

    /* Resources */

    /** DAML namespace resource for the current release */
    public static final Resource NAMESPACE_DAML = new ResourceImpl( NAMESPACE_DAML_2001_03_URI );

    /** RDF resource for DAML List class.   */
    public static final DAMLClass List = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "List", null, s_instance );

    /** RDF resource for DAML UniqueProperty class */
    public static final DAMLClass UniqueProperty = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "UniqueProperty", null, s_instance );

    /** RDF resource for DAML TransitiveProperty class */
    public static final DAMLClass TransitiveProperty = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "TransitiveProperty", null, s_instance );

    /** RDF resource for DAML UnambiguousProperty class */
    public static final DAMLClass UnambiguousProperty = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "UnambiguousProperty", null, s_instance );

    /** RDF resource for DAML Restriction class */
    public static final DAMLClass Restriction = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "Restriction", null, s_instance );

    /** RDF resource for DAML Ontology class */
    public static final DAMLClass Ontology = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "Ontology", null, s_instance );

    /** RDF resource for the nil (empty) list.  */
    public static final DAMLList nil = new DAMLListImpl( NAMESPACE_DAML_2001_03_URI, "nil", null, s_instance );

    /** RDF resource for the top type (i.e. the super-type of all types).  */
    public static final DAMLClass Thing = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "Thing", null, s_instance );

    /** RDF resource for the bottom type (i.e. the super-type of no types).  */
    public static final DAMLClass Nothing = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "Nothing", null, s_instance );

    /** Alias for rdfs:Literal in the daml namespace.  */
    public static final DAMLClass Literal = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "Literal", null, s_instance );

    /** RDF resource for DAML Class class (a DAML sub-class of rdfs:Class).   */
    public static final DAMLClass Class = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "Class", null, s_instance );

    /** RDF resource for DAML Datatype class (a DAML sub-class of rdfs:Class).   */
    public static final DAMLClass Datatype = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "Datatype", null, s_instance );

    /** RDF resource for DAML DatatypeProperty class (a DAML sub-class of rdf:Property).   */
    public static final DAMLClass DatatypeProperty = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "DatatypeProperty", null, s_instance );

    /** RDF resource for DAML ObjectProperty class (a DAML sub-class of rdf:Property).   */
    public static final DAMLClass ObjectProperty = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "ObjectProperty", null, s_instance );

    /** Alias for rdfs:Property in the daml namespace.  From 2001/03 onwards, use of ObjectProperty or DatatypeProperty is suggested. */
    public static final DAMLClass Property = new DAMLClassImpl( NAMESPACE_DAML_2001_03_URI, "Property", null, s_instance );


    /* Deprecated resources */

    /**
     * RDF resource for DAML Disjoint class. Removed from 2001/03 onwards.
     * @deprecated Use {@link #disjointWith} or {@link #disjointUnionOf} instead.
     **/
    public static final DAMLClass Disjoint = null;


    /* Properties */

    /** RDF Property for the DAML versionInfo property */
    public static /* final */ Property versionInfo = null;

    /** RDF Property for the DAML imports property on Ontologies */
    public static /* final */ Property imports = null;

    /** RDF Property for the DAML disjointWith property on Classes */
    public static /* final */ Property disjointWith = null;

    /** RDF Property for the DAML disjointUnionOf property on Classes */
    public static /* final */ Property disjointUnionOf = null;

    /** RDF Property for the DAML sameClassAs property on Classes */
    public static /* final */ Property sameClassAs = null;

    /** RDF Property for the DAML samePropertyAs property on Properties */
    public static /* final */ Property samePropertyAs = null;

    /** RDF Property for the oneOf property on DAML class expressions */
    public static /* final */ Property oneOf = null;

    /** RDF Property for the intersectionOf property on class expressions */
    public static /* final */ Property intersectionOf = null;

    /** RDF Property for the unionOf property on class expressions  */
    public static /* final */ Property unionOf = null;

    /** RDF Property for the complementOf property on class expressions */
    public static /* final */ Property complementOf = null;

    /** RDF Property for the equivalentTo property on DAML values */
    public static /* final */ Property equivalentTo = null;

    /** RDF Property for the DAML onProperty property on Restrictions */
    public static /* final */ Property onProperty = null;

    /** RDF Property for the DAML toClass property on Restrictions */
    public static /* final */ Property toClass = null;

    /** RDF Property for the DAML hasValue property on Restrictions */
    public static /* final */ Property hasValue = null;

    /** RDF Property for the DAML hasClass property on Restrictions */
    public static /* final */ Property hasClass = null;

    /** RDF Property for the DAML hasClassQ property on Restrictions */
    public static /* final */ Property hasClassQ = null;

    /** RDF Property for the DAML cardinality property on Restrictions */
    public static /* final */ Property cardinality = null;

    /** RDF Property for the DAML minCardinality property on Restrictions */
    public static /* final */ Property minCardinality = null;

    /** RDF Property for the DAML maxCardinality property on Restrictions */
    public static /* final */ Property maxCardinality = null;

    /** RDF Property for the DAML cardinalityQ property on Restrictions */
    public static /* final */ Property cardinalityQ = null;

    /** RDF Property for the DAML minCardinalityQ property on Restrictions */
    public static /* final */ Property minCardinalityQ = null;

    /** RDF Property for the DAML maxCardinalityQ property on Restrictions */
    public static /* final */ Property maxCardinalityQ = null;

    /** RDF Property for the DAML inverseOf property on Properties */
    public static /* final */ Property inverseOf = null;

    /** RDF Property for the DAML first property on Lists */
    public static /* final */ Property first = null;

    /** RDF Property for the DAML rest property on Lists */
    public static /* final */ Property rest = null;

    /** RDF Property for the DAML item property on Lists */
    public static /* final */ Property item = null;

    /** Alias for rdfs:subPropertyOf in daml namespace */
    public static /* final */ Property subPropertyOf = null;

    /** Alias for rdf:type in daml namespace */
    public static /* final */ Property type = null;

    /** Alias for rdf:value in daml namespace */
    public static /* final */ Property value = null;

    /** Alias for rdfs:subClassOf in daml namespace */
    public static /* final */ Property subClassOf = null;

    /** Alias for rdfs:domain in daml namespace */
    public static /* final */ Property domain = null;

    /** Alias for rdfs:range in daml namespace */
    public static /* final */ Property range = null;

    /** Alias for rdfs:label in daml namespace */
    public static /* final */ Property label = null;

    /** Alias for rdfs:comment in daml namespace */
    public static /* final */ Property comment = null;

    /** Alias for rdfs:seeAlso in daml namespace */
    public static /* final */ Property seeAlso = null;

    /** Alias for rdfs:isDefinedBy in daml namespace */
    public static /* final */ Property isDefinedBy = null;

    /** RDF Property for the DAML sameIndividualAs property on instances */
    public static /* final */ Property sameIndividualAs = null;

    /** RDF Property for the DAML differentIndvidualFrom property on instances */
    public static /* final */ Property differentIndividualFrom = null;


    // Static variables
    //////////////////////////////////

    static {
        try {
            // properties:
            versionInfo       = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "versionInfo",                     null, null );
            imports           = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "imports",                         null, null );
            disjointWith      = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "disjointWith",                    null, null );
            disjointUnionOf   = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "disjointUnionOf",                 null, null );
            sameClassAs       = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "sameClassAs",                     null, null );
            samePropertyAs    = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "samePropertyAs",                  null, null );
            equivalentTo      = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "equivalentTo",                    null, null );
            oneOf             = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "oneOf",                           null, null );
            intersectionOf    = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "intersectionOf",                  null, null );
            unionOf           = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "unionOf",                         null, null );
            complementOf      = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "complementOf",                    null, null );
            onProperty        = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "onProperty",                      null, null );
            toClass           = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "toClass",                         null, null );
            hasValue          = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "hasValue",                        null, null );
            hasClass          = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "hasClass",                        null, null );
            hasClassQ         = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "hasClassQ",                       null, null );
            cardinality       = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "cardinality",                     null, null );
            cardinalityQ      = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "cardinalityQ",                    null, null );
            minCardinality    = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "minCardinality",                  null, null );
            minCardinalityQ   = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "minCardinalityQ",                 null, null );
            maxCardinality    = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "maxCardinality",                  null, null );
            maxCardinalityQ   = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "maxCardinalityQ",                 null, null );
            inverseOf         = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "inverseOf",                       null, null );
            first             = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "first",                           null, null );
            rest              = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "rest",                            null, null );
            item              = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "item",                            null, null );
            subPropertyOf     = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "subPropertyOf",                   null, null );
            type              = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "type",                            null, null );
            value             = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "value",                           null, null );
            subClassOf        = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "subClassOf",                      null, null );
            domain            = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "domain",                          null, null );
            range             = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "range",                           null, null );
            label             = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "label",                           null, null );
            comment           = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "comment",                         null, null );
            seeAlso           = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "seeAlso",                         null, null );
            isDefinedBy       = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "isDefinedBy",                     null, null );

            sameIndividualAs        = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "sameIndividualAs", null,    null );
            differentIndividualFrom = new DAMLPropertyImpl( NAMESPACE_DAML_2001_03_URI, "differentIndividualFrom",   null, null );
        }
        catch (Exception e) {
            // shouldn't happen
            Log.severe( "Error while creating vocabulary: " + e, e );
            throw new RuntimeException( "RDF Exception while creating vocabulary: " + e );
        }
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
    public DAMLClass List() { return List; }

    /** Answer the RDF resource for DAML UniqueProperty class */
    public DAMLClass UniqueProperty() { return UniqueProperty; }

    /** Answer the RDF resource for DAML TransitiveProperty class */
    public DAMLClass TransitiveProperty() { return TransitiveProperty; }

    /** Answer the RDF resource for DAML UnambiguousProperty class */
    public DAMLClass UnambiguousProperty() { return UnambiguousProperty; }

    /** Answer the RDF resource for DAML Restriction class */
    public DAMLClass Restriction() { return Restriction; }

    /** Answer the RDF resource for DAML Ontology class */
    public DAMLClass Ontology() { return Ontology; }

    /** Answer the RDF resource for the nil (empty) list.  */
    public DAMLList nil() { return nil; }

    /** Answer the RDF resource for the top type (i.e. the super-type of all types).  */
    public DAMLClass Thing() { return Thing; }

    /** Answer the RDF resource for the bottom type (i.e. the super-type of no types).  */
    public DAMLClass Nothing() { return Nothing; }

    /** Answer the Alias for rdfs:Literal in the daml namespace.  */
    public DAMLClass Literal() { return Literal; }

    /** Answer the RDF resource for DAML Class class (a DAML sub-class of rdfs:Class).   */
    public DAMLClass Class() { return Class; }

    /** Answer the RDF resource for DAML Datatype class (a DAML sub-class of rdfs:Class).   */
    public DAMLClass Datatype() { return Datatype; }

    /** Answer the RDF resource for DAML DatatypeProperty class (a DAML sub-class of rdf:Property).   */
    public DAMLClass DatatypeProperty() { return DatatypeProperty; }

    /** Answer the RDF resource for DAML ObjectProperty class (a DAML sub-class of rdf:Property).   */
    public DAMLClass ObjectProperty() { return ObjectProperty; }


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
    public DAMLClass Property() { return Property; }


    /* Deprecated resources and properties */

    /**
     * RDF resource for DAML Disjoint class. Removed from 2001/03 onwards.
     * @deprecated Use {@link #disjointWith} or {@link #disjointUnionOf} instead.
     **/
    public DAMLClass Disjoint() { return Disjoint; }




}

