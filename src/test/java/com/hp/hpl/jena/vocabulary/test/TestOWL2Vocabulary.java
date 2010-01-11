/*
    (c) Copyright 2010 Epimorphics Limited
    [see end of file]
    $Id: TestOWL2Vocabulary.java,v 1.1 2010-01-11 09:17:06 chris-dollin Exp $
*/

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;

/**
    Tests for spellings of OWL2 terms and their relationship with OWL[1]
    terms.
    
    @author chris.dollin
*/
public class TestOWL2Vocabulary extends VocabTestBase
    {
    public TestOWL2Vocabulary( String name )
        { super( name ); }
    
    public void testOWL2SharesOWL1URI()
        {
        assertEquals( OWL.getURI(), OWL2.getURI() );
        }
    
    public void testOWL2TermSpellings()
        {
        assertResource( OWL2.AllDifferent, "AllDifferent" );
        assertResource( OWL2.AllDisjointClasses, "AllDisjointClasses" );
        assertResource( OWL2.AllDisjointProperties, "AllDisjointProperties" );
        assertResource( OWL2.Annotation, "Annotation" );
        assertResource( OWL2.AnnotationProperty, "AnnotationProperty" );
        assertResource( OWL2.AsymmetricProperty, "AsymmetricProperty" );
        assertResource( OWL2.Axiom, "Axiom" );
        assertResource( OWL2.Class, "Class" );
        assertResource( OWL2.DataRange, "DataRange" );
        assertResource( OWL2.DatatypeProperty, "DatatypeProperty" );
        assertResource( OWL2.DeprecatedClass, "DeprecatedClass" );
        assertResource( OWL2.DeprecatedProperty, "DeprecatedProperty" );
        assertResource( OWL2.FunctionalProperty, "FunctionalProperty" );
        assertResource( OWL2.InverseFunctionalProperty, "InverseFunctionalProperty" );
        assertResource( OWL2.IrreflexiveProperty, "IrreflexiveProperty" );
        assertResource( OWL2.NamedIndividual, "NamedIndividual" );
        assertResource( OWL2.NegativePropertyAssertion, "NegativePropertyAssertion" );
        assertResource( OWL2.Nothing, "Nothing" );
        assertResource( OWL2.ObjectProperty, "ObjectProperty" );
        assertResource( OWL2.Ontology, "Ontology" );
        assertResource( OWL2.OntologyProperty, "OntologyProperty" );
        assertResource( OWL2.ReflexiveProperty, "ReflexiveProperty" );
        assertResource( OWL2.Restriction, "Restriction" );
        assertResource( OWL2.SymmetricProperty, "SymmetricProperty" );
        assertResource( OWL2.Thing, "Thing" );
        assertResource( OWL2.TransitiveProperty, "TransitiveProperty" );
        assertProperty( OWL2.allValuesFrom, "allValuesFrom" );
        assertProperty( OWL2.annotatedProperty, "annotatedProperty" );
        assertProperty( OWL2.annotatedSource, "annotatedSource" );
        assertProperty( OWL2.annotatedTarget, "annotatedTarget" );
        assertProperty( OWL2.assertionProperty, "assertionProperty" );
        assertProperty( OWL2.backwardCompatibleWith, "backwardCompatibleWith" );
        assertProperty( OWL2.bottomDataProperty, "bottomDataProperty" );
        assertProperty( OWL2.bottomObjectProperty, "bottomObjectProperty" );
        assertProperty( OWL2.cardinality, "cardinality" );
        assertProperty( OWL2.complementOf, "complementOf" );
        assertProperty( OWL2.datatypeComplementOf, "datatypeComplementOf" );
        assertProperty( OWL2.deprecated, "deprecated" );
        assertProperty( OWL2.differentFrom, "differentFrom" );
        assertProperty( OWL2.disjointUnionOf, "disjointUnionOf" );
        assertProperty( OWL2.disjointWith, "disjointWith" );
        assertProperty( OWL2.distinctMembers, "distinctMembers" );
        assertProperty( OWL2.equivalentClass, "equivalentClass" );
        assertProperty( OWL2.equivalentProperty, "equivalentProperty" );
        assertProperty( OWL2.hasKey, "hasKey" );
        assertProperty( OWL2.hasSelf, "hasSelf" );
        assertProperty( OWL2.hasValue, "hasValue" );
        assertProperty( OWL2.imports, "imports" );
        assertProperty( OWL2.incompatibleWith, "incompatibleWith" );
        assertProperty( OWL2.intersectionOf, "intersectionOf" );
        assertProperty( OWL2.inverseOf, "inverseOf" );
        assertProperty( OWL2.maxCardinality, "maxCardinality" );
        assertProperty( OWL2.maxQualifiedCardinality, "maxQualifiedCardinality" );
        assertProperty( OWL2.members, "members" );
        assertProperty( OWL2.minCardinality, "minCardinality" );
        assertProperty( OWL2.minQualifiedCardinality, "minQualifiedCardinality" );
        assertProperty( OWL2.onClass, "onClass" );
        assertProperty( OWL2.onDataRange, "onDataRange" );
        assertProperty( OWL2.onDatatype, "onDatatype" );
        assertProperty( OWL2.onProperties, "onProperties" );
        assertProperty( OWL2.onProperty, "onProperty" );
        assertProperty( OWL2.oneOf, "oneOf" );
        assertProperty( OWL2.priorVersion, "priorVersion" );
        assertProperty( OWL2.propertyChainAxiom, "propertyChainAxiom" );
        assertProperty( OWL2.propertyDisjointWith, "propertyDisjointWith" );
        assertProperty( OWL2.qualifiedCardinality, "qualifiedCardinality" );
        assertProperty( OWL2.sameAs, "sameAs" );
        assertProperty( OWL2.someValuesFrom, "someValuesFrom" );
        assertProperty( OWL2.sourceIndividual, "sourceIndividual" );
        assertProperty( OWL2.targetIndividual, "targetIndividual" );
        assertProperty( OWL2.targetValue, "targetValue" );
        assertProperty( OWL2.topDataProperty, "topDataProperty" );
        assertProperty( OWL2.topObjectProperty, "topObjectProperty" );
        assertProperty( OWL2.unionOf, "unionOf" );
        assertProperty( OWL2.versionIRI, "versionIRI" );
        assertProperty( OWL2.versionInfo, "versionInfo" );
        assertProperty( OWL2.withRestrictions, "withRestrictions" );
        }
    
    public void testSharedOWLTerms()
        {
        assertEquals( OWL2.AllDifferent, OWL.AllDifferent );
        assertEquals( OWL2.AnnotationProperty, OWL.AnnotationProperty );
        assertEquals( OWL2.Class, OWL.Class );
        assertEquals( OWL2.DataRange, OWL.DataRange );
        assertEquals( OWL2.DatatypeProperty, OWL.DatatypeProperty );
        assertEquals( OWL2.DeprecatedClass, OWL.DeprecatedClass );
        assertEquals( OWL2.DeprecatedProperty, OWL.DeprecatedProperty );
        assertEquals( OWL2.FunctionalProperty, OWL.FunctionalProperty );
        assertEquals( OWL2.InverseFunctionalProperty, OWL.InverseFunctionalProperty );
        assertEquals( OWL2.Nothing, OWL.Nothing );
        assertEquals( OWL2.ObjectProperty, OWL.ObjectProperty );
        assertEquals( OWL2.Ontology, OWL.Ontology );
        assertEquals( OWL2.OntologyProperty, OWL.OntologyProperty );
        assertEquals( OWL2.Restriction, OWL.Restriction );
        assertEquals( OWL2.SymmetricProperty, OWL.SymmetricProperty );
        assertEquals( OWL2.Thing, OWL.Thing );
        assertEquals( OWL2.TransitiveProperty, OWL.TransitiveProperty );
        assertEquals( OWL2.allValuesFrom, OWL.allValuesFrom );
        assertEquals( OWL2.backwardCompatibleWith, OWL.backwardCompatibleWith );
        assertEquals( OWL2.cardinality, OWL.cardinality );
        assertEquals( OWL2.complementOf, OWL.complementOf );
        assertEquals( OWL2.differentFrom, OWL.differentFrom );
        assertEquals( OWL2.disjointWith, OWL.disjointWith );
        assertEquals( OWL2.distinctMembers, OWL.distinctMembers );
        assertEquals( OWL2.equivalentClass, OWL.equivalentClass );
        assertEquals( OWL2.equivalentProperty, OWL.equivalentProperty );
        assertEquals( OWL2.hasValue, OWL.hasValue );
        assertEquals( OWL2.imports, OWL.imports );
        assertEquals( OWL2.incompatibleWith, OWL.incompatibleWith );
        assertEquals( OWL2.intersectionOf, OWL.intersectionOf );
        assertEquals( OWL2.inverseOf, OWL.inverseOf );
        assertEquals( OWL2.maxCardinality, OWL.maxCardinality );
        assertEquals( OWL2.minCardinality, OWL.minCardinality );
        assertEquals( OWL2.onProperty, OWL.onProperty );
        assertEquals( OWL2.oneOf, OWL.oneOf );
        assertEquals( OWL2.priorVersion, OWL.priorVersion );
        assertEquals( OWL2.sameAs, OWL.sameAs );
        assertEquals( OWL2.someValuesFrom, OWL.someValuesFrom );
        assertEquals( OWL2.unionOf, OWL.unionOf );
        assertEquals( OWL2.versionInfo, OWL.versionInfo );
        }

    private void assertProperty( Property p, String local )
        { assertProperty( OWL2.getURI() + local, p ); }

    private void assertResource( Resource r, String local )
        { assertResource( OWL2.getURI() + local, r ); }
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
