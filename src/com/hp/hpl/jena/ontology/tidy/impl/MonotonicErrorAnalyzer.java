/*
 (c) Copyright 2003-2005 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: MonotonicErrorAnalyzer.java,v 1.8 2005-01-05 14:42:15 chris-dollin Exp $
 */
package com.hp.hpl.jena.ontology.tidy.impl;

import java.util.*;
import java.lang.reflect.*;
import com.hp.hpl.jena.shared.BrokenException;

/**
 * 
 * This class looks at particular triples and tries to work out what went wrong,
 * giving a specific anaylsis.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll </a>
 *  
 */
class MonotonicErrorAnalyzer implements Constants {
	final static private int BLANK_PROP = 1;

	final static private int LITERAL_PROP = 2;

	final static private int LITERAL_SUBJ = 3;

	final static private int BADID_USE = 4;

	final static private int BUILTIN_NON_PRED = 5;

	final static private int BUILTIN_NON_SUBJ = 6;

	final static private int TYPE_NEEDS_ID = 10;

	final static private int TYPE_NEEDS_BLANK = 11;
    
    final static private int TYPE_NEEDS_ID_OR_BLANK = 12;

	final static private int TYPE_OF_CLASS_ONLY_ID = 13;

	final static private int TYPE_OF_PROPERTY_ONLY_ID = 14;

	final static private int TYPE_FOR_BUILTIN = 15;

	final static private int ST_DOMAIN_RANGE = 20;

	final static int BAD_DOMAIN = ST_DOMAIN_RANGE + 1;

	final static int BAD_RANGE = ST_DOMAIN_RANGE + 2;

	final static int BAD_DOMAIN_RANGE = ST_DOMAIN_RANGE + 3;

	final static private int DIFFERENT_CATS = 30;

	final static private int DIFFERENT_CATS_S = DIFFERENT_CATS + 1;

	final static private int DIFFERENT_CATS_P = DIFFERENT_CATS + 2;

	final static private int DIFFERENT_CATS_SP = DIFFERENT_CATS + 3;

	final static private int DIFFERENT_CATS_O = DIFFERENT_CATS + 4;

	final static private int DIFFERENT_CATS_SO = DIFFERENT_CATS + 5;

	final static private int DIFFERENT_CATS_PO = DIFFERENT_CATS + 6;

	final static private int DIFFERENT_CATS_SPO = DIFFERENT_CATS + 7;

	final static private int DC_DOM_RANGE = 40;

	final static private int INCOMPATIBLE_S = DC_DOM_RANGE + 1;

	final static private int INCOMPATIBLE_P = DC_DOM_RANGE + 2;

	final static private int INCOMPATIBLE_SP = DC_DOM_RANGE + 3;

	final static private int INCOMPATIBLE_O = DC_DOM_RANGE + 4;

	final static private int INCOMPATIBLE_SO = DC_DOM_RANGE + 5;

	final static private int INCOMPATIBLE_PO = DC_DOM_RANGE + 6;

	final static private int INCOMPATIBLE_SPO = DC_DOM_RANGE + 7;

	final static private int NOT_CLASSIFIED = -1;

	static private LookupTable look = (LookupTable) LookupTable.get();

	static private final int SZ = CategorySet.unsorted.size();

	static private final boolean isClassOnly[] = new boolean[SZ];

	static private final boolean isPropertyOnly[] = new boolean[SZ];

	static private final boolean isUserID[] = new boolean[SZ];

	static private final boolean isBlank[] = new boolean[SZ];

	static private final boolean isBuiltin[] = new boolean[SZ];

	static private final int start[][] = new int[SZ][];

