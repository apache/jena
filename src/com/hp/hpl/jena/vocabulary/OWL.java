/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            3 Mar 2003
 * Filename           $RCSfile: OWL.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-04 17:48:56 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
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
 * A vocabulary class providing static constants for the 
 * <a href="http://www.w3.org/2002/07/owl">OWL</a> 
 * language elements
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OWL.java,v 1.4 2003-03-04 17:48:56 ian_dickinson Exp $
 */
public class OWL
{
    // Constants
    //////////////////////////////////

    /** Model to hold the vocabulary resources only */
    private static Model m_vocabModel = ModelFactory.createDefaultModel();
    
    /** URI denoting the OWL namespace, currently <code>http://www.w3.org/2002/07/owl#</code> */
    public static final String NAMESPACE = "http://www.w3.org/2002/07/owl#";
    
    /** Resource denoting the class of OWL classes */
    public static final Resource Class                        = m_vocabModel.createResource( OWL.NAMESPACE + "Class"                     );
    
    /** Resource denoting the class of OWL restrictions */
    public static final Resource Restriction                  = m_vocabModel.createResource( OWL.NAMESPACE + "Restriction"               );
    
    /** Resource denoting the universal set containing all OWL individuals */
    public static final Resource Thing                        = m_vocabModel.createResource( OWL.NAMESPACE + "Thing"                     );
    
    /** Resource denoting the empty set containing no OWL individuals */
    public static final Resource Nothing                      = m_vocabModel.createResource( OWL.NAMESPACE + "Nothing"                   );
    
    /** Resource denoting the class of OWL properties that have individuals as their range */
    public static final Resource ObjectProperty               = m_vocabModel.createResource( OWL.NAMESPACE + "ObjectProperty"            );
    
    /** Resource denoting the class of OWL properties that have typed literals as their range */
    public static final Resource DatatypeProperty             = m_vocabModel.createResource( OWL.NAMESPACE + "DatatypeProperty"          );
    
    /** Resource denoting the class of OWL properties that are transitive */
    public static final Resource TransitiveProperty           = m_vocabModel.createResource( OWL.NAMESPACE + "TransitiveProperty"        );
    
    /** Resource denoting the class of OWL properties that are symmetric */
    public static final Resource SymmetricProperty            = m_vocabModel.createResource( OWL.NAMESPACE + "SymmetricProperty"         );
    
    /** Resource denoting the class of OWL properties that are functional (have a unique range element for a given domain element) */
    public static final Resource FunctionalProperty           = m_vocabModel.createResource( OWL.NAMESPACE + "FunctionalProperty"        );
    
    /** Resource denoting the class of OWL properties that are inverse functional (have a unique domain element for a given range element) */
    public static final Resource InverseFunctionalProperty    = m_vocabModel.createResource( OWL.NAMESPACE + "InverseFunctionalProperty" );
    
    /** Resource denoting the class of OWL axioms stating that a set of classes are pairwise disjoint */
    public static final Resource AllDifferent                 = m_vocabModel.createResource( OWL.NAMESPACE + "AllDifferent"              );
    
    /** Resource denoting the class of OWL ontology nodes */
    public static final Resource Ontology                     = m_vocabModel.createResource( OWL.NAMESPACE + "Ontology"                  );
    
    /** Resource denoting the class of OWL classes that have been deprecated */
    public static final Resource DeprecatedClass              = m_vocabModel.createResource( OWL.NAMESPACE + "DeprecatedClass"           );
    
    /** Resource denoting the class of OWL properties that have been deprecated */
    public static final Resource DeprecatedProperty           = m_vocabModel.createResource( OWL.NAMESPACE + "DeprecatedProperty"        );
    
    
    /** Property denoting the OWL predicate that states that two given properties are equivalent */
    public static final Property samePropertyAs               = m_vocabModel.createProperty( OWL.NAMESPACE, "samePropertyAs"         );
    
    /** Property denoting the OWL predicate that states that two given classes are equivalent */
    public static final Property sameClassAs                  = m_vocabModel.createProperty( OWL.NAMESPACE, "sameClassAs"            );
    
    /** Property denoting the OWL predicate that states that one class has no instances in common with another */
    public static final Property disjointWith                 = m_vocabModel.createProperty( OWL.NAMESPACE, "disjointWith"               );
    
    /** Property denoting the OWL predicate that states that two given indviduals are the same object */
    public static final Property sameIndividualAs             = m_vocabModel.createProperty( OWL.NAMESPACE, "sameIndividualAs"           );
    
    /** Property denoting the OWL predicate that states that two given values are equivalent */
    public static final Property sameAs                       = m_vocabModel.createProperty( OWL.NAMESPACE, "sameAs"                     );
    
    /** Property denoting the OWL predicate that states that two given values are different */
    public static final Property differentFrom                = m_vocabModel.createProperty( OWL.NAMESPACE, "differentFrom"              );
    
    /** Property denoting the OWL predicate that states that two given classes have different members */
    public static final Property distinctMembers              = m_vocabModel.createProperty( OWL.NAMESPACE, "distinctMembers"            );
    
