/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: DAML_OILProfile.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-12 17:16:22 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Vocabulary implementation for the OWL 2002/07 language.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DAML_OILProfile.java,v 1.1 2003-03-12 17:16:22 ian_dickinson Exp $
 */
public class DAML_OILProfile
    implements Profile
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** Model to hold the vocabulary resources only */
    private Model m_vocabModel = ModelFactory.createDefaultModel();
    
    // class resources
    private Resource m_class                        = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "Class"                     );
    private Resource m_restriction                  = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "Restriction"               );
    private Resource m_thing                        = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "Thing"                     );
    private Resource m_nothing                      = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "Nothing"                   );
    private Resource m_objectProperty               = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "ObjectProperty"            );
    private Resource m_datatypeProperty             = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "DatatypeProperty"          );
    private Resource m_transitiveProperty           = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "TransitiveProperty"        );
    private Resource m_symmetricProperty            = null;
    private Resource m_functionalProperty           = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "UniqueProperty"            );
    private Resource m_inverseFunctionalProperty    = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "UnambiguousProperty"       );
    private Resource m_allDifferent                 = null;
    private Resource m_ontology                     = m_vocabModel.createResource( DAML_OILProfile._NAMESPACE() + "Ontology"                  );
    private Resource m_deprecatedClass              = null;
    private Resource m_deprecatedProperty           = null;
    
    private Property m_equivalentProperty           = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "samePropertyAs"             );
    private Property m_equivalentClass              = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "equivalentClass"            );
    private Property m_disjointWith                 = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "disjointWith"               );
    private Property m_sameIndividualAs             = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "sameIndividualAs"           );
    private Property m_sameAs                       = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "equivalentTo"               );
    private Property m_differentFrom                = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "differentIndvidualFrom"     );
    private Property m_distinctMembers              = null;
    private Property m_unionOf                      = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "unionOf"                    );
    private Property m_intersectionOf               = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "intersectionOf"             );
    private Property m_complementOf                 = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "complementOf"               );
    private Property m_oneOf                        = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "oneOf"                      );
    private Property m_onProperty                   = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "onProperty"                 );
    private Property m_allValuesFrom                = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "toClass"                    );
    private Property m_hasValue                     = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "hasValue"                   );
    private Property m_someValuesFrom               = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "hasClass"                   );
    private Property m_minCardinality               = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "minCardinality"             );
    private Property m_maxCardinality               = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "maxCardinality"             );
    private Property m_cardinality                  = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "cardinality"                );
    private Property m_inverseOf                    = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "inverseOf"                  );
    private Property m_imports                      = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "imports"                    );
    private Property m_versionInfo                  = m_vocabModel.createProperty( DAML_OILProfile._NAMESPACE(), "versionInfo"                );
    private Property m_priorVersion                 = null;
    private Property m_backwardsCompatibleWith      = null;
    private Property m_incompatibleWith             = null;



    // Constructors
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer the string that is the namespace prefix for this vocabulary
     * </p>
     * 
     * @return The namespace prefix <code>http://www.daml.org/2001/03/daml+oil#</code>
     */
    public static String _NAMESPACE() {             return "http://www.daml.org/2001/03/daml+oil#"; }
    
    
    public String   NAMESPACE() {                   return DAML_OILProfile._NAMESPACE(); }

    public Resource CLASS() {                       return m_class; }
    public Resource RESTRICTION() {                 return m_restriction; }
    public Resource THING() {                       return m_thing; }
    public Resource NOTHING() {                     return m_nothing; }
    public Resource OBJECT_PROPERTY() {             return m_objectProperty; }
    public Resource DATATYPE_PROPERTY() {           return m_datatypeProperty; }
    public Resource TRANSITIVE_PROPERTY() {         return m_transitiveProperty; }
    public Resource SYMMETRIC_PROPERTY() {          return m_symmetricProperty; }
    public Resource FUNCTIONAL_PROPERTY() {         return m_functionalProperty; }
    public Resource INVERSE_FUNCTIONAL_PROPERTY() { return m_inverseFunctionalProperty; }
    public Resource ALL_DIFFERENT() {               return m_allDifferent; }
    public Resource ONTOLOGY() {                    return m_ontology; }
    public Resource DEPRECATED_CLASS() {            return m_deprecatedClass; }
    public Resource DEPRECATED_PROPERTY() {         return m_deprecatedProperty; }

    public Property SAME_PROPERTY_AS() {            return m_equivalentProperty; }
    public Property SAME_CLASS_AS() {               return m_equivalentClass; }
    public Property DISJOINT_WITH() {               return m_disjointWith; }
    public Property SAME_INDIVIDUAL_AS() {          return m_sameIndividualAs; }
    public Property SAME_AS() {                     return m_sameAs; }
    public Property DIFFERENT_FROM() {              return m_differentFrom; }
    public Property DISTINCT_MEMBERS() {            return m_distinctMembers; }
    public Property UNION_OF() {                    return m_unionOf; }
    public Property INTERSECTION_OF() {             return m_intersectionOf; }
    public Property COMPLEMENT_OF() {               return m_complementOf; }
    public Property ONE_OF() {                      return m_oneOf; }
    public Property ON_PROPERTY() {                 return m_onProperty; }
    public Property ALL_VALUES_FROM() {             return m_allValuesFrom; }
    public Property HAS_VALUE() {                   return m_hasValue; }
    public Property SOME_VALUES_FROM() {            return m_someValuesFrom; }
    public Property MIN_CARDINALITY() {             return m_minCardinality; }
    public Property MAX_CARDINALITY() {             return m_maxCardinality; }
    public Property CARDINALITY() {                 return m_cardinality; }
    public Property INVERSE_OF() {                  return m_inverseOf; }
    public Property IMPORTS() {                     return m_imports; }
    public Property VERSION_INFO() {                return m_versionInfo; }
    public Property PRIOR_VERSION() {               return m_priorVersion; }
    public Property BACKWARDS_COMPATIBLE_WITH() {   return m_backwardsCompatibleWith; }
    public Property INCOMPATIBLE_WITH() {           return m_incompatibleWith; }
    
    

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