	static {
		int ix = 0;
		int s[] = new int[4];
		for (int i = 1; i < SZ; i++) {
			if (Grammar.isPseudoCategory(i))
				continue;
			ix = 0;
			if (look.meet(i, Grammar.classOnly) == i) {
				isClassOnly[i] = true;
				s[ix++] = Grammar.classOnly;
			}
			if (look.meet(i, Grammar.propertyOnly) == i) {
				isPropertyOnly[i] = true;
				s[ix++] = Grammar.propertyOnly;
			}
			if (look.meet(i, Grammar.userID) == i) {
				isUserID[i] = true;
				s[ix++] = Grammar.userID;
			}
			if (look.meet(i, Grammar.blank) == i) {
				isBlank[i] = true;
				s[ix++] = Grammar.blank;
			}
			if (ix == 0) {
				s[ix++] = i;
				switch (i) {
				case Grammar.badID:
				case Grammar.dlInteger:
				case Grammar.liteInteger:
				case Grammar.literal:
				case Grammar.userTypedLiteral:
					break;
				default:
					//		System.err.println("Builtin: " + Grammar.catNames[i]);
					isBuiltin[i] = true;
				}
			}
			if (maybeBuiltinID(i)) {
				s[ix++] = i;
			}

			start[i] = new int[ix];
			System.arraycopy(s, 0, start[i], 0, ix);
		}
	}

	static private int miss[] = new int[8];


	static private int getErrorCode(int s, int p, int o, int sx, int px, int ox) {
        // TODO should check args all in range 0 <= it < SZ.
		int key = look.qrefine(sx, px, ox);
		if (key == Failure) {
			return singleTripleError(sx, px, ox);
		}
		int misses = 0;
		int given[] = { s, p, o };
		int general[] = { look.subject(sx, key), look.prop(px, key),
				look.object(ox, key) };
		int meet[] = new int[3];
		for (int i = 0; i < 3; i++) {
			meet[i] = look.meet(general[i], given[i]);
			if (meet[i] == Failure) {
				misses |= (1 << i);
				int r = catMiss(general[i], given[i]);
			}
		}
		miss[misses]++;
		if (misses == 0) {
			return difficultCase(given, general, meet);
		}
		return DIFFERENT_CATS + misses;

	}

	static private int spo(int f, int old, int key) {
		switch (f) {
		case 0:
			return look.subject(old, key);

		case 1:
			return look.prop(old, key);

		case 2:
			return look.object(old, key);
		}
		throw new BrokenException("Illegal argument to spo: " + f);
	}

	static private int diffPreds[] = new int[SZ];

	static private int difficultCase(int given[], int general[], int meet[]) {
		int givenName[] = { NOT_CLASSIFIED, NOT_CLASSIFIED, NOT_CLASSIFIED };
		int wantedName[] = { NOT_CLASSIFIED, NOT_CLASSIFIED, NOT_CLASSIFIED };

		int key[] = new int[3];
		int key2[] = new int[3];
		int cats[][] = new int[3][];
		int failures = 0;
		boolean bad = false;
		for (int i = 0; i < 3; i++) {
			key[i] = look.qrefine(i == 0 ? general[0] : given[0],
					i == 1 ? general[1] : given[1], i == 2 ? general[2]
							: given[2]);
			cats[i] = nonPseudoCats(given[i]);
			key2[i] = look.qrefine(i != 0 ? general[0] : given[0],
					i != 1 ? general[1] : given[1], i != 2 ? general[2]
							: given[2]);
			if (key2[i] == Failure)
				throw new BrokenException("logic error");
		}
		for (int i = 0; i < 3; i++) {
			if (key[i] == Failure)
				failures |= (1 << i);
			else {
				int n = spo(i, general[i], key[i]);
				int nc[] = nonPseudoCats(n);
				if (Q.intersect(nc, cats[i]))
					System.err.println("Intersect!");
				givenName[i] = nameCatSet(cats[i], nc);
				wantedName[i] = nameCatSet(nc,cats[i]);
				if (givenName[i] == NOT_CLASSIFIED
						|| wantedName[i] == NOT_CLASSIFIED)
					bad = true;
			}
		}

		int rslt = DC_DOM_RANGE + (7 ^ failures);
		switch (rslt) {
		case INCOMPATIBLE_SO:
			computeGivenWanted(1,general, key2, cats);
			break;
		case INCOMPATIBLE_PO:
			computeGivenWanted(0,general, key2, cats);
			break;
		case INCOMPATIBLE_SP:
			computeGivenWanted(2,general, key2, cats);
			break;
		case INCOMPATIBLE_P:
			
			computeGivenWanted(0,general, key2, cats);
			computeGivenWanted(2,general, key2, cats);
			
			break;
		default:
			throw new BrokenException(
					"No code for this case - it doesn't happen.");
		}
		return rslt;
	}

