/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: OWLProfile.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-27 16:28:46 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.*;



/**
 * <p>
 * Ontology language profile implementation for the OWL 2002/07 language.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OWLProfile.java,v 1.2 2003-03-27 16:28:46 ian_dickinson Exp $
 */
public class OWLProfile
    implements Profile
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    public String   NAMESPACE() {                   return OWL.NAMESPACE; }

    public Resource CLASS() {                       return OWL.Class; }
    public Resource RESTRICTION() {                 return OWL.Restriction; }
    public Resource THING() {                       return OWL.Thing; }
    public Resource NOTHING() {                     return OWL.Nothing; }
    public Resource OBJECT_PROPERTY() {             return OWL.ObjectProperty; }
    public Resource DATATYPE_PROPERTY() {           return OWL.DatatypeProperty; }
    public Resource TRANSITIVE_PROPERTY() {         return OWL.TransitiveProperty; }
    public Resource SYMMETRIC_PROPERTY() {          return OWL.SymmetricProperty; }
    public Resource FUNCTIONAL_PROPERTY() {         return OWL.FunctionalProperty; }
    public Resource INVERSE_FUNCTIONAL_PROPERTY() { return OWL.InverseFunctionalProperty; }
    public Resource ALL_DIFFERENT() {               return OWL.AllDifferent; }
    public Resource ONTOLOGY() {                    return OWL.Ontology; }
    public Resource DEPRECATED_CLASS() {            return OWL.DeprecatedClass; }
    public Resource DEPRECATED_PROPERTY() {         return OWL.DeprecatedProperty; }

    public Property SAME_PROPERTY_AS() {            return OWL.samePropertyAs; }
    public Property SAME_CLASS_AS() {               return OWL.sameClassAs; }
    public Property DISJOINT_WITH() {               return OWL.disjointWith; }
    public Property SAME_INDIVIDUAL_AS() {          return OWL.sameIndividualAs; }
    public Property SAME_AS() {                     return OWL.sameAs; }
    public Property DIFFERENT_FROM() {              return OWL.differentFrom; }
    public Property DISTINCT_MEMBERS() {            return OWL.distinctMembers; }
    public Property UNION_OF() {                    return OWL.unionOf; }
    public Property INTERSECTION_OF() {             return OWL.intersectionOf; }
    public Property COMPLEMENT_OF() {               return OWL.complementOf; }
    public Property ONE_OF() {                      return OWL.oneOf; }
    public Property ON_PROPERTY() {                 return OWL.onProperty; }
    public Property ALL_VALUES_FROM() {             return OWL.allValuesFrom; }
    public Property HAS_VALUE() {                   return OWL.hasValue; }
    public Property SOME_VALUES_FROM() {            return OWL.someValuesFrom; }
    public Property MIN_CARDINALITY() {             return OWL.minCardinality; }
    public Property MAX_CARDINALITY() {             return OWL.maxCardinality; }
    public Property CARDINALITY() {                 return OWL.cardinality; }
    public Property INVERSE_OF() {                  return OWL.inverseOf; }
    public Property IMPORTS() {                     return OWL.imports; }
    public Property VERSION_INFO() {                return OWL.versionInfo; }
    public Property PRIOR_VERSION() {               return OWL.priorVersion; }
    public Property BACKWARD_COMPATIBLE_WITH() {    return OWL.backwardCompatibleWith; }
    public Property INCOMPATIBLE_WITH() {           return OWL.incompatibleWith; }
    
    

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

