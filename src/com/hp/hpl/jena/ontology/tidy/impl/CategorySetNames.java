/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.impl;

import java.util.Arrays;
import com.hp.hpl.jena.shared.BrokenException;

/**
 * Provides static method {@link #symmetricNames}to give a string
 * representation of a category set, contrasted with a disjoint set.
 * 
 * @author Jeremy J. Carroll
 *  
 */
public class CategorySetNames {
    
    static public String getName(int i){
        return (String)names[i][1];
    }
    static int userIDorBlankNode[] =
        new int[1+CategorySet.getSet(Grammar.userID).length
                + CategorySet.getSet(Grammar.blank).length];
    static {
        userIDorBlankNode[0] = Grammar.dataAnnotationPropID;
        System.arraycopy(
                CategorySet.getSet(Grammar.userID),
                0,
                userIDorBlankNode,
                1,
                CategorySet.getSet(Grammar.userID).length);
        System.arraycopy(
                CategorySet.getSet(Grammar.blank),
                0,
                userIDorBlankNode,
                1+CategorySet.getSet(Grammar.userID).length,
                CategorySet.getSet(Grammar.blank).length);
       Arrays.sort(userIDorBlankNode);

        
    }
    // TODO update comment
	/**
	 * The order of this array encodes a declarative preference: - the most
	 * precise description that fits is preferred - the shortest describing
	 * string is preferred (some strings are short but awkward, in these cases a
	 * second string is given, whose length is also counted)
	 * 
	 * Exceptions to this declarative preference are: prefer { objectPropID,
	 * transPropID } over { objectPropID }
	 * 
	 * and prefer { objectPropID, transPropID } over { transPropID }
	 * 
	 * To verify the behaviour set DEBUG_NAMES to true and run main() (fairly
	 * slow: ten, fifteen minutes)
	 *  
	 */
	static Object names[][] = {
			// use "a blank node" where more precision is not needed.
			{ CategorySet.getSet(Grammar.blank), "a blank node", "ok" },
			{ new int[] { Grammar.owlAllDifferent }, "owl:AllDifferent" },
			{ new int[] { Grammar.owlAnnotationProperty },
					"owl:AnnotationProperty" },
			{ new int[] { Grammar.owlClass }, "owl:Class" },
			{ new int[] { Grammar.owlDataRange }, "owl:DataRange" },
			{ new int[] { Grammar.owlDatatypeProperty }, "owl:DatatypeProperty" },
			{ new int[] { Grammar.owlDeprecatedClass }, "owl:DeprecatedClass" },
			{ new int[] { Grammar.owlDeprecatedProperty },
					"owl:DeprecatedProperty" },
			{ new int[] { Grammar.owlFunctionalProperty },
					"owl:FunctionalProperty" },
			{ new int[] { Grammar.owlInverseFunctionalProperty },
					"owl:InverseFunctionalProperty" },
			{ new int[] { Grammar.owlObjectProperty }, "owl:ObjectProperty" },
			{ new int[] { Grammar.owlOntology }, "owl:Ontology" },
			{ new int[] { Grammar.owlOntologyProperty }, "owl:OntologyProperty" },
			{ new int[] { Grammar.owlRestriction }, "owl:Restriction" },
			{ new int[] { Grammar.owlSymmetricProperty },
					"owl:SymmetricProperty" },
			{ new int[] { Grammar.owlTransitiveProperty },
					"owl:TransitiveProperty" },
			{ new int[] { Grammar.owlcomplementOf }, "owl:complementOf" },
			{ new int[] { Grammar.owldifferentFrom }, "owl:differentFrom" },
			{ new int[] { Grammar.owldisjointWith }, "owl:disjointWith" },
			{ new int[] { Grammar.owldistinctMembers }, "owl:distinctMembers" },
			{ new int[] { Grammar.owlequivalentClass }, "owl:equivalentClass" },
			{ new int[] { Grammar.owlequivalentProperty },
					"owl:equivalentProperty" },
			{ new int[] { Grammar.owlhasValue }, "owl:hasValue" },
			{ new int[] { Grammar.owlintersectionOf }, "owl:intersectionOf" },
			{ new int[] { Grammar.owlinverseOf }, "owl:inverseOf" },
			{ new int[] { Grammar.owlmaxCardinality }, "owl:maxCardinality" },
			{ new int[] { Grammar.owlonProperty }, "owl:onProperty" },
			{ new int[] { Grammar.owloneOf }, "owl:oneOf" },
			{ new int[] { Grammar.owlsameAs }, "owl:sameAs" },
			{ new int[] { Grammar.owlsomeValuesFrom },
					"owl:someValuesFrom or owl:allValuesFrom" },
			{ new int[] { Grammar.owlunionOf }, "owl:unionOf" },
			{ new int[] { Grammar.rdfList }, "rdf:List" },
			{ new int[] { Grammar.rdfProperty }, "rdf:Property" },
			{ new int[] { Grammar.rdffirst }, "rdf:first" },
			{ new int[] { Grammar.rdfnil }, "rdf:nil" },
			{ new int[] { Grammar.rdfrest }, "rdf:rest" },
			{ new int[] { Grammar.rdftype }, "rdf:type" },
			{ new int[] { Grammar.rdfsClass }, "rdfs:Class" },
			{ new int[] { Grammar.rdfsDatatype }, "rdfs:Datatype" },
			{ new int[] { Grammar.rdfsdomain }, "rdfs:domain" },
			{ new int[] { Grammar.rdfsrange }, "rdfs:range" },
			{ new int[] { Grammar.rdfssubClassOf }, "rdfs:subClassOf" },
			{ new int[] { Grammar.rdfssubPropertyOf }, "rdfs:subPropertyOf" },
			{ new int[] { Grammar.dataRangeID }, "rdfs:Literal" },
			{ new int[] { Grammar.unnamedDataRange }, "a datarange" },

			{ new int[] { Grammar.unnamedIndividual, Grammar.individualID },
					"an individual" },
			{ new int[] { Grammar.unnamedOntology, Grammar.ontologyID },
					"an ontology" },
			//			{ new int[] { Grammar.unnamedIndividual }, "an unnamed
			// individual" },
			//			{ new int[] { Grammar.unnamedOntology }, "an unnamed ontology" },
			{ new int[] { Grammar.unnamedIndividual, Grammar.unnamedOntology },
					"an unnamed individual or unnamed ontology" },
			{ new int[] { Grammar.allDifferent },
					"a blank node in an owl:AllDifferent construction" },
			{ new int[] { Grammar.annotationPropID }, "an annotation property" },
			{ new int[] { Grammar.classID }, "a named class" },
			{ new int[] { Grammar.dataPropID }, "a datatype property" },
			{ new int[] { Grammar.datatypeID }, "a datatype" },
			{ new int[] { Grammar.individualID }, "a named individual" },
			{ new int[] { Grammar.objectPropID, Grammar.transitivePropID },
					"an object property" },
			{ new int[] { Grammar.transitivePropID },
					"a transitive object property" },
			{ new int[] { Grammar.objectPropID },
					"a non-transitive object property" },

			{ new int[] { Grammar.ontologyID }, "a named ontology" },
			{ new int[] { Grammar.ontologyPropertyID }, "an ontology property" },

			{ new int[] { Grammar.classID, Grammar.individualID },
					"a named class or a named individual" },

			{ new int[] { Grammar.classID, Grammar.datatypeID },
					"a named class or a datatype identifier" },
			{
					new int[] { Grammar.description5disjointWith,
							Grammar.description5equivalentClass,
							Grammar.description5object,
							Grammar.description5subClassOf },
					"a class description" },

			{
					new int[] { Grammar.listOfDataLiteral,
							Grammar.listOfDescription,
							Grammar.listOfIndividualID }, "a non-empty list" },
			{
					new int[] { Grammar.rdfnil, Grammar.listOfDataLiteral,
							Grammar.listOfDescription,
							Grammar.listOfIndividualID },
					"a possibly empty list" },

			{
					new int[] { Grammar.restriction6disjointWith,
							Grammar.restriction6equivalentClass,
							Grammar.restriction6object,
							Grammar.restriction6subClassOf,
							Grammar.restriction7disjointWith,
							Grammar.restriction7equivalentClass,
							Grammar.restriction7object,
							Grammar.restriction7subClassOf,
							Grammar.restriction8disjointWith,
							Grammar.restriction8equivalentClass,
							Grammar.restriction8object,
							Grammar.restriction8subClassOf },
					"a property restriction" },
			{
					new int[] { Grammar.description5disjointWith,
							Grammar.description5equivalentClass,
							Grammar.description5object,
							Grammar.description5subClassOf,
							Grammar.unnamedDataRange },
					"a class description or a datarange" },
			{
					new int[] { Grammar.description5disjointWith,
							Grammar.description5equivalentClass,
							Grammar.description5object,
							Grammar.description5subClassOf,
							Grammar.restriction6disjointWith,
							Grammar.restriction6equivalentClass,
							Grammar.restriction6object,
							Grammar.restriction6subClassOf,
							Grammar.restriction7disjointWith,
							Grammar.restriction7equivalentClass,
							Grammar.restriction7object,
							Grammar.restriction7subClassOf,
							Grammar.restriction8disjointWith,
							Grammar.restriction8equivalentClass,
							Grammar.restriction8object,
							Grammar.restriction8subClassOf },
					"a class description or a property restriction" },
			{
					new int[] { Grammar.classID,
							Grammar.description5disjointWith,
							Grammar.description5equivalentClass,
							Grammar.description5object,
							Grammar.description5subClassOf,
							Grammar.restriction6disjointWith,
							Grammar.restriction6equivalentClass,
							Grammar.restriction6object,
							Grammar.restriction6subClassOf,
							Grammar.restriction7disjointWith,
							Grammar.restriction7equivalentClass,
							Grammar.restriction7object,
							Grammar.restriction7subClassOf,
							Grammar.restriction8disjointWith,
							Grammar.restriction8equivalentClass,
							Grammar.restriction8object,
							Grammar.restriction8subClassOf },
					"a class expression", "fairly complex notion -----------" },
			{
					new int[] { Grammar.description5disjointWith,
							Grammar.description5equivalentClass,
							Grammar.description5object,
							Grammar.description5subClassOf,
							Grammar.restriction6disjointWith,
							Grammar.restriction6equivalentClass,
							Grammar.restriction6object,
							Grammar.restriction6subClassOf,
							Grammar.restriction7disjointWith,
							Grammar.restriction7equivalentClass,
							Grammar.restriction7object,
							Grammar.restriction7subClassOf,
							Grammar.restriction8disjointWith,
							Grammar.restriction8equivalentClass,
							Grammar.restriction8object,
							Grammar.restriction8subClassOf,
							Grammar.unnamedDataRange },
					"a class description or a property restriction or a datarange" },

			{
					new int[] { Grammar.restriction6disjointWith,
							Grammar.restriction6equivalentClass,
							Grammar.restriction6object,
							Grammar.restriction6subClassOf, },
					"a restriction on a datatype property" },
			{
					new int[] { Grammar.restriction7disjointWith,
							Grammar.restriction7equivalentClass,
							Grammar.restriction7object,
							Grammar.restriction7subClassOf,
							Grammar.restriction8disjointWith,
							Grammar.restriction8equivalentClass,
							Grammar.restriction8object,
							Grammar.restriction8subClassOf },
					"a restriction on an object property" },

			{
					new int[] { Grammar.restriction7disjointWith,
							Grammar.restriction7equivalentClass,
							Grammar.restriction7object,
							Grammar.restriction7subClassOf, },
					"a restriction on a non-transitive object property" },

			{
					new int[] { Grammar.restriction6disjointWith,
							Grammar.restriction6equivalentClass,
							Grammar.restriction6object,
							Grammar.restriction6subClassOf,
							Grammar.restriction7disjointWith,
							Grammar.restriction7equivalentClass,
							Grammar.restriction7object,
							Grammar.restriction7subClassOf, },
					"a restriction on a datatype property or on a non-transitive object property" },
			{
					new int[] { Grammar.restriction8disjointWith,
							Grammar.restriction8equivalentClass,
							Grammar.restriction8object,
							Grammar.restriction8subClassOf },
					"a restriction on a transitive object property" },

			{
					new int[] { Grammar.description5object },
					"a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith", },
			{
					new int[] { Grammar.unnamedDataRange,
							Grammar.description5object },
					"a datarange or a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith", },
			{
					new int[] { Grammar.restriction6object,
							Grammar.restriction7object,
							Grammar.restriction8object, },
					"a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith", },
			{
					new int[] { Grammar.description5object,
							Grammar.restriction6object,
							Grammar.restriction7object,
							Grammar.restriction8object, },
					"a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith", },
			{
					new int[] { Grammar.description5object,
							Grammar.restriction6object,
							Grammar.restriction7object,
							Grammar.restriction8object,
							Grammar.unnamedDataRange, },
					"a datarange or a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith", },
			{
					new int[] { Grammar.description5disjointWith },
					"a class description participating in an owl:disjointWith construct", },
			{
					new int[] { Grammar.restriction6disjointWith,
							Grammar.restriction7disjointWith,
							Grammar.restriction8disjointWith, },
					"a restriction participating in an owl:disjointWith construct", },
			{
					new int[] { Grammar.description5disjointWith,
							Grammar.restriction6disjointWith,
							Grammar.restriction7disjointWith,
							Grammar.restriction8disjointWith, },
					"a description or a restriction participating in an owl:disjointWith construct", },
			{
					new int[] { Grammar.description5equivalentClass },
					"a class description participating in an owl:equivalentClass construct", },
			{
					new int[] { Grammar.restriction6equivalentClass,
							Grammar.restriction7equivalentClass,
							Grammar.restriction8equivalentClass, },
					"a restriction participating in an owl:equivalentClass construct", },
			{
					new int[] { Grammar.description5equivalentClass,
							Grammar.restriction6equivalentClass,
							Grammar.restriction7equivalentClass,
							Grammar.restriction8equivalentClass, },
					"a description or a restriction participating in an owl:equivalentClass construct", },
			{
					new int[] { Grammar.description5subClassOf },
					"a class description participating in an rdfs:subClassOf construct", },
			{
					new int[] { Grammar.restriction6subClassOf,
							Grammar.restriction7subClassOf,
							Grammar.restriction8subClassOf, },
					"a restriction participating in an rdfs:subClassOf construct", },
			{
					new int[] { Grammar.description5subClassOf,
							Grammar.restriction6subClassOf,
							Grammar.restriction7subClassOf,
							Grammar.restriction8subClassOf, },
					"a description or a restriction participating in an rdfs:subClassOf construct", },
			{
					new int[] { Grammar.description5disjointWith,
							Grammar.description5equivalentClass,
							Grammar.description5object,
							Grammar.description5subClassOf,
							Grammar.restriction6disjointWith,
							Grammar.restriction6equivalentClass,
							Grammar.restriction6object,
							Grammar.restriction6subClassOf,
							Grammar.restriction7disjointWith,
							Grammar.restriction7equivalentClass,
							Grammar.restriction7object,
							Grammar.restriction7subClassOf,
							Grammar.restriction8disjointWith,
							Grammar.restriction8equivalentClass,
							Grammar.restriction8object,
							Grammar.restriction8subClassOf,
							Grammar.unnamedDataRange, Grammar.classID,
							Grammar.dataRangeID, Grammar.datatypeID },
					"a generalized class or datatype expression",
					"complex notion, quite" },

			{ new int[] { Grammar.listOfDescription },
					"a list of class expressions", },
			{ new int[] { Grammar.listOfIndividualID },
					"a list of named individuals" },
			{ new int[] { Grammar.listOfDataLiteral }, "a list of literals" },
			{
					new int[] { Grammar.listOfDataLiteral,
							Grammar.listOfIndividualID },
					"a list of literals or a list of named individuals" },

			{
					new int[] { Grammar.transitivePropID, Grammar.objectPropID,
							Grammar.dataPropID },
					"an object or datatype property" },
			{
					new int[] { Grammar.annotationPropID, Grammar.objectPropID,
							Grammar.transitivePropID },
					"an annotation property or an object property" },
			{
					new int[] { Grammar.annotationPropID,
							Grammar.dataAnnotationPropID,
							Grammar.ontologyPropertyID },
					"an annotation or ontology property" },

			{ new int[] { Grammar.annotationPropID, Grammar.dataPropID },
					"a datatype property or an annotation property" },
			{
					new int[] { Grammar.annotationPropID, Grammar.dataPropID,
							Grammar.objectPropID,

							Grammar.ontologyPropertyID,
							Grammar.transitivePropID },
					"a property of some sort", "fairly horrid ---------" },

			{
					new int[] { Grammar.annotationPropID, Grammar.objectPropID,
							Grammar.ontologyPropertyID,
							Grammar.transitivePropID },
					"an annotation property, an object property or an ontology property" },

			{
					new int[] { Grammar.annotationPropID, Grammar.objectPropID,
							Grammar.dataPropID, Grammar.transitivePropID },
					"an annotation property, a datatype property or an object property" },
			{ new int[] { Grammar.dataPropID, Grammar.objectPropID },
					"a datatype property or a non-transitive object property" },
			{
					new int[] { Grammar.listOfDescription,
							Grammar.listOfIndividualID },
					"a list of class expressions or a list of named individuals" },
			{ CategorySet.getSet(Grammar.userID), "a user ID",
					"much too general, often, except when needed" },
            { userIDorBlankNode, "a user ID or blank node",
					"much too general, often, except when needed" },

			{
					new int[] { Grammar.dlInteger, Grammar.liteInteger,
							Grammar.literal, Grammar.userTypedLiteral },
					"a literal", },

			{ new int[] { Grammar.dlInteger, Grammar.liteInteger, },
					"a non-negative integer", },

			{ new int[] { Grammar.literal, Grammar.userTypedLiteral },
					"a literal other than a non-negative integer", },
			{
					new int[] { Grammar.individualID, Grammar.dlInteger,
							Grammar.liteInteger, Grammar.literal,
							Grammar.userTypedLiteral },
					"a literal or a named individual", },
			{
					new int[] { Grammar.classID,
							Grammar.description5disjointWith,
							Grammar.description5equivalentClass,
							Grammar.description5object,
							Grammar.description5subClassOf,
							Grammar.unnamedDataRange },
					// TODO check error message with this one
					"a named class or a class description or a datarange",
					"prefer not to use" },
			{
					new int[] { Grammar.classID, Grammar.dlInteger,
							Grammar.individualID, Grammar.liteInteger,
							Grammar.literal, Grammar.userTypedLiteral,
							Grammar.description5object,
							Grammar.restriction6object,
							Grammar.restriction7object,
							Grammar.restriction8object },
					"a possible member of a list (a class expression, a literal, or a named individual)",
					"really do not want to use this unless needed -------------------------------------" },

			{
					new int[] { Grammar.classID, Grammar.description5object,
							Grammar.restriction6object,
							Grammar.restriction7object,
							Grammar.restriction8object,
							Grammar.owlAllDifferent,
							Grammar.owlAnnotationProperty, Grammar.owlClass,
							Grammar.owlDataRange, Grammar.owlDatatypeProperty,
							Grammar.owlDeprecatedClass,
							Grammar.owlDeprecatedProperty,
							Grammar.owlFunctionalProperty,
							Grammar.owlInverseFunctionalProperty,
							Grammar.owlObjectProperty, Grammar.owlOntology,
							Grammar.owlOntologyProperty,
							Grammar.owlRestriction,
							Grammar.owlSymmetricProperty,
							Grammar.owlTransitiveProperty, Grammar.rdfList,
							Grammar.rdfProperty, Grammar.rdfsClass,
							Grammar.rdfsDatatype },
					// TODO check error messages with this one in
					"a legal object of rdf:type",
					"what a horrid description ------------------------------------------------------------------------------- horrid horrid" },

	};
    static {
        for (int i = 0; i < names.length; i++) {
            Arrays.sort((int[]) names[i][0]);
        }
    }

    private static final int NOT_CLASSIFIED = -1;

    static boolean DEBUG_NAMES = false;

    static private int empty[] = {};

    static private int nameCatSet(int in[], int out[]) {
        int rslt = NOT_CLASSIFIED;
        if (out == null)
            out = empty;

        if (in.length==0)
            throw new BrokenException("Logic error");
        for (int i = 0; i < names.length; i++) {
            int cats[] = (int[]) names[i][0];
            if (Q.subset(in, cats) && !Q.intersect(cats, out)) {
                if (rslt == NOT_CLASSIFIED)
                    rslt = i;
                if (!DEBUG_NAMES)
                    return rslt;
                DebugCategorySetNames.debugCatNames(rslt, i);
            }
        }
        if (rslt != NOT_CLASSIFIED)
            return rslt;
        throw new BrokenException("Logic error: unnamed: in: "+CategorySet.catString(in)+" out: "+CategorySet.catString(out));

    }

    static void symmetricNames(WantedGiven w,int wantedThisTriple[], int givenOtherTriples[]) {
        w.setWanted(nameCatSet(wantedThisTriple, givenOtherTriples));
        w.setGiven(nameCatSet(givenOtherTriples, wantedThisTriple));
    }

}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