	private static int[] computeGivenWanted(int unchanged,int[] general, int[] key2, int[][] cats) {
		int rslt[] = new int[4];
		int ix = 0;
		for (int i=0;i<3;i++)
			if (i!=unchanged) {
				int want = spo(i,general[i],key2[3-i-unchanged]);
				int wantC[] = nonPseudoCats(want);
				rslt[ix++] = nameCatSet(wantC,cats[i]);
				rslt[ix++] = nameCatSet(cats[i],wantC);
			}
		return rslt;
		/*
		int wantSubj = look.subject(general[0],key2[1]);
		int wsc[] = nonPseudoCats(wantSubj);
		int wantObj = look.object(general[2],key2[1]);
		int woc[] = nonPseudoCats(wantObj);
		int wantSPred = look.prop(general[1],key2[0]);
		int wspc[] = nonPseudoCats(wantSPred);
		int wantOPred = look.prop(general[1],key2[2]);
		int wopc[] = nonPseudoCats(wantOPred);
		nameCatSet(wsc,cats[0]);
		nameCatSet(cats[0],wsc);
		nameCatSet(woc,cats[2]);
		nameCatSet(cats[2],woc);
		nameCatSet(wspc,cats[1]);
		nameCatSet(cats[1],wspc);
		nameCatSet(wopc,cats[1]);
		nameCatSet(cats[1],wopc);
		*/
	}

	static private boolean isLiteral(int o) {
		return o == Grammar.literal || o == Grammar.liteInteger
				|| o == Grammar.dlInteger || o == Grammar.userTypedLiteral;
	}

	static private int empty[] = {};

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
	static private Object names[][] = {
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
	};

	static boolean usedName[] = new boolean[names.length];

	static private int descLength(int nm) {
		Object a[] = names[nm];
		return ((String) a[1]).length()
				+ (a.length > 2 ? ((String) a[2]).length() : 0);
	}

	static private int nameInfo[][] = new int[names.length][names.length];

	static private boolean DEBUG_NAMES = false;

