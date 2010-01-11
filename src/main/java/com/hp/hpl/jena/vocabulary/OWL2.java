/*
  (c) Copyright 2010 
  [See end of file]
  $Id: OWL2.java,v 1.1 2010-01-11 09:17:04 chris-dollin Exp $
*/

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
    OWL2 vocabulary. NOTE: Jena does not provide OWL2 inference or OntModel
    support. These constants are provided for the convenience of users who
    are doing OWL2 work with the current OWL1 support and desire a suitable
    set of names. 
    @author chris.dollin
*/
public class OWL2
    {
    public final static String NS = "http://www.w3.org/2002/07/owl" + "#";

    public static String getURI() 
        { return NS; }
    
    protected final static Resource resource( String local )
        { return ResourceFactory.createResource( NS + local ); }
    
    protected final static Property property( String local )
        { return ResourceFactory.createProperty( NS + local ); }

    public final static Resource AllDifferent = resource( "AllDifferent" );

    public final static Resource AllDisjointClasses = resource( "AllDisjointClasses" );

    public final static Resource AllDisjointProperties = resource( "AllDisjointProperties" );

    public final static Resource Annotation = resource( "Annotation" );

    public final static Resource AnnotationProperty = resource( "AnnotationProperty" );

    public final static Resource AsymmetricProperty = resource( "AsymmetricProperty" );

    public final static Resource Axiom = resource( "Axiom" );

    public final static Resource Class = resource( "Class" );

    public final static Resource DataRange = resource( "DataRange" );

    public final static Resource DatatypeProperty = resource( "DatatypeProperty" );

    public final static Resource DeprecatedClass = resource( "DeprecatedClass" );

    public final static Resource DeprecatedProperty = resource( "DeprecatedProperty" );

    public final static Resource FunctionalProperty = resource( "FunctionalProperty" );

    public final static Resource InverseFunctionalProperty = resource( "InverseFunctionalProperty" );

    public final static Resource IrreflexiveProperty = resource( "IrreflexiveProperty" );

    public final static Resource NamedIndividual = resource( "NamedIndividual" );

    public final static Resource NegativePropertyAssertion = resource( "NegativePropertyAssertion" );

    public final static Resource Nothing = resource( "Nothing" );

    public final static Resource ObjectProperty = resource( "ObjectProperty" );

    public final static Resource Ontology = resource( "Ontology" );

    public final static Resource OntologyProperty = resource( "OntologyProperty" );

    public final static Resource ReflexiveProperty = resource( "ReflexiveProperty" );

    public final static Resource Restriction = resource( "Restriction" );

    public final static Resource SymmetricProperty = resource( "SymmetricProperty" );

    public final static Resource Thing = resource( "Thing" );

    public final static Resource TransitiveProperty = resource( "TransitiveProperty" );

    public final static Property allValuesFrom = property( "allValuesFrom" );

    public final static Property annotatedProperty = property( "annotatedProperty" );

    public final static Property annotatedSource = property( "annotatedSource" );

    public final static Property annotatedTarget = property( "annotatedTarget" );

    public final static Property assertionProperty = property( "assertionProperty" );

    public final static Property backwardCompatibleWith = property( "backwardCompatibleWith" );

    public final static Property bottomDataProperty = property( "bottomDataProperty" );

    public final static Property bottomObjectProperty = property( "bottomObjectProperty" );

    public final static Property cardinality = property( "cardinality" );

    public final static Property complementOf = property( "complementOf" );

    public final static Property datatypeComplementOf = property( "datatypeComplementOf" );

    public final static Property deprecated = property( "deprecated" );

    public final static Property differentFrom = property( "differentFrom" );

    public final static Property disjointUnionOf = property( "disjointUnionOf" );

    public final static Property disjointWith = property( "disjointWith" );

    public final static Property distinctMembers = property( "distinctMembers" );

    public final static Property equivalentClass = property( "equivalentClass" );

    public final static Property equivalentProperty = property( "equivalentProperty" );

    public final static Property hasKey = property( "hasKey" );

    public final static Property hasSelf = property( "hasSelf" );

    public final static Property hasValue = property( "hasValue" );

    public final static Property imports = property( "imports" );

    public final static Property incompatibleWith = property( "incompatibleWith" );

    public final static Property intersectionOf = property( "intersectionOf" );

    public final static Property inverseOf = property( "inverseOf" );

    public final static Property maxCardinality = property( "maxCardinality" );

    public final static Property maxQualifiedCardinality = property( "maxQualifiedCardinality" );

    public final static Property members = property( "members" );

    public final static Property minCardinality = property( "minCardinality" );

    public final static Property minQualifiedCardinality = property( "minQualifiedCardinality" );

    public final static Property onClass = property( "onClass" );

    public final static Property onDataRange = property( "onDataRange" );

    public final static Property onDatatype = property( "onDatatype" );

    public final static Property onProperties = property( "onProperties" );

    public final static Property onProperty = property( "onProperty" );

    public final static Property oneOf = property( "oneOf" );

    public final static Property priorVersion = property( "priorVersion" );

    public final static Property propertyChainAxiom = property( "propertyChainAxiom" );

    public final static Property propertyDisjointWith = property( "propertyDisjointWith" );

    public final static Property qualifiedCardinality = property( "qualifiedCardinality" );

    public final static Property sameAs = property( "sameAs" );

    public final static Property someValuesFrom = property( "someValuesFrom" );

    public final static Property sourceIndividual = property( "sourceIndividual" );

    public final static Property targetIndividual = property( "targetIndividual" );

    public final static Property targetValue = property( "targetValue" );

    public final static Property topDataProperty = property( "topDataProperty" );

    public final static Property topObjectProperty = property( "topObjectProperty" );

    public final static Property unionOf = property( "unionOf" );

    public final static Property versionIRI = property( "versionIRI" );

    public final static Property versionInfo = property( "versionInfo" );

    public final static Property withRestrictions = property( "withRestrictions" );
    }

/*
    (c) Copyright 2010 Epimorphics Limited
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