    /** Property denoting the OWL predicate that states that a given class is same as the union of a list of give class descriptions */
    public static final Property unionOf                      = m_vocabModel.createProperty( OWL.NAMESPACE, "unionOf"                    );
    
    /** Property denoting the OWL predicate that states that a given class is same as the intersection of a list of give class descriptions */
    public static final Property intersectionOf               = m_vocabModel.createProperty( OWL.NAMESPACE, "intersectionOf"             );
    
    /** Property denoting the OWL predicate that states that a given class is the complement of another class description */
    public static final Property complementOf                 = m_vocabModel.createProperty( OWL.NAMESPACE, "complementOf"               );
    
    /** Property denoting the OWL predicate that states that a given class is constructed from a closed enumeration of values */
    public static final Property oneOf                        = m_vocabModel.createProperty( OWL.NAMESPACE, "oneOf"                      );
    
    /** Property denoting the OWL predicate that states that names the property that a property restriction applies to */
    public static final Property onProperty                   = m_vocabModel.createProperty( OWL.NAMESPACE, "onProperty"                 );
    
    /** Property denoting the OWL predicate that states that a class is in a given restriction if all values of the restricted property are in the given class  */
    public static final Property allValuesFrom                = m_vocabModel.createProperty( OWL.NAMESPACE, "allValuesFrom"              );
    
    /** Property denoting the OWL predicate that nominates the value the restricted property must have for the class to be in the restriction */
    public static final Property hasValue                     = m_vocabModel.createProperty( OWL.NAMESPACE, "hasValue"                   );
    
    /** Property denoting the OWL predicate that states that a class is in a given restriction if at least one value of the restricted property is in the given class  */
    public static final Property someValuesFrom               = m_vocabModel.createProperty( OWL.NAMESPACE, "someValuesFrom"             );
    
    /** Property denoting the OWL predicate that states that a property should have a given minimum cardinality  */
    public static final Property minCardinality               = m_vocabModel.createProperty( OWL.NAMESPACE, "minCardinality"             );
    
    /** Property denoting the OWL predicate that states that a property should have a given maximum cardinality  */
    public static final Property maxCardinality               = m_vocabModel.createProperty( OWL.NAMESPACE, "maxCardinality"             );
    
    /** Property denoting the OWL predicate that states that a property should have a given cardinality  */
    public static final Property cardinality                  = m_vocabModel.createProperty( OWL.NAMESPACE, "cardinality"                );
    
    /** Property denoting the OWL predicate that states that one property is the inverse of another  */
    public static final Property inverseOf                    = m_vocabModel.createProperty( OWL.NAMESPACE, "inverseOf"                  );
    
    /** Property denoting the OWL predicate that states that a given ontology imports another  */
    public static final Property imports                      = m_vocabModel.createProperty( OWL.NAMESPACE, "imports"                    );
    
    /** Property denoting the OWL predicate that states the version metadata for a given ontology  */
    public static final Property versionInfo                  = m_vocabModel.createProperty( OWL.NAMESPACE, "versionInfo"                );
    
    /** Property denoting the OWL predicate that states that a given ontology is a prior version of the ontology given by the domain  */
    public static final Property priorVersion                 = m_vocabModel.createProperty( OWL.NAMESPACE, "priorVersion"               );
    
    /** Property denoting the OWL predicate that states that a given ontology is a backwardly compatible prior version of the ontology given by the domain  */
    public static final Property backwardCompatibleWith      = m_vocabModel.createProperty( OWL.NAMESPACE, "backwardCompatibleWith"    );
    
    /** Property denoting the OWL predicate that states that a given ontology is a backwardly incompatible version of the ontology given by the domain  */
    public static final Property incompatibleWith             = m_vocabModel.createProperty( OWL.NAMESPACE, "incompatibleWith"           );

    
    // the following aren't strictly relevant, since they are just the RDF/RDFS vocab elements
    // however they are referenced by the OWL spec, so we include them for completeness

    /** Resource denoting the class of datatype values @see RDFS#Datatype */
    public static final Resource Datatype        = RDFS.Datatype;
    
    /** Resource denoting the class of list values @see RDF#List */
    public static final Resource List            = RDF.List;
    
    /** Resource denoting the class of literal values @see RDFS#Literal */
    public static final Resource Literal         = RDFS.Literal;

    /** Resource denoting the nil (empty) list @see RDF#nil */
    public static final Resource nil             = RDF.nil;

    /** Property denoting the type of a value @see RDF#type */
    public static final Property type            = RDF.type;
    
    /** Property denoting the comment on a value @see RDFS#comment */
    public static final Property comment         = RDFS.comment;
    
    /** Property denoting the domain of a predicate @see RDFS#domain */
    public static final Property domain          = RDFS.domain;
    
    /** Property denoting the label on a value @see RDFS#label */
    public static final Property label           = RDFS.label;
    
    /** Property denoting the range of a predicate @see RDFS#range */
    public static final Property range           = RDFS.range;
    
    /** Property denoting the super-class of a class @see RDFS#subClassOf */
    public static final Property subClassOf      = RDFS.subClassOf;
    
    /** Property denoting the super-property of a property @see RDFS#subPropertyOf */
    public static final Property subPropertyOf   = RDFS.subPropertyOf;
    

    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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