	static private String okNames[][] = {
			//		{ "an individual", "an unnamed individual"},
			//		{ "an ontology", "an unnamed ontology"},
			{ "a class description",
					"a class description participating in an owl:disjointWith construct" },
			{ "a class description",
					"a class description participating in an owl:equivalentClass construct" },
			{
					"a class description",
					"a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a class description",
					"a datarange or a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{ "a class description",
					"a class description participating in an rdfs:subClassOf construct" },
			{ "a non-empty list", "a list of literals" },
			{ "a non-empty list",
					"a list of literals or a list of named individuals" },
			{ "a non-empty list", "a list of class expressions" },
			{ "a non-empty list",
					"a list of class expressions or a list of named individuals" },
			{ "a non-empty list", "a list of named individuals" },
			{ "a property restriction", "a restriction on a datatype property" },
			{ "a property restriction",
					"a restriction on a datatype property or on a non-transitive object property" },
			{ "a property restriction",
					"a restriction participating in an owl:disjointWith construct" },
			{ "a property restriction",
					"a description or a restriction participating in an owl:disjointWith construct" },
			{ "a property restriction",
					"a restriction participating in an owl:equivalentClass construct" },
			{
					"a property restriction",
					"a description or a restriction participating in an owl:equivalentClass construct" },
			{
					"a property restriction",
					"a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a property restriction",
					"a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a property restriction",
					"a datarange or a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a property restriction",
					"a possible member of a list (a class expression, a literal, or a named individual)" },
			{ "a property restriction",
					"a restriction participating in an rdfs:subClassOf construct" },
			{ "a property restriction",
					"a description or a restriction participating in an rdfs:subClassOf construct" },
			{ "a property restriction", "a restriction on an object property" },
			{ "a property restriction",
					"a restriction on a non-transitive object property" },
			{ "a property restriction",
					"a restriction on a transitive object property" },
			{ "a class description or a property restriction",
					"a description or a restriction participating in an owl:disjointWith construct" },
			{
					"a class description or a property restriction",
					"a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a class description or a property restriction",
					"a datarange or a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a class description or a property restriction",
					"a possible member of a list (a class expression, a literal, or a named individual)" },
			{
					"a class description or a property restriction or a datarange",
					"a datarange or a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{ "a class description or a property restriction",
					"a description or a restriction participating in an rdfs:subClassOf construct" },
			{
					"a class description or a property restriction",
					"a description or a restriction participating in an owl:equivalentClass construct" },
			{
					"a class description or a datarange",
					"a datarange or a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{ "a property of some sort",
					"an annotation property, an object property or an ontology property" },
			{ "a property of some sort",
					"an annotation property, a datatype property or an object property" },
			{ "an object property", "a non-transitive object property" },
			{ "an ontology", "a named ontology" },
			{ "an object property", "a transitive object property" },
			{ "an object or datatype property",
					"a datatype property or a non-transitive object property" },
			{ "an individual", "a named individual" },
			{ "a literal", "a non-negative integer" },
			{ "a literal", "a literal other than a non-negative integer" },
			{ "a class expression",
					"a named class or a class description or a datarange" },
			{
					"a class expression",
					"a possible member of a list (a class expression, a literal, or a named individual)" },
			{ "a blank node",
					"a blank node in an owl:AllDifferent construction" },
			{ "a blank node", "a property restriction" },
			{ "a blank node", "a class description or a property restriction" },
			{ "a blank node", "a class expression" },
			{ "a blank node",
					"a class description or a property restriction or a datarange" },
			{ "a blank node", "a generalized class or datatype expression" },
			{ "a restriction on an object property",
					"a restriction on a non-transitive object property" },
			{ "a restriction on an object property",
					"a restriction on a transitive object property" },
			{ "a blank node",
					"a restriction on a datatype property or on a non-transitive object property" },
			{ "a generalized class or datatype expression",
					"a named class or a class description or a datarange" },
			{ "a blank node", "a non-empty list" },
			{ "a blank node", "a possibly empty list" },

	};

	static void showNameInfo(boolean shorter, int not, int pref) {
		String prefix = shorter ? "!!" : "//";
		if (!shorter) {
			String p = (String) names[pref][1];
			String o = (String) names[not][1];

			for (int i = 0; i < okNames.length; i++) {
				if (p.equals(okNames[i][0]) && o.equals(okNames[i][1]))
					return;
			}
			System.err.println("{ \"" + names[pref][1] + "\", \""
					+ names[not][1] + "\"},");
		}
		System.err.println(prefix + "Preferred: \"" + names[pref][1] + "\"");
		System.err.println(prefix + "Over: \"" + names[not][1] + "\"");
		System.err.println(prefix + "Array lengths: "
				+ ((int[]) names[pref][0]).length + " vs "
				+ ((int[]) names[not][0]).length + "    Desc length: "
				+ descLength(pref) + " vs " + descLength(not));

	}

	static private int nameCatSet(int in[], int out[]) {
		int rslt = NOT_CLASSIFIED;
		if (out == null)
			out = empty;

		for (int i = 0; i < names.length; i++) {
			int cats[] = (int[]) names[i][0];
			if (Q.subset(in, cats) && !Q.intersect(cats, out)) {
				if (rslt == NOT_CLASSIFIED)
					usedName[i] = true;
				if (!DEBUG_NAMES)
					return i;
				if (rslt == NOT_CLASSIFIED)
					rslt = i;
				else {
					boolean moreSpecific = ((int[]) names[i][0]).length < ((int[]) names[rslt][0]).length;

					if (((int[]) names[i][0]).length == ((int[]) names[rslt][0]).length
							&& Q.subset((int[]) names[i][0],
									(int[]) names[rslt][0])
							&& Q.subset((int[]) names[rslt][0],
									(int[]) names[i][0])) {
						System.err.println("Duplicates!!!!");
						showNameInfo(true, i, rslt);
					}
					boolean shorter = descLength(i) < descLength(rslt);
					int old = nameInfo[i][rslt];
					nameInfo[i][rslt] |= 1 + (moreSpecific ? 2 : 0)
							+ (shorter ? 4 : 0);
					if ((old == 0 || old == 1) && (moreSpecific || shorter))
						showNameInfo(shorter, i, rslt);
				}
			}
		}
		if (rslt != NOT_CLASSIFIED)
			return rslt;

		throw new BrokenException("Logic error.");

	}

	static private int dbgCnt = 0;

	static private boolean debug() {
		return (dbgCnt++ % 50 == 0 && dbgCnt < 2000);
	}

	static private void dumpc(String x, int c, int d) {
		System.out.println(x + "(a)" + CategorySet.catString(c));
		if (d != 0)
			System.out.println(x + "(b)" + CategorySet.catString(d));
	}

	static private int singleTripleError(int sx, int px, int ox) {
		if (isBlank[px])
			return BLANK_PROP;
		if (isLiteral(px))
			return LITERAL_PROP;
		if (isLiteral(sx))
			return LITERAL_SUBJ;

		if (sx == Grammar.badID || px == Grammar.badID || ox == Grammar.badID)
			return BADID_USE;

		// TODO case with bad subj and bad pred
		if (isBuiltin[sx] && !look.canBeSubj(sx))
			return BUILTIN_NON_SUBJ;
        
        // The intent here is that we are only testing for built-in
        // non-predicates,  but a few cases are both 'built-in'
        // and not isBuiltin[], e.g. rdf:Bag, which in much
        // of the code is just a URIref, but can only be a classID
        // and not a propID. The syntactic category given to rdf:Bag
        // initially is { notype, classID } which is not an isBuiltin[] like owlClass.
        
		if (//isBuiltin[px]&&
		!look.canBeProp(px)) {
			return BUILTIN_NON_PRED;
		}

        // TODO is this test spurious? Very likely.
        
		if (px < SZ) {
			int bad = 0;
			int sxx[] = nonPseudoCats(sx);
			int oxx[] = nonPseudoCats(ox);
			if (!Q.intersect(sxx, look.domain(px))) {
				bad += 1;
				int sGivenSubj = nameCatSet(sxx, look.domain(px));
				int sWantedSubj = nameCatSet(look.domain(px), sxx);
			}
			if (!Q.intersect(oxx, look.range(px))) {
				bad += 2;
				int sGivenObj = nameCatSet(oxx, look.range(px));
				int sWantedObj = nameCatSet(look.range(px), oxx);
			}
			if (bad != 0)
				return ST_DOMAIN_RANGE + bad;
		}
		if (px == Grammar.rdftype) {
			return handleRDFType( sx, ox );
		}
		throw new BrokenException("Unreachable code.");
	}

	/**
     * @param sx
     * @param ox
     */
    private static int handleRDFType( int sx, int ox )
        {
            if (maybeBuiltinID(sx))
				return TYPE_FOR_BUILTIN;
			switch (ox) {
            case Grammar.owlOntology:
                if (isClassOnly(sx))
                    return TYPE_OF_CLASS_ONLY_ID;
                if (!isUserID[sx] && !isBlank[sx])
                    return TYPE_NEEDS_ID_OR_BLANK;
                throw new BrokenException("Unreachable code.");
                   
			case Grammar.rdfsDatatype:
			case Grammar.rdfProperty:
			case Grammar.owlAnnotationProperty:
			case Grammar.owlDatatypeProperty:
			case Grammar.owlFunctionalProperty:
			case Grammar.owlInverseFunctionalProperty:
			case Grammar.owlObjectProperty:
			case Grammar.owlOntologyProperty:
			case Grammar.owlSymmetricProperty:
			case Grammar.owlTransitiveProperty:
			case Grammar.owlDeprecatedProperty:
				if (isClassOnly(sx))
					return TYPE_OF_CLASS_ONLY_ID;
                if (!isUserID[sx] )
                    return TYPE_NEEDS_ID;
                throw new BrokenException("Unreachable code.");
                
			case Grammar.owlDeprecatedClass:
				if (!isUserID[sx] )
					return TYPE_NEEDS_ID;
                throw new BrokenException("Unreachable code.");
                
			case Grammar.rdfList:
			case Grammar.owlAllDifferent:
			case Grammar.owlDataRange:
			case Grammar.owlRestriction:
				if (!isBlank[sx])
					return TYPE_NEEDS_BLANK;
                throw new BrokenException("Unreachable code.");
                
			default:
				if (isBlank[ox] || isClassOnly(ox)) {
					if (isClassOnly(sx))
						return TYPE_OF_CLASS_ONLY_ID;
					if (isPropertyOnly(sx))
						return TYPE_OF_PROPERTY_ONLY_ID;
				}
				if (isClassOnly(sx) && isUserID[ox])
					return TYPE_OF_CLASS_ONLY_ID;
			}
			if (isPropertyOnly(sx)) {

				switch (ox) {
				case Grammar.rdfsDatatype:
				case Grammar.rdfsClass:
				case Grammar.owlClass:
				case Grammar.owlOntology:
				case Grammar.owlDeprecatedClass:
					return TYPE_OF_PROPERTY_ONLY_ID;

				}
				if (isUserID[ox])
					return TYPE_OF_PROPERTY_ONLY_ID;

			}
            throw new BrokenException("Unreachable code.");
        }

    static private boolean maybeBuiltinID(int sx) {
		boolean builtin;
		switch (sx) {
		case Grammar.annotationPropID:// rdfs:seeAlso ??
		case Grammar.datatypeID: //xsd:int
		case Grammar.dataRangeID: //rdfs:Literal
		case Grammar.dataAnnotationPropID: // rdfs:label
		case Grammar.classID:  // owl:Thing
		case Grammar.ontologyPropertyID: // owl:priorVersion
			builtin = true;
			break;
		default:
			builtin = false;
			break;
		}
		return builtin;
	}

	static private boolean isClassOnly(int sx) {
		return look.meet(sx, Grammar.classOnly) == sx;
	}

	static private boolean isPropertyOnly(int sx) {
		return look.meet(sx, Grammar.propertyOnly) == sx;
	}

    /**
         categories without pseudo-categories. Note the pseuds (if any) all appear 
         at the beginning, by (a) sorting and (b) arrangement of the grammar.
    */
	static private int[] nonPseudoCats(int c) {
		int cats[] = CategorySet.getSet(c);
		int i;
		for (i = 0; i < cats.length; i++) {
			if (!Grammar.isPseudoCategory(cats[i]))
				break;
		}
		if (i == 0)
			return cats;

		int rslt[] = new int[cats.length - i];
		System.arraycopy(cats, i, rslt, 0, rslt.length);
		return rslt;

	}

	static private int catMiss(int oz, int o) {
		if (isBlank[oz] != isBlank[o])
			throw new RuntimeException("Logic error");
		int a[] = nonPseudoCats(oz);
		int b[] = nonPseudoCats(o);

		int given = nameCatSet(a, b);
		int wanted = nameCatSet(b, a);

		if (given != NOT_CLASSIFIED && wanted != NOT_CLASSIFIED
				&& given != wanted)
			return DIFFERENT_CATS;
		throw new BrokenException("Logic Error");
	}

	static private int bad = 0;

	// The number of errors of each type.
	static private int eCnt[] = new int[1000];

	// The first three examples found for each sort.
	static private int eExamples[][][] = new int[1000][3][6];

	static private void allCases(int s, int p, int o) {
		for (int i = 0; i < start[s].length; i++)
			for (int j = 0; j < start[p].length; j++)
				for (int k = 0; k < start[o].length; k++) {
					bad++;
					int e = getErrorCode(s, p, o, start[s][i], start[p][j],
							start[o][k]);
					int ix = eCnt[e]++;
					if (ix < 3) {
						eExamples[e][ix][0] = s;
						eExamples[e][ix][1] = p;
						eExamples[e][ix][2] = o;
						eExamples[e][ix][3] = start[s][i];
						eExamples[e][ix][4] = start[p][j];
						eExamples[e][ix][5] = start[o][k];
					}
				}
	}

	static private void foo(String tag, String desc) {
		System.err.println("{ new int[]{ Grammar.description5" + tag + "},");
		System.err.println(" \"a class description participating " + desc
				+ "\",");
		System.err.println("},");
		System.err.println("{ new int[]{ Grammar.restriction6" + tag + ",");
		System.err.println(" Grammar.restriction7" + tag + ",");
		System.err.println(" Grammar.restriction8" + tag + ",");
		System.err.println("},");
		System.err.println(" \"a restriction participating " + desc + "\",");
		System.err.println("},");
		System.err.println("{ new int[]{ Grammar.description5" + tag
				+ ", Grammar.restriction6" + tag + ",");
		System.err.println(" Grammar.restriction7" + tag + ",");
		System.err.println(" Grammar.restriction8" + tag + ",");
		System.err.println("},");
		System.err.println(" \"a description or a restriction participating "
				+ desc + "\",");
		System.err.println("},");

	}

	public static void main(String args[]) {

		/*
		 * foo("object","as the object of a triple other than rdfs:subClassOf,
		 * owl:equivalentClass or owl:disjointWith"); foo("disjointWith","in an
		 * owl:disjointWith construct"); foo("equivalentClass","in an
		 * owl:equivalentClass construct"); foo("subClassOf","in an
		 * rdfs:subClassOf construct");
		 */
		for (int j = 1; j < SZ; j++) {
			if (Grammar.isPseudoCategory(j))
				continue;
			if (isBlank[j])
				continue;
			if (isLiteral(j))
				continue;
			for (int i = 1; i < SZ; i++) {
				if (Grammar.isPseudoCategory(i))
					continue;
				if (isLiteral(i))
					continue;
				for (int k = 1; k < SZ; k++) {
					if (Grammar.isPseudoCategory(k))
						continue;
					if (look.qrefine(i, j, k) == Failure) {
						allCases(i, j, k);

					}

					// TODO meet stuff - probably dead this task
				}
			}

		}
		System.out.println(bad + " cases considered.");
		dump("Codes:", eCnt);
		dump("Misses", miss);

		for (int i = 0; i < SZ; i++)
			if (diffPreds[i] != 0)
				System.out.println(CategorySet.catString(i) + " "
						+ diffPreds[i]);
		for (int i = 0; i < names.length; i++)
			for (int j = 0; j < names.length; j++) {
				switch (nameInfo[i][j]) {
				case 0:
				case 7:
				case 1:
					break;
				case 3:
				case 5:
					//showNameInfo(i, j);
					break;
				case 2:
				case 4:
				case 6:
					throw new BrokenException("Logic error");
				}
			}
		for (int i = 0; i < usedName.length; i++)
			if (!usedName[i])
				System.err.println("Unused: \"" + names[i][1] + "\"");
	}

	static private String fieldName[] = { "subj", "pred", "obj ", "S1  ",
			"P1  ", "01  " };

	static private void dump(String s, int a[]) {
		boolean examples = s.startsWith("Codes");
		Field fld[] = MonotonicErrorAnalyzer.class.getDeclaredFields();
		Field fldsByN[] = new Field[1000];
		if (examples) {
			for (int i = 0; i < fld.length; i++) {
				if (fld[i].getType() == Integer.TYPE
						&& (fld[i].getModifiers() & Modifier.STATIC) != 0) {
					int vl = 0;
					try {
						vl = fld[i].getInt(null);
					} catch (IllegalAccessException ee) {
						throw new RuntimeException(ee);
					}
					if (vl >= 0 && vl < 1000) {
						if (fldsByN[vl] != null) {
							System.err.println("Flds: " + fldsByN[vl].getName()
									+ " & " + fld[i].getName());

						}
						fldsByN[vl] = fld[i];
					}
				}

			}
		}
		System.out.println(s);
		for (int i = 0; i < a.length; i++)
			if (a[i] != 0) {
				System.out.println(i + "\t" + a[i]);
				if (examples) {
					if (fldsByN[i] != null)
						System.out.println(fldsByN[i].getName());
					for (int j = 0; j < 0 && j < a[i]; j++) {
						System.out.println("Ex: " + j);
						for (int k = 0; k < 6; k++)
							System.out
									.println("    "
											+ fieldName[k]
											+ " "
											+ CategorySet
													.catString(eExamples[i][j][k]));

					}
				}
			}
	}

}

/*
 * (c) Copyright 2003-2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
