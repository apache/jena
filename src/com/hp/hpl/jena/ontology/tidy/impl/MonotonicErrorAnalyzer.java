/*
 (c) Copyright 2003-2005 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: MonotonicErrorAnalyzer.java,v 1.3 2005-01-02 20:30:51 jeremy_carroll Exp $
 */
package com.hp.hpl.jena.ontology.tidy.impl;

import java.util.*;
import java.lang.reflect.*;

/**
 * 
 * This class looks at particular triples and tries to work out what went wrong,
 * giving a specific anaylsis.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll </a>
 *  
 */
class MonotonicErrorAnalyzer implements Constants {
	final static int TYPE_FOR_BUILTIN = 184;

	final static int TYPE_OF_PROPERTY_ONLY_ID = 183;

	final static int TYPE_OF_CLASS_ONLY_ID = 182;

	final static int BAD_RANGE_LIST_DESC = 140;

	final static int BAD_RANGE_GENERAL_CLASS = 141;

	final static int BAD_RANGE_HAS_VALUE = 142;

	final static int BAD_RANGE_CARDINALITY = 143;

	final static int BAD_RANGE_INDIVIDUAL = 144;

	final static int BAD_RANGE_LIST = 145;

	final static int BAD_RANGE_FIRST = 146;

	final static int BAD_RANGE_PROP = 147;

	final static int BAD_RANGE_LIST_IND = 148;

	final static int BAD_RANGE_ONEOF = 149;

	final static int BAD_RANGE_CLASS_DATATYPE = 150;

	final static int BAD_RANGE_TYPE = 151;

	final static int BAD_RANGE_ONT_PROP = 152;

	final static int BAD_DOMAIN_REST = 160;

	final static int BAD_DOMAIN_ALL_DIFF = 161;

	final static int BAD_DOMAIN_INDIVIDUAL = 162;

	final static int BAD_DOMAIN_LIST = 163;

	final static int BAD_DOMAIN_PROP = 164;

	final static int BAD_DOMAIN_CLASS_DESC = 165;

	final static int BAD_DOMAIN_GENERAL_CLASS = 166;

	//final static int BAD_DOMAIN_CLASS = 167;

	final static int BAD_DOMAIN_TYPE = 168;

	final static int BAD_DOMAIN_ONT_PROP = 169;

	static final int GENERIC = 100;

	static final int SINGLE_TRIPLE = 101;

	static final int DIFFICULT = 110;

	static final int OBJ_PROP_AND_LIT = 111;

	static final int DATA_PROP_AND_NONLIT = 112;

	static final int NOT_ANNO_ON_NON_INDIV = 113;

	final static int GENERIC_OBJPROP = 114;

	final static int OBJPROP_WITH_NON_INDIV_BLANK_OBJ = 115;

	final static int OBJPROP_WITH_NON_INDIV_ID_OBJ = 116;

	final static int NOT_ANNO_WITH_OBJ_NON_INDIV = 117;

	final static int ONTPROP_BAD_SUBJ = 118;

	final static int ONTPROP_BLANK_OBJ = 119;

	final static int ONTPROP_BAD_OBJ = 120;

	final static int BLANK_PROP = 130;

	final static int LITERAL_PROP = 131;

	final static int LITERAL_SUBJ = 132;

	final static int BADID_USE = 133;

	final static int BUILTIN_NON_PRED = 134;

	final static int BUILTIN_NON_SUBJ = 135;

	final static int CLASS_AS_PROP = 136;

	final static int DATA_ANN_PROP_BAD_OBJ = 137;

	final static int ANNPROP_WITH_NON_INDIV_BLANK_OBJ = 138;

	final static int TYPE_NEEDS_ID = 180;

	final static int TYPE_NEEDS_BLANK = 181;

	final static int DIFFERENT_CATS = 200;
	
	// begin unused

	final static int DIFFERENT_DESCRIPTIONS = 201;

	final static int OBJ_OR_ANNOTATION_PROP = 202;

	final static int DIFFERENT_LISTS = 203;

	final static int DIFFERENT_DATA_OR_IND_LISTS = 204;

	final static int DATA_PROP_OR_NON_DATA_PROP = 205;

	final static int OBJ_PROP_OR_NON_OBJ_PROP = 206;

	final static int OWL_PROP_OR_NON_OWL_PROP = 207;

	final static int OBJ_OR_DATA_RESTRICTION = 208;

	final static int DIFFERENT_DATA_OR_NONDATA_LISTS = 209;

	final static int BAD_TRANS_RESTRICTION = 210;

	final static int BAD_TRANS_PROP = 211;

	final static int ONTO_PROP_ON_INDIVIDUAL = 212;

	// These next three not used.
	// Difficult cases ...

	final static int LIST_MISMATCH = 220;

	final static int TRANS_PROP_MISMATCH = 221;

	final static int DATAPROP_OBJPROP_MISMATCH = 222;
	
	// end unused
	
	final static int DC_DOM_RANGE = 223;

	static LookupTable look = (LookupTable) LookupTable.get();

	static final int SZ = CategorySet.unsorted.size();

	static final boolean isClassOnly[] = new boolean[SZ];

	static final boolean isPropertyOnly[] = new boolean[SZ];

	static final boolean isUserID[] = new boolean[SZ];

	static final boolean isBlank[] = new boolean[SZ];

	static final boolean isBuiltin[] = new boolean[SZ];

	static final int start[][] = new int[SZ][];

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

	static int miss[] = new int[8];

	static int mTypes[] = new int[40];

	static int getErrorCode(int s, int p, int o, int sx, int px, int ox) {
		int key = look.qrefine(sx, px, ox);
		if (key == Failure) {
			return singleTripleError(sx, px, ox);
		} else {
			int misses = 0;
			int sz = look.subject(sx, key);
			int pz = look.prop(px, key);
			int oz = look.object(ox, key);
			int rslt = GENERIC;
			int mType = 0;
			if (look.meet(sz, s) == Failure) {
				misses |= 1;
				int r = catMiss(sz, s);
				if (r != GENERIC) {
					rslt = r;
					mType++;
				} else {
					mType += 10;
				}
			}
			if (look.meet(pz, p) == Failure) {
				misses |= 2;
				int r = catMiss(pz, p);
				if (r != GENERIC) {
					rslt = r;
					mType++;
				} else {
					mType += 10;
				}
			}
			if (look.meet(oz, o) == Failure) {
				misses |= 4;
				int r = catMiss(oz, o);
				if (r != GENERIC) {
					rslt = r;
					mType++;
				} else {
					mType += 10;
				}
			}
			miss[misses]++;
			mTypes[mType]++;
			if (misses == 0) {
				return difficultCase(s, p, o, sz, pz, oz);
			}
			return rslt;
		}
	}

	private static boolean isNonTransOWLProp(int s, int sc[]) {
		return isOWLPropertyID(s) && !Q.member(Grammar.transitivePropID, sc);

	}

	private static int diffPreds[] = new int[SZ];

	private static int difficultCase(int s, int p, int o, int sz, int pz, int oz) {
		int rslt = DIFFICULT;
		if (isOWLPropertyID(p) && isNotIndividual(s))
			return NOT_ANNO_ON_NON_INDIV;
		if (isOWLPropertyID(p) && isUserID[o] && isNotIndividual(o))
			return NOT_ANNO_WITH_OBJ_NON_INDIV;
		if (isOntologyProp(p) && isNotOntology(s))
			return ONTPROP_BAD_SUBJ;
		/*
		 * unused code if (isOntologyProp(p) && isBlank[o]) return
		 * ONTPROP_BLANK_OBJ;
		 */
		if (isOntologyProp(p) && isNotOntology(o))
			return ONTPROP_BAD_OBJ;
		/*
		 * unused code if (isAnnotationProp(p) && isBlank[o] &&
		 * isNotIndividual(o)) return ANNPROP_WITH_NON_INDIV_BLANK_OBJ;
		 * 
		 * if (isObjectProperty(p) && isNotIndividual(o)) {
		 * 
		 * if (isBlank[o]) return OBJPROP_WITH_NON_INDIV_BLANK_OBJ;
		 * 
		 * if (isUserID[o]) return OBJPROP_WITH_NON_INDIV_ID_OBJ; rslt =
		 * DIFFICULT; }
		 */

		int sc[] = nonPseudoCats(s);
		int oc[] = nonPseudoCats(o);
		
		

		int sSC = simpleClassify(s, sc);
		int oSC = simpleClassify(o, oc);
		int sGivenName = -1, sWantedName = -1, 
		oGivenName = -1, oWantedName = -1;
		int key = look.qrefine(s, pz, oz);
		if (key == Failure) {
			System.err.println("Unexpected object failure.");
		} else {
			int o2 = look.object(oz,key);
			int o2c[] = nonPseudoCats(o2);
			if (Q.intersect(oc,o2c)) {
				System.err.println("??1 "+
						"S "+CategorySet.catString(s)+
						"P "+CategorySet.catString(p) +
						"O "+CategorySet.catString(o)
						);
				System.err.println("??2 "+
						"S "+CategorySet.catString(sz)+
						"P "+CategorySet.catString(pz) +
						"O "+CategorySet.catString(oz)
						);
				System.err.println("??3 O "+CategorySet.catString(o2));
			}
			oGivenName = nameCatSet(oc,o2c);
			oWantedName = nameCatSet(o2c,oc);
		}
		
		
		key = look.qrefine(sz, pz, o);
		if (key == Failure) {
			System.err.println("Unexpected subject failure.");
		} else {
			int s2 = look.subject(sz,key);
			int s2c[] = nonPseudoCats(s2);
			if (Q.intersect(sc,s2c)) {
				System.err.println("?#1 "+
						"S "+CategorySet.catString(s)+
						"P "+CategorySet.catString(p) +
						"O "+CategorySet.catString(o)
						);
				System.err.println("?#2 "+
						"S "+CategorySet.catString(sz)+
						"P "+CategorySet.catString(pz) +
						"O "+CategorySet.catString(oz)
						);
				System.err.println("?#3 S "+CategorySet.catString(s2));
			}			sGivenName = nameCatSet(sc,s2c);
			sWantedName = nameCatSet(s2c,sc);
		}
		
		if (sGivenName!= -1 &&
				sWantedName != -1 &&
				oGivenName != -1 &&
				oWantedName != -1)
			return DC_DOM_RANGE;

		switch (p) {

		case Grammar.rdfrest:
			if (sc.length == 1 && oc.length == 1 && sc[0] != oc[0]
					&& sSC == SC.list && oSC == SC.list)
				return LIST_MISMATCH;
			break;
		case Grammar.owlequivalentProperty:
		case Grammar.owlinverseOf:
		case Grammar.rdfssubPropertyOf:
			if (Q.member(Grammar.dataPropID, sc)
					&& !Q.member(Grammar.dataPropID, oc)
					&& Q.intersect(objProps, oc) && !Q.intersect(objProps, sc))
				return DATAPROP_OBJPROP_MISMATCH;
			if (!Q.member(Grammar.dataPropID, sc)
					&& Q.member(Grammar.dataPropID, oc)
					&& !Q.intersect(objProps, oc) && Q.intersect(objProps, sc))
				return DATAPROP_OBJPROP_MISMATCH;
			if (oc.length == 1 && oc[0] == Grammar.transitivePropID
					&& isNonTransOWLProp(s, sc)
					&& p != Grammar.rdfssubPropertyOf)
				return TRANS_PROP_MISMATCH;
			if (sc.length == 1 && sc[0] == Grammar.transitivePropID
					&& isNonTransOWLProp(o, oc))
				return TRANS_PROP_MISMATCH;

		case Grammar.owlonProperty:
		case Grammar.owlsomeValuesFrom:
		case Grammar.rdffirst:
		case Grammar.rdfsrange:

			break;
		case Grammar.owloneOf:
			if (debug()) {
				dumpc("DC-subj", s, sz);
				dumpc("DC-prop", p, pz);
				dumpc("DC-obj", o, oz);
			}
		}
		diffPreds[p]++;

		return rslt;
	}

	private static boolean isNotOntology(int s) {
		int c[] = CategorySet.getSet(s);
		return !Q.intersect(ontos, c);
	}

	private static boolean isNonTransProp(int s[]) {
		switch (s.length) {
		case 1:
			return s[0] == Grammar.objectPropID;
		case 2:
			return s[0] == Grammar.dataPropID && s[1] == Grammar.objectPropID;
		}
		return false;
	}

	static int ontp2 = CategorySet.find(new int[] { Grammar.notype,
			Grammar.ontologyPropertyID }, false);

	private static boolean isOntologyProp(int p) {
		return p == Grammar.ontologyPropertyID || p == ontp2;
	}

	static int annop2 = CategorySet.find(new int[] { Grammar.notype,
			Grammar.annotationPropID }, false);

	private static boolean isAnnotationProp(int p) {
		return p == Grammar.annotationPropID || p == annop2;
	}

	static int class2 = CategorySet.find(new int[] { Grammar.notype,
			Grammar.classID }, false);

	private static boolean isClassID(int p) {
		return p == Grammar.classID || p == class2;
	}

	static int dp1 = Grammar.dataPropID;

	static int dp2 = CategorySet.find(new int[] { Grammar.notype,
			Grammar.dataPropID }, false);

	private static boolean isDataProp(int p) {
		return p == dp1 || p == dp2;
	}

	static int op1 = Grammar.objectPropID;

	static int op2 = Grammar.transitivePropID;

	static int op3 = CategorySet.find(new int[] { Grammar.notype,
			Grammar.objectPropID }, false);

	static int op4 = CategorySet.find(new int[] { Grammar.notype,
			Grammar.transitivePropID }, false);

	static int op5 = CategorySet.find(new int[] { Grammar.objectPropID,
			Grammar.transitivePropID }, false);

	static int op6 = CategorySet.find(new int[] { Grammar.objectPropID,
			Grammar.notype, Grammar.transitivePropID }, false);

	private static boolean isObjectProperty(int p) {
		return p == op1 || p == op2 || p == op3 || p == op4 || p == op5
				|| p == op6;
	}

	private static boolean isLiteral(int o) {
		return o == Grammar.literal || o == Grammar.liteInteger
				|| o == Grammar.dlInteger || o == Grammar.userTypedLiteral;
	}

	private static boolean isNotLiteral(int o) {
		return !isLiteral(o);
	}

	static final int indv[] = new int[] { Grammar.unnamedIndividual,
			Grammar.individualID };

	static final int annOrOnt[] = new int[] { Grammar.annotationPropID,
			Grammar.dataAnnotationPropID, Grammar.ontologyPropertyID };

	static final int ontos[] = new int[] { Grammar.ontologyID,
			Grammar.unnamedOntology };

	static final int owlProps[] = new int[] { Grammar.transitivePropID,
			Grammar.objectPropID, Grammar.dataPropID };

	static final int owlProps2[] = new int[] { Grammar.notype,
			Grammar.transitivePropID, Grammar.objectPropID, Grammar.dataPropID };

	static final int unnamedIndividualOrOntology[] = new int[] {
			Grammar.unnamedIndividual, Grammar.unnamedOntology };

	static final int descriptions[] = new int[] {
			Grammar.description5disjointWith,
			Grammar.description5equivalentClass, Grammar.description5object,
			Grammar.description5subClassOf };

	static final int descriptionsAndDatarange[] = new int[] {
			Grammar.description5disjointWith, Grammar.unnamedDataRange,
			Grammar.description5equivalentClass, Grammar.description5object,
			Grammar.description5subClassOf };

	static final int lists[] = new int[] { Grammar.listOfDataLiteral,
			Grammar.listOfDescription, Grammar.listOfIndividualID };

	static final int restrictions[] = new int[] {
			Grammar.restriction6disjointWith,
			Grammar.restriction6equivalentClass, Grammar.restriction6object,
			Grammar.restriction6subClassOf, Grammar.restriction7disjointWith,
			Grammar.restriction7equivalentClass, Grammar.restriction7object,
			Grammar.restriction7subClassOf, Grammar.restriction8disjointWith,
			Grammar.restriction8equivalentClass, Grammar.restriction8object,
			Grammar.restriction8subClassOf };

	static final int dataPropRestrictions[] = new int[] {
			Grammar.restriction6disjointWith,
			Grammar.restriction6equivalentClass, Grammar.restriction6object,
			Grammar.restriction6subClassOf, };

	static final int objProps[] = new int[] { Grammar.objectPropID,
			Grammar.transitivePropID };

	static final int objPropRestrictions[] = new int[] {
			Grammar.restriction7disjointWith,
			Grammar.restriction7equivalentClass, Grammar.restriction7object,
			Grammar.restriction7subClassOf, Grammar.restriction8disjointWith,
			Grammar.restriction8equivalentClass, Grammar.restriction8object,
			Grammar.restriction8subClassOf };

	static final int nonTransPropRestrictions[] = new int[] {
			Grammar.restriction6disjointWith,
			Grammar.restriction6equivalentClass, Grammar.restriction6object,
			Grammar.restriction6subClassOf, Grammar.restriction7disjointWith,
			Grammar.restriction7equivalentClass, Grammar.restriction7object,
			Grammar.restriction7subClassOf, };

	static final int transPropRestrictions[] = new int[] {
			Grammar.restriction8disjointWith,
			Grammar.restriction8equivalentClass, Grammar.restriction8object,
			Grammar.restriction8subClassOf };

	static final int restrictionsAndDescriptions[] = new int[] {

	Grammar.description5disjointWith, Grammar.description5equivalentClass,
			Grammar.description5object, Grammar.description5subClassOf,
			Grammar.restriction6disjointWith,
			Grammar.restriction6equivalentClass, Grammar.restriction6object,
			Grammar.restriction6subClassOf, Grammar.restriction7disjointWith,
			Grammar.restriction7equivalentClass, Grammar.restriction7object,
			Grammar.restriction7subClassOf, Grammar.restriction8disjointWith,
			Grammar.restriction8equivalentClass, Grammar.restriction8object,
			Grammar.restriction8subClassOf };

	static final int disjointWith[] = new int[] {

	Grammar.description5disjointWith, Grammar.restriction6disjointWith,
			Grammar.restriction7disjointWith, Grammar.restriction8disjointWith, };

	static final int subClass[] = new int[] { Grammar.description5subClassOf,
			Grammar.restriction6subClassOf, Grammar.restriction7subClassOf,
			Grammar.restriction8subClassOf };

	static final int equivalentClass[] = new int[] {
			Grammar.description5equivalentClass,
			Grammar.restriction6equivalentClass,
			Grammar.restriction7equivalentClass,
			Grammar.restriction8equivalentClass };

	static final int classOrDatarangeObject[] = new int[] {
			Grammar.description5object, Grammar.restriction6object,
			Grammar.restriction7object, Grammar.restriction8object,
			Grammar.unnamedDataRange };

	static final int restrictionsAndDescriptionsAndDatarange[] = new int[] {
			Grammar.unnamedDataRange, Grammar.description5disjointWith,
			Grammar.description5equivalentClass, Grammar.description5object,
			Grammar.description5subClassOf, Grammar.restriction6disjointWith,
			Grammar.restriction6equivalentClass, Grammar.restriction6object,
			Grammar.restriction6subClassOf, Grammar.restriction7disjointWith,
			Grammar.restriction7equivalentClass, Grammar.restriction7object,
			Grammar.restriction7subClassOf, Grammar.restriction8disjointWith,
			Grammar.restriction8equivalentClass, Grammar.restriction8object,
			Grammar.restriction8subClassOf };

	static final int userPropIDs[] = { Grammar.annotationPropID,
			Grammar.dataPropID, Grammar.objectPropID,
			Grammar.ontologyPropertyID, Grammar.transitivePropID,

	};

	static final int individualPropIDs[] = { Grammar.annotationPropID,
			Grammar.dataPropID, Grammar.objectPropID, Grammar.transitivePropID, };

	static final int annoOntoObjectPropIDs[] = { Grammar.annotationPropID,
			Grammar.objectPropID, Grammar.ontologyPropertyID,
			Grammar.transitivePropID };

	static final int annoDataPropIDs[] = { Grammar.annotationPropID,
			Grammar.dataPropID, };

	static final int objectOrAnnotationPropIDs[] = { Grammar.annotationPropID,
			Grammar.objectPropID, Grammar.transitivePropID,

	};

	static final int ontologyOrAnnotationPropIDs[] = {
			Grammar.annotationPropID, Grammar.ontologyPropertyID,

	};
	static {
		Arrays.sort(annOrOnt);
		Arrays.sort(objectOrAnnotationPropIDs);
		Arrays.sort(ontologyOrAnnotationPropIDs);
		Arrays.sort(annoOntoObjectPropIDs);
		Arrays.sort(annoDataPropIDs);
		Arrays.sort(indv);
		Arrays.sort(ontos);
		Arrays.sort(owlProps);
		Arrays.sort(owlProps2);
		Arrays.sort(lists);
		Arrays.sort(dataPropRestrictions);
		Arrays.sort(transPropRestrictions);
		Arrays.sort(nonTransPropRestrictions);
		Arrays.sort(objPropRestrictions);
		Arrays.sort(objProps);
		Arrays.sort(restrictions);
		Arrays.sort(descriptions);
		Arrays.sort(descriptionsAndDatarange);
		Arrays.sort(classOrDatarangeObject);
		Arrays.sort(equivalentClass);
		Arrays.sort(subClass);
		Arrays.sort(disjointWith);
		Arrays.sort(restrictionsAndDescriptionsAndDatarange);
		Arrays.sort(restrictionsAndDescriptions);
		Arrays.sort(unnamedIndividualOrOntology);
		Arrays.sort(userPropIDs);
		Arrays.sort(individualPropIDs);
	}

	private static boolean isNotIndividual(int s) {
		int c[] = CategorySet.getSet(s);
		return !Q.intersect(indv, c);
	}

	private static boolean isIndividual(int s) {
		int c[] = CategorySet.getSet(s);
		return Q.subset(c, indv);
	}

	private static boolean isNotAnnOrOntoProp(int p) {
		int c[] = CategorySet.getSet(p);
		return !Q.intersect(annOrOnt, c);
	}

	static int empty[] = {};

	static Object names[][] = {
			{		new int[] { Grammar.unnamedIndividual,
					Grammar.individualID }, "an individual" },
			{		new int[] { Grammar.unnamedOntology,
					Grammar.ontologyID }, "an ontology" },
			{ new int[] { Grammar.unnamedIndividual }, "an unnamed individual" },
			{ new int[] { Grammar.unnamedOntology }, "an unnamed ontology" },
			{ new int[] {
					  Grammar.unnamedIndividual, Grammar.unnamedOntology }
			, "an unnamed individual or unnamed ontology" },
			{ new int[] { Grammar.allDifferent },
					"a blank node in an owl:AllDifferent construction" },
			{
					new int[] { Grammar.description5disjointWith,
							Grammar.description5equivalentClass,
							Grammar.description5object,
							Grammar.description5subClassOf },
					"a class description" },
			{		new int[] { Grammar.listOfDataLiteral,
							Grammar.listOfDescription,
							Grammar.listOfIndividualID }, "a list" },
			{		new int[] { Grammar.restriction6disjointWith,
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
								Grammar.unnamedDataRange},
						"a class description or a datarange" },
					{
						 new int[] {
						 Grammar.description5disjointWith, Grammar.description5equivalentClass,
						Grammar.description5object, Grammar.description5subClassOf,
						Grammar.restriction6disjointWith, Grammar.restriction6equivalentClass,
						 Grammar.restriction6object, Grammar.restriction6subClassOf,
						Grammar.restriction7disjointWith, Grammar.restriction7equivalentClass,
						 Grammar.restriction7object, Grammar.restriction7subClassOf,
						Grammar.restriction8disjointWith, Grammar.restriction8equivalentClass,
						 Grammar.restriction8object, Grammar.restriction8subClassOf },
						 "a class description or a property restriction" },
							{
							 new int[] {
							 Grammar.description5disjointWith, Grammar.description5equivalentClass,
							Grammar.description5object, Grammar.description5subClassOf,
							Grammar.restriction6disjointWith, Grammar.restriction6equivalentClass,
							 Grammar.restriction6object, Grammar.restriction6subClassOf,
							Grammar.restriction7disjointWith, Grammar.restriction7equivalentClass,
							 Grammar.restriction7object, Grammar.restriction7subClassOf,
							Grammar.restriction8disjointWith, Grammar.restriction8equivalentClass,
							 Grammar.restriction8object, Grammar.restriction8subClassOf,
							 Grammar.unnamedDataRange},
							 "a class description or a property restriction or a datarange" },
						 
					 { new int[] {
					  Grammar.restriction6disjointWith, Grammar.restriction6equivalentClass,
					  Grammar.restriction6object, Grammar.restriction6subClassOf, },
					  "a restriction on a datatype property" }, 
					 { new int[] { Grammar.restriction7disjointWith,
					 		  Grammar.restriction7equivalentClass, Grammar.restriction7object,
					 		  Grammar.restriction7subClassOf, Grammar.restriction8disjointWith,
					 		  Grammar.restriction8equivalentClass, Grammar.restriction8object,
					 		  Grammar.restriction8subClassOf },
							  "a restriction on an object property" },
							  
					{ new int[] { Grammar.unnamedDataRange }, "a datarange" },
					{ new int[] {
					  Grammar.description5disjointWith, Grammar.unnamedDataRange,
					  Grammar.description5equivalentClass, Grammar.description5object,
		
					 Grammar.description5subClassOf }, "a description or datarange" },
					 {new int[] {
 Grammar.restriction7disjointWith, Grammar.restriction7equivalentClass,
							  Grammar.restriction7object, Grammar.restriction7subClassOf, },
							  "a restriction on a non-transitive object property" },

					 {new int[] {
					  Grammar.restriction6disjointWith, Grammar.restriction6equivalentClass,
					  Grammar.restriction6object, Grammar.restriction6subClassOf,
					  Grammar.restriction7disjointWith, Grammar.restriction7equivalentClass,
					  Grammar.restriction7object, Grammar.restriction7subClassOf, },
					  "a restriction on a datatype property or on a non-transitive object property" },
					  { new int[] {
Grammar.restriction8disjointWith, Grammar.restriction8equivalentClass,
Grammar.restriction8object, Grammar.restriction8subClassOf },
"a restriction on a transitive object property" },


{ new int[]{ Grammar.description5object},
	 "a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith",
	},
	{ new int[]{ Grammar.unnamedDataRange, Grammar.description5object},
		 "a datarange or a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith",
		},
	{ new int[]{ Grammar.restriction6object,
	 Grammar.restriction7object,
	 Grammar.restriction8object,
	},
	 "a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith",
	},
	{ new int[]{ Grammar.description5object, Grammar.restriction6object,
	 Grammar.restriction7object,
	 Grammar.restriction8object,
	},
	 "a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith",
	},
	{ new int[]{ Grammar.description5object, Grammar.restriction6object,
			 Grammar.restriction7object,
			 Grammar.restriction8object,
			 Grammar.unnamedDataRange,
			},
			 "a datarange or a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith",
			},
	{ new int[]{ Grammar.description5disjointWith},
	 "a class description participating in an owl:disjointWith construct",
	},
	{ new int[]{ Grammar.restriction6disjointWith,
	 Grammar.restriction7disjointWith,
	 Grammar.restriction8disjointWith,
	},
	 "a restriction participating in an owl:disjointWith construct",
	},
	{ new int[]{ Grammar.description5disjointWith, Grammar.restriction6disjointWith,
	 Grammar.restriction7disjointWith,
	 Grammar.restriction8disjointWith,
	},
	 "a description or a restriction participating in an owl:disjointWith construct",
	},
	{ new int[]{ Grammar.description5equivalentClass},
	 "a class description participating in an owl:equivalentClass construct",
	},
	{ new int[]{ Grammar.restriction6equivalentClass,
	 Grammar.restriction7equivalentClass,
	 Grammar.restriction8equivalentClass,
	},
	 "a restriction participating in an owl:equivalentClass construct",
	},
	{ new int[]{ Grammar.description5equivalentClass, Grammar.restriction6equivalentClass,
	 Grammar.restriction7equivalentClass,
	 Grammar.restriction8equivalentClass,
	},
	 "a description or a restriction participating in an owl:equivalentClass construct",
	},
	{ new int[]{ Grammar.description5subClassOf},
	 "a class description participating in an rdfs:subClassOf construct",
	},
	{ new int[]{ Grammar.restriction6subClassOf,
	 Grammar.restriction7subClassOf,
	 Grammar.restriction8subClassOf,
	},
	 "a restriction participating in an rdfs:subClassOf construct",
	},
	{ new int[]{ Grammar.description5subClassOf, Grammar.restriction6subClassOf,
	 Grammar.restriction7subClassOf,
	 Grammar.restriction8subClassOf,
	},
	 "a description or a restriction participating in an rdfs:subClassOf construct",
	},

					 
					 { new int[] { Grammar.annotationPropID }, "an annotation property" },
			{ new int[] { Grammar.classID }, "a named class" },
			{ new int[] { Grammar.dataPropID }, "a datatype property" },
			{ new int[] { Grammar.datatypeID }, "a datatype" },
			{ new int[] { Grammar.individualID }, "a named individual" },
			{ new int[] { Grammar.objectPropID, Grammar.transitivePropID },
					"an object property" },
					{ new int[] {  Grammar.transitivePropID },
					"a transitive object property" },
					{ new int[] { Grammar.transitivePropID },
					"a non-transitive object property" },

			{ new int[] { Grammar.ontologyID }, "a named ontology" },
			{ new int[] { Grammar.ontologyPropertyID }, "an ontology property" },

			{ new int[] { Grammar.classID ,Grammar.individualID },
				"a named class or a named individual"
			},
			{ new int[] { Grammar.listOfDescription },
				"a list of class expressions",
			},
			{ new int[] { Grammar.listOfIndividualID },
				"a list of named individuals"
			},
			{ new int[] { Grammar.classID ,Grammar.datatypeID },
				"a named class or a datatype identifier"
			},
			{ new int[] { Grammar.listOfDataLiteral ,Grammar.listOfIndividualID },
				"a list of literals or a list of named individuals"
			},
			{ new int[] { Grammar.annotationPropID ,
					Grammar.objectPropID ,
					Grammar.transitivePropID },
					"an annotation property or an object property"
			},
			{ new int[]	{Grammar.annotationPropID ,
					Grammar.dataPropID ,
					Grammar.objectPropID ,
					
					Grammar.ontologyPropertyID ,
					Grammar.transitivePropID },
					"a property of some sort"
			},
			
			{ new int[] { Grammar.annotationPropID ,
					Grammar.objectPropID ,Grammar.ontologyPropertyID ,
					Grammar.transitivePropID },
					"an annotation property, an object property or an ontology property"
			},

			{ new int[] { Grammar.annotationPropID ,
					Grammar.objectPropID ,
					Grammar.dataPropID,
					Grammar.transitivePropID },
					"an annotation property, a datatype property or an object property"
			},
			{ new int[] { Grammar.annotationPropID ,
					Grammar.dataPropID },
					"a datatype property or an annotation property"
			},
			{ new int[] { Grammar.objectPropID },
				"a non-transitive object property"
			},
			{ new int[] { Grammar.dataPropID ,Grammar.objectPropID },
				"a datatype property or a non-transitive object property"
			},
			{ new int[] { Grammar.listOfDescription ,
					Grammar.listOfIndividualID },
					"a list of class expressions or a list of named individuals"
			},
			{ new int[] { Grammar.listOfDataLiteral },
				"a list of literals"
			},
			{ CategorySet.getSet(Grammar.userID), "a user ID", },

			{
					new int[] { Grammar.transitivePropID, Grammar.objectPropID,
							Grammar.dataPropID },
							"an object or datatype property" },
					{ new int[] { Grammar.annotationPropID,
							  Grammar.dataAnnotationPropID, Grammar.ontologyPropertyID },
							  "an anotation or ontology property" },
							  
			{
					new int[] { Grammar.dlInteger, Grammar.liteInteger,
							Grammar.literal, Grammar.userTypedLiteral },
					"a literal", },

			{ CategorySet.getSet(Grammar.blank), "a blank node", },

	};

	static {
		for (int i=0;i<names.length;i++) {
			Arrays.sort((int[])names[i][0]);
		}
};
	/*
	 * static final int indv[] = new int[] { Grammar.unnamedIndividual,
	 * Grammar.individualID };
	 * 
	 * static final int annOrOnt[] = new int[] { Grammar.annotationPropID,
	 * Grammar.dataAnnotationPropID, Grammar.ontologyPropertyID };
	 * 
	 * static final int ontos[] = new int[] { Grammar.ontologyID,
	 * Grammar.unnamedOntology };
	 * 
	 * static final int owlProps[] = ;
	 * 
	 * static final int owlProps2[] = new int[] { Grammar.notype,
	 * Grammar.transitivePropID, Grammar.objectPropID, Grammar.dataPropID };
	 * 
	 * static final int unnamedIndividualOrOntology[] = new int[] {
	 * Grammar.unnamedIndividual, Grammar.unnamedOntology };
	 * 
	 * static final int descriptions[] = new int[] {
	 * Grammar.description5disjointWith, Grammar.description5equivalentClass,
	 * Grammar.description5object, Grammar.description5subClassOf };
	 * 
	 * static final int descriptionsAndDatarange[] = new int[] {
	 * Grammar.description5disjointWith, Grammar.unnamedDataRange,
	 * Grammar.description5equivalentClass, Grammar.description5object,
	 * Grammar.description5subClassOf };
	 * 
	 * static final int lists[] = new int[] { Grammar.listOfDataLiteral,
	 * Grammar.listOfDescription, Grammar.listOfIndividualID };
	 * 
	 * 
	 * static final int dataPropRestrictions[] = new int[] {
	 * Grammar.restriction6disjointWith, Grammar.restriction6equivalentClass,
	 * Grammar.restriction6object, Grammar.restriction6subClassOf, };
	 * 
	 * static final int objProps[] = new int[]{ Grammar.objectPropID,
	 * Grammar.transitivePropID }; static final int objPropRestrictions[] = new
	 * int[] { Grammar.restriction7disjointWith,
	 * Grammar.restriction7equivalentClass, Grammar.restriction7object,
	 * Grammar.restriction7subClassOf, Grammar.restriction8disjointWith,
	 * Grammar.restriction8equivalentClass, Grammar.restriction8object,
	 * Grammar.restriction8subClassOf };
	 * 
	 * static final int nonTransPropRestrictions[] = new int[] {
	 * Grammar.restriction6disjointWith, Grammar.restriction6equivalentClass,
	 * Grammar.restriction6object, Grammar.restriction6subClassOf,
	 * Grammar.restriction7disjointWith, Grammar.restriction7equivalentClass,
	 * Grammar.restriction7object, Grammar.restriction7subClassOf, };
	 * 
	 * static final int transPropRestrictions[] = new int[] {
	 * Grammar.restriction8disjointWith, Grammar.restriction8equivalentClass,
	 * Grammar.restriction8object, Grammar.restriction8subClassOf };
	 * 
	 * static final int restrictionsAndDescriptions[] = new int[] {
	 * 
	 * Grammar.description5disjointWith, Grammar.description5equivalentClass,
	 * Grammar.description5object, Grammar.description5subClassOf,
	 * Grammar.restriction6disjointWith, Grammar.restriction6equivalentClass,
	 * Grammar.restriction6object, Grammar.restriction6subClassOf,
	 * Grammar.restriction7disjointWith, Grammar.restriction7equivalentClass,
	 * Grammar.restriction7object, Grammar.restriction7subClassOf,
	 * Grammar.restriction8disjointWith, Grammar.restriction8equivalentClass,
	 * Grammar.restriction8object, Grammar.restriction8subClassOf };
	 * 
	 * static final int disjointWith[] = new int[] {
	 * 
	 * Grammar.description5disjointWith, Grammar.restriction6disjointWith,
	 * Grammar.restriction7disjointWith, Grammar.restriction8disjointWith, };
	 * 
	 * static final int subClass[] = new int[] { Grammar.description5subClassOf,
	 * Grammar.restriction6subClassOf, Grammar.restriction7subClassOf,
	 * Grammar.restriction8subClassOf };
	 * 
	 * static final int equivalentClass[] = new int[] {
	 * Grammar.description5equivalentClass, Grammar.restriction6equivalentClass,
	 * Grammar.restriction7equivalentClass, Grammar.restriction8equivalentClass };
	 * 
	 * static final int classOrDatarangeObject[] = new int[] {
	 * Grammar.description5object, Grammar.restriction6object,
	 * Grammar.restriction7object, Grammar.restriction8object,
	 * Grammar.unnamedDataRange };
	 * 
	 * static final int restrictionsAndDescriptionsAndDatarange[] = new int[] {
	 * Grammar.unnamedDataRange, Grammar.description5disjointWith,
	 * Grammar.description5equivalentClass, Grammar.description5object,
	 * Grammar.description5subClassOf, Grammar.restriction6disjointWith,
	 * Grammar.restriction6equivalentClass, Grammar.restriction6object,
	 * Grammar.restriction6subClassOf, Grammar.restriction7disjointWith,
	 * Grammar.restriction7equivalentClass, Grammar.restriction7object,
	 * Grammar.restriction7subClassOf, Grammar.restriction8disjointWith,
	 * Grammar.restriction8equivalentClass, Grammar.restriction8object,
	 * Grammar.restriction8subClassOf };
	 * 
	 * static final int userPropIDs[] = { Grammar.annotationPropID,
	 * Grammar.dataPropID, Grammar.objectPropID, Grammar.ontologyPropertyID,
	 * Grammar.transitivePropID,
	 *  };
	 * 
	 * static final int individualPropIDs[] = { Grammar.annotationPropID,
	 * Grammar.dataPropID, Grammar.objectPropID, Grammar.transitivePropID, };
	 * 
	 * static final int annoOntoObjectPropIDs[] = { Grammar.annotationPropID,
	 * Grammar.objectPropID, Grammar.ontologyPropertyID,
	 * Grammar.transitivePropID };
	 * 
	 * static final int annoDataPropIDs[] = { Grammar.annotationPropID,
	 * Grammar.dataPropID, };
	 * 
	 * static final int objectOrAnnotationPropIDs[] = {
	 * Grammar.annotationPropID, Grammar.objectPropID, Grammar.transitivePropID,
	 *  };
	 * 
	 * static final int ontologyOrAnnotationPropIDs[] = {
	 * Grammar.annotationPropID, Grammar.ontologyPropertyID,
	 *  };
	 * 
	 * private static int rdClassify(int a[]) { if (Q.subset(a, disjointWith))
	 * return SC.disjointWith; if (Q.subset(a, equivalentClass)) return
	 * SC.equivalentClass; if (Q.subset(a, subClass)) return SC.subClassOf; if
	 * (Q.subset(a, classOrDatarangeObject)) return SC.classOrDatarangeObject;
	 * return SC.UNKNOWN; } if (a.length == 1) { switch (a[0]) { case
	 * Grammar.unnamedIndividual}, "" }, return SC.unnamedIndividual; case
	 * Grammar.unnamedOntology: return SC.unnamedOntology; case
	 * Grammar.allDifferent: return SC.allDifferent; case
	 * Grammar.description5disjointWith: case
	 * Grammar.description5equivalentClass: case Grammar.description5object:
	 * case Grammar.description5subClassOf: return SC.description; case
	 * Grammar.listOfDataLiteral: case Grammar.listOfDescription: case
	 * Grammar.listOfIndividualID: return SC.list; case
	 * Grammar.restriction6disjointWith: case
	 * Grammar.restriction6equivalentClass: case Grammar.restriction6object:
	 * case Grammar.restriction6subClassOf: case
	 * Grammar.restriction7disjointWith: case
	 * Grammar.restriction7equivalentClass: case Grammar.restriction7object:
	 * case Grammar.restriction7subClassOf: case
	 * Grammar.restriction8disjointWith: case
	 * Grammar.restriction8equivalentClass: case Grammar.restriction8object:
	 * case Grammar.restriction8subClassOf: return SC.restriction; case
	 * Grammar.unnamedDataRange: return SC.unnamedDataRange; case
	 * Grammar.annotationPropID: return SC.annotationPropID; case
	 * Grammar.classID: return SC.classID; case Grammar.dataPropID: return
	 * SC.dataPropID; case Grammar.datatypeID: return SC.datatypeID; case
	 * Grammar.individualID: return SC.individualID; case Grammar.objectPropID:
	 * return SC.objectPropID; case Grammar.ontologyID: return SC.ontologyID;
	 * case Grammar.ontologyPropertyID: return SC.ontologyPropertyID; case
	 * Grammar.transitivePropID: return SC.objectPropID; default: throw new
	 * RuntimeException("Impossible"); } } if (isObjectProperty(c)) return
	 * SC.objectPropID; if (Q.subset(a, descriptions)) return SC.description; if
	 * (Q.subset(a, lists)) return SC.list; if (Q.subset(a, restrictions))
	 * return SC.restriction;
	 * 
	 * if (isOWLPropertyID(c)) return SC.owlPropID; if (Q.subset(a,
	 * userPropIDs)) return SC.userPropID;
	 * 
	 * if (Q.subset(a, restrictionsAndDescriptions)) return
	 * SC.descriptionOrRestriction;
	 * 
	 * if (Q.subset(a, descriptionsAndDatarange)) return
	 * SC.descriptionOrDatarange;
	 * 
	 * if (isRestrictionOrDescriptionOrDatarange(a)) return
	 * SC.descriptionOrRestrictionOrDatarange;
	 * 
	 * if (Q.subset(a, unnamedIndividualOrOntology)) return
	 * SC.unnamedIndividualOrOntology;
	 * 
	 * if (a.length == 2 && a[0] == Grammar.classID) { switch (a[1]) { case
	 * Grammar.individualID: return SC.classOrIndividualID; case
	 * Grammar.datatypeID: return SC.classOrDatatypeID; } }
	 * 
	 * 
	 * 
	 *  };
	 */
static Vector doneNames = new Vector();
	static int nameCatSet(int in[], int out[]) {
		if (out == null)
			out = empty;
		
		for (int i=0; i<names.length; i++) {
			int cats[] = (int[])names[i][0];
			if (Q.subset(in,cats) && !Q.intersect(cats,out)) {
				return i;
			}
		}
		Iterator it = doneNames.iterator();
		while (it.hasNext()){
			int a[][] = (int[][])it.next();
			if (Q.subset(in,a[0]) &&
					Q.subset(a[0],in) &&
					Q.subset(out,a[1]) &&
					Q.subset(a[1],out))
				return -1;
		}
		System.err.println(doneNames.size()+" nameCatSet(" +
		CategorySet.catString(in) + ","
		+ CategorySet.catString(out) );
		doneNames.add(new int[][]{in,out});

		return -1;
	}

	static int dbgCnt = 0;

	static boolean debug() {
		return (dbgCnt++ % 50 == 0 && dbgCnt < 2000);
	}

	static void dumpc(String x, int c, int d) {
		System.out.println(x + "(a)" + CategorySet.catString(c));
		if (d != 0)
			System.out.println(x + "(b)" + CategorySet.catString(d));
	}

	private static int badBuiltinRange(int p, int o) {
		switch (p) {
		// classID
		case Grammar.rdfsrange:
		case Grammar.owlsomeValuesFrom:
			return isNotClassID(o) && isNotDatatypeID(o)
					&& o != Grammar.dataRangeID ? BAD_RANGE_CLASS_DATATYPE : 0;

		// description restriction or classID
		case Grammar.owlequivalentClass:
		case Grammar.rdfssubClassOf:
		case Grammar.owldisjointWith:
		case Grammar.owlcomplementOf:
		case Grammar.rdfsdomain:
			return isNotDescription(o) && isNotClassID(o)
					&& isNotRestriction(o) ? BAD_RANGE_GENERAL_CLASS : 0;

		// description or classID
		case Grammar.owlintersectionOf:
		case Grammar.owlunionOf:
			return isNotListOfDesc(o) ? BAD_RANGE_LIST_DESC : 0;
		case Grammar.owloneOf:
			return isNotListOfLit(o) && isNotListOfIndividual(o) ? BAD_RANGE_ONEOF
					: 0;
		case Grammar.owldistinctMembers:
			return isNotListOfIndividual(o) ? BAD_RANGE_LIST_IND : 0;

		// propertyID (obj, trans or data)
		case Grammar.owlonProperty:
		case Grammar.rdfssubPropertyOf:
		case Grammar.owlequivalentProperty:
		case Grammar.owlinverseOf:
			return isNotOWLPropertyID(o) ? BAD_RANGE_PROP : 0;

		// list node
		case Grammar.rdffirst:
			return isNotIndividualID(o) && isNotDescription(o)
					&& isNotClassID(o) && isNotRestriction(o) ? BAD_RANGE_FIRST
					: 0;

		case Grammar.rdfrest:
			return isNotListNode(o) && o != Grammar.rdfnil ? BAD_RANGE_LIST : 0;
		// indivID
		case Grammar.owldifferentFrom:
		case Grammar.owlsameAs:
			return isNotIndividualID(o) ? BAD_RANGE_INDIVIDUAL : 0;

		case Grammar.owlmaxCardinality:
			return o != Grammar.liteInteger && o != Grammar.dlInteger ? BAD_RANGE_CARDINALITY
					: 0;
		// restriction
		case Grammar.owlhasValue:
			return isNotIndividualID(o) && isNotLiteral(o) ? BAD_RANGE_HAS_VALUE
					: 0;
		case Grammar.rdftype:
			switch (o) {
			case Grammar.rdfProperty:
			case Grammar.rdfsClass:
			case Grammar.rdfsDatatype:
			case Grammar.rdfList:
			case Grammar.owlAllDifferent:
			case Grammar.owlAnnotationProperty:
			case Grammar.owlClass:
			case Grammar.owlDataRange:
			case Grammar.owlDatatypeProperty:
			case Grammar.owlDeprecatedClass:
			case Grammar.owlFunctionalProperty:
			case Grammar.owlInverseFunctionalProperty:
			case Grammar.owlObjectProperty:
			case Grammar.owlOntology:
			case Grammar.owlOntologyProperty:
			case Grammar.owlRestriction:
			case Grammar.owlSymmetricProperty:
			case Grammar.owlTransitiveProperty:
				return 0;
			}
			return isNotDescription(o) && isNotClassID(o)
					&& isNotRestriction(o) ? BAD_RANGE_TYPE : 0;

		default:
			if (isOntologyProp(p))
				return isOntologyID(o) ? 0 : BAD_RANGE_ONT_PROP;
			return 0;
		}
	}

	private static boolean isNotListOfIndividual(int o) {
		return !Q.member(Grammar.listOfIndividualID, CategorySet.getSet(o));
	}

	private static boolean isNotListOfLit(int o) {
		return !Q.member(Grammar.listOfDataLiteral, CategorySet.getSet(o));

	}

	private static boolean isNotListOfDesc(int o) {
		return !Q.member(Grammar.listOfDescription, CategorySet.getSet(o));
	}

	private static boolean isNotDatatypeID(int o) {

		return !Q.member(Grammar.datatypeID, CategorySet.getSet(o));
	}

	private static int badBuiltinDomain(int p, int s) {
		switch (p) {
		// description restriction or classID
		case Grammar.owlequivalentClass:
		case Grammar.rdfssubClassOf:
		case Grammar.owldisjointWith:
			return isNotDescription(s) && isNotClassID(s)
					&& isNotRestriction(s) ? BAD_DOMAIN_GENERAL_CLASS : 0;

		// description or classID
		case Grammar.owlcomplementOf:
		case Grammar.owlintersectionOf:
		case Grammar.owlunionOf:
		case Grammar.owloneOf:
			return isNotDescription(s) && isNotClassID(s) ? BAD_DOMAIN_CLASS_DESC
					: 0;

		case Grammar.rdftype:
			return isUserID[s] || isBlank[s] ? 0 : BAD_DOMAIN_TYPE;
		// propertyID (obj, trans or data)
		case Grammar.rdfssubPropertyOf:
		case Grammar.owlequivalentProperty:
		case Grammar.owlinverseOf:
		case Grammar.rdfsrange:
		case Grammar.rdfsdomain:
			return isNotOWLPropertyID(s) ? BAD_DOMAIN_PROP : 0;

		// list node
		case Grammar.rdffirst:
		case Grammar.rdfrest:
			return isNotListNode(s) ? BAD_DOMAIN_LIST : 0;
		// indivID
		case Grammar.owldifferentFrom:
		case Grammar.owlsameAs:
			return isNotIndividualID(s) ? BAD_DOMAIN_INDIVIDUAL : 0;

		// alldifferent
		case Grammar.owldistinctMembers:
			return isNotAllDifferent(s) ? BAD_DOMAIN_ALL_DIFF : 0;

		// restriction
		case Grammar.owlhasValue:
		case Grammar.owlmaxCardinality:
		case Grammar.owlonProperty:
		case Grammar.owlsomeValuesFrom:
			return isNotRestriction(s) ? BAD_DOMAIN_REST : 0;
		default:
			if (isOntologyProp(p))
				return isBlank[s] || isOntologyID(s) ? 0 : BAD_DOMAIN_ONT_PROP;
			return 0;
		}
	}

	static int ont2 = CategorySet.find(new int[] { Grammar.notype,
			Grammar.ontologyID }, false);

	private static boolean isOntologyID(int p) {
		return p == Grammar.ontologyID || p == ont2;
	}

	private static boolean isNotRestriction(int s) {
		return look.meet(s, Grammar.restrictions) == Failure;
	}

	private static boolean isNotAllDifferent(int s) {
		return !Q.member(Grammar.allDifferent, CategorySet.getSet(s));
	}

	private static boolean isNotIndividualID(int s) {
		return !Q.member(Grammar.individualID, CategorySet.getSet(s));
	}

	private static boolean isNotListNode(int s) {
		return look.meet(s, Grammar.lists) == Failure;
	}

	private static boolean isNotOWLPropertyID(int s) {
		return !Q.intersect(owlProps, CategorySet.getSet(s));
	}

	private static boolean isOWLPropertyID(int s) {
		return Q.subset(CategorySet.getSet(s), owlProps2);
	}

	private static boolean isNotDescription(int s) {
		return look.meet(s, Grammar.descriptions) == Failure;
	}

	private static boolean isNotClassID(int s) {
		return !Q.member(Grammar.classID, CategorySet.getSet(s));
	}

	private static int singleTripleError(int sx, int px, int ox) {
		if (isBlank[px])
			return BLANK_PROP;
		if (isLiteral(px))
			return LITERAL_PROP;
		if (isLiteral(sx))
			return LITERAL_SUBJ;

		if (sx == Grammar.badID || px == Grammar.badID)
			return BADID_USE;
		if (ox == Grammar.badID)
			return BADID_USE;

		if (isBuiltin[sx] && !look.canBeSubj(sx))
			return BUILTIN_NON_SUBJ;
		if (//isBuiltin[px]&&
		!look.canBeProp(px))
			return BUILTIN_NON_PRED;
		if (isClassID(px))
			return CLASS_AS_PROP;
		if (px == Grammar.dataAnnotationPropID && isNotLiteral(ox))
			return DATA_ANN_PROP_BAD_OBJ;

		int x = badBuiltinDomain(px, sx);
		if (x != 0)
			return x;
		x = badBuiltinRange(px, ox);
		if (x != 0)
			return x;
		if (px == Grammar.rdftype) {

			if (maybeBuiltinID(sx))
				return TYPE_FOR_BUILTIN;
			switch (ox) {
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
			case Grammar.owlOntology:
				if (isClassOnly(sx))
					return TYPE_OF_CLASS_ONLY_ID;
			// fall through
			case Grammar.owlDeprecatedClass:
				if (!isUserID[sx] && ox != Grammar.owlOntology)
					return TYPE_NEEDS_ID;

			case Grammar.rdfList:
			case Grammar.owlAllDifferent:
			case Grammar.owlDataRange:
			case Grammar.owlRestriction:
				if (!isBlank[sx])
					return TYPE_NEEDS_BLANK;
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
		}
		if (debug()) {
			dumpc("DC-subj", sx, 0);
			dumpc("DC-prop", px, 0);
			dumpc("DC-obj", ox, 0);
		}
		// TODO not reached.
		// TODO fix me
		if (isObjectProperty(px) && isLiteral(ox))
			return OBJ_PROP_AND_LIT;
		if (isDataProp(px) && isNotLiteral(ox))
			return DATA_PROP_AND_NONLIT;

		return SINGLE_TRIPLE;
	}

	private static boolean maybeBuiltinID(int sx) {
		boolean builtin;
		switch (sx) {
		case Grammar.annotationPropID:
		case Grammar.datatypeID:
		case Grammar.dataRangeID:
		case Grammar.dataAnnotationPropID:
		case Grammar.classID:
		case Grammar.ontologyPropertyID:
			builtin = true;
			break;
		default:
			builtin = false;
			break;
		}
		return builtin;
	}

	private static boolean isClassOnly(int sx) {
		return look.meet(sx, Grammar.classOnly) == sx;
	}

	private static boolean isPropertyOnly(int sx) {
		return look.meet(sx, Grammar.propertyOnly) == sx;
	}

	private static int[] nonPseudoCats(int c) {
		int cats[] = CategorySet.getSet(c);
		int i;
		for (i = 0; i < cats.length; i++) {
			if (!Grammar.isPseudoCategory(cats[i]))
				break;
		}
		if (i == 0)
			return cats;
		else {
			int rslt[] = new int[cats.length - i];
			System.arraycopy(cats, i, rslt, 0, rslt.length);
			return rslt;
		}
	}

	static class SC {
		static final int UNKNOWN = -1;

		static final int unnamedIndividual = 0;

		static final int unnamedOntology = 1;

		static final int allDifferent = 2;

		static final int description = 3;

		static final int list = 4;

		static final int restriction = 5;

		static final int unnamedDataRange = 6;

		static final int descriptionOrRestriction = 7;

		static final int descriptionOrRestrictionOrDatarange = 8;

		static final int unnamedIndividualOrOntology = 9;

		static final int descriptionOrDatarange = 10;

		static final int annotationPropID = 0;

		static final int classID = 1;

		static final int dataPropID = 2;

		static final int datatypeID = 3;

		static final int individualID = 4;

		static final int objectPropID = 5;

		static final int ontologyID = 6;

		static final int ontologyPropertyID = 7;

		static final int owlPropID = 8;

		static final int userPropID = 9;

		static final int classOrIndividualID = 10;

		static final int classOrDatatypeID = 11;

		static final int subClassOf = 0;

		static final int disjointWith = 1;

		static final int equivalentClass = 2;

		static final int classOrDatarangeObject = 3;
	}

	private static boolean isRestrictionOrDescriptionOrDatarange(int a[]) {
		return Q.subset(a, restrictionsAndDescriptionsAndDatarange);
	}

	private static int rdClassify(int a[]) {
		if (Q.subset(a, disjointWith))
			return SC.disjointWith;
		if (Q.subset(a, equivalentClass))
			return SC.equivalentClass;
		if (Q.subset(a, subClass))
			return SC.subClassOf;
		if (Q.subset(a, classOrDatarangeObject))
			return SC.classOrDatarangeObject;
		return SC.UNKNOWN;
	}

	private static int simpleClassify(int c, int a[]) {
		if (a.length == 1) {
			switch (a[0]) {
			case Grammar.unnamedIndividual:
				return SC.unnamedIndividual;
			case Grammar.unnamedOntology:
				return SC.unnamedOntology;
			case Grammar.allDifferent:
				return SC.allDifferent;
			case Grammar.description5disjointWith:
			case Grammar.description5equivalentClass:
			case Grammar.description5object:
			case Grammar.description5subClassOf:
				return SC.description;
			case Grammar.listOfDataLiteral:
			case Grammar.listOfDescription:
			case Grammar.listOfIndividualID:
				return SC.list;
			case Grammar.restriction6disjointWith:
			case Grammar.restriction6equivalentClass:
			case Grammar.restriction6object:
			case Grammar.restriction6subClassOf:
			case Grammar.restriction7disjointWith:
			case Grammar.restriction7equivalentClass:
			case Grammar.restriction7object:
			case Grammar.restriction7subClassOf:
			case Grammar.restriction8disjointWith:
			case Grammar.restriction8equivalentClass:
			case Grammar.restriction8object:
			case Grammar.restriction8subClassOf:
				return SC.restriction;
			case Grammar.unnamedDataRange:
				return SC.unnamedDataRange;
			case Grammar.annotationPropID:
				return SC.annotationPropID;
			case Grammar.classID:
				return SC.classID;
			case Grammar.dataPropID:
				return SC.dataPropID;
			case Grammar.datatypeID:
				return SC.datatypeID;
			case Grammar.individualID:
				return SC.individualID;
			case Grammar.objectPropID:
				return SC.objectPropID;
			case Grammar.ontologyID:
				return SC.ontologyID;
			case Grammar.ontologyPropertyID:
				return SC.ontologyPropertyID;
			case Grammar.transitivePropID:
				return SC.objectPropID;
			default:
				throw new RuntimeException("Impossible");
			}
		}
		if (isObjectProperty(c))
			return SC.objectPropID;
		if (Q.subset(a, descriptions))
			return SC.description;
		if (Q.subset(a, lists))
			return SC.list;
		if (Q.subset(a, restrictions))
			return SC.restriction;

		if (isOWLPropertyID(c))
			return SC.owlPropID;
		if (Q.subset(a, userPropIDs))
			return SC.userPropID;

		if (Q.subset(a, restrictionsAndDescriptions))
			return SC.descriptionOrRestriction;

		if (Q.subset(a, descriptionsAndDatarange))
			return SC.descriptionOrDatarange;

		if (isRestrictionOrDescriptionOrDatarange(a))
			return SC.descriptionOrRestrictionOrDatarange;

		if (Q.subset(a, unnamedIndividualOrOntology))
			return SC.unnamedIndividualOrOntology;

		if (a.length == 2 && a[0] == Grammar.classID) {
			switch (a[1]) {
			case Grammar.individualID:
				return SC.classOrIndividualID;
			case Grammar.datatypeID:
				return SC.classOrDatatypeID;
			}
		}

		return SC.UNKNOWN;
	}

	static {
		if (Grammar.classID > Grammar.individualID
				|| Grammar.classID > Grammar.datatypeID)
			System.err.println("Compilation assumption failure");
		if (Grammar.listOfDataLiteral > Grammar.listOfIndividualID)
			System.err.println("Compilation assumption failure");
		if (Grammar.listOfDescription > Grammar.listOfIndividualID)
			System.err.println("Compilation assumption failure");
		if (Grammar.dataPropID > Grammar.objectPropID)
			System.err.println("Compilation assumption failure");
	}

	static private boolean maybeSameNonSymmetric(boolean blank, int sc1, int sc2) {
		if (blank)

			switch (sc1) {
			case SC.UNKNOWN:
				return true;
			case SC.descriptionOrRestrictionOrDatarange:
				if (sc2 == SC.unnamedDataRange
						|| sc2 == SC.descriptionOrRestriction)
					return true;
			case SC.descriptionOrRestriction:

				return sc2 == SC.description || sc2 == SC.restriction;
			case SC.descriptionOrDatarange:
				return sc2 == SC.description || sc2 == SC.unnamedDataRange

				|| sc2 == SC.descriptionOrRestriction
						|| sc2 == SC.descriptionOrRestrictionOrDatarange;
			case SC.unnamedIndividualOrOntology:
				return sc2 == SC.unnamedIndividual || sc2 == SC.unnamedOntology;
			}
		else
			switch (sc1) {
			case SC.UNKNOWN:
				return true;
			case SC.userPropID:
				if (sc2 == SC.owlPropID || sc2 == SC.annotationPropID
						|| sc2 == SC.ontologyPropertyID)
					return true;
			case SC.owlPropID:
				return sc2 == SC.dataPropID || sc2 == SC.objectPropID;
			case SC.classOrDatatypeID:
				return sc2 == SC.datatypeID || sc2 == SC.classID;
			case SC.classOrIndividualID:
				return sc2 == SC.individualID || sc2 == SC.classID
						|| sc2 == SC.classOrDatatypeID;
			}
		return false;
	}

	static private boolean maybeSame(boolean blank, int sc1, int sc2) {
		return sc1 == sc2 || maybeSameNonSymmetric(blank, sc1, sc2)
				|| maybeSameNonSymmetric(blank, sc2, sc1);
	}

	private static int userPropertyMiss(int a[], int scB) {
		if (Q.subset(a, annoOntoObjectPropIDs) && scB == SC.dataPropID)
			return DATA_PROP_OR_NON_DATA_PROP;
		if (Q.subset(a, annoDataPropIDs)
				&& (scB == SC.objectPropID || scB == SC.ontologyPropertyID))
			return OBJ_PROP_OR_NON_OBJ_PROP;
		if (Q.subset(a, ontologyOrAnnotationPropIDs)) {
			switch (scB) {
			case SC.dataPropID:
			case SC.objectPropID:
			case SC.owlPropID:
				return OWL_PROP_OR_NON_OWL_PROP;
			}
		}
		return 0;
	}

	private static int catMiss(int oz, int o) {
		if (isBlank[oz] != isBlank[o])
			throw new RuntimeException("Logic error");
		int a[] = nonPseudoCats(oz);
		int b[] = nonPseudoCats(o);

		int given = nameCatSet(a,b);
		int wanted = nameCatSet(b,a);
		
		if (given != -1 && wanted != -1 && given != wanted)
			return DIFFERENT_CATS;
		
		System.err.println("Code not reached.");
		
		int scA = simpleClassify(oz, a);
		int scB = simpleClassify(o, b);
		
		

		if (!maybeSame(isBlank[oz], scA, scB))
			return DIFFERENT_CATS;

		if (isRestrictionOrDescriptionOrDatarange(a)
				&& isRestrictionOrDescriptionOrDatarange(b)) {
			int rdA = rdClassify(a);
			int rdB = rdClassify(b);
			if (rdA != rdB && rdA != SC.UNKNOWN && rdB != SC.UNKNOWN)
				return DIFFERENT_DESCRIPTIONS;

			if (scA == scB && scA == SC.restriction) {
				if ((Q.subset(a, objPropRestrictions) && Q.subset(b,
						dataPropRestrictions))
						|| (Q.subset(b, objPropRestrictions) && Q.subset(a,
								dataPropRestrictions)))
					return OBJ_OR_DATA_RESTRICTION;
				if ((Q.subset(a, transPropRestrictions) && Q.subset(b,
						nonTransPropRestrictions))
						|| (Q.subset(b, transPropRestrictions) && Q.subset(a,
								nonTransPropRestrictions)))
					return BAD_TRANS_RESTRICTION;
			}
		}

		if (scA == SC.userPropID && Q.subset(a, objectOrAnnotationPropIDs)
				&& !Q.subset(b, objectOrAnnotationPropIDs)) {
			return OBJ_OR_ANNOTATION_PROP;
		}
		if (scB == SC.userPropID && Q.subset(b, objectOrAnnotationPropIDs)
				&& !Q.subset(a, objectOrAnnotationPropIDs)) {
			return OBJ_OR_ANNOTATION_PROP;
		}

		if (scA == SC.list && scB == SC.list && a[0] != b[0]) {
			if (a.length == 1 && b.length == 1)
				return DIFFERENT_LISTS;
			if (a.length == 2 && b.length == 1 && a[1] != b[0]) {
				if (a[0] == Grammar.listOfDataLiteral
						&& a[1] == Grammar.listOfIndividualID)
					return DIFFERENT_DATA_OR_IND_LISTS;
				if (a[0] == Grammar.listOfDescription
						&& a[1] == Grammar.listOfIndividualID)
					return DIFFERENT_DATA_OR_NONDATA_LISTS;
			}
			if (b.length == 2 && a.length == 1 && b[1] != a[0]) {
				if (b[0] == Grammar.listOfDataLiteral
						&& b[1] == Grammar.listOfIndividualID)
					return DIFFERENT_DATA_OR_IND_LISTS;
				if (b[0] == Grammar.listOfDescription
						&& b[1] == Grammar.listOfIndividualID)
					return DIFFERENT_DATA_OR_NONDATA_LISTS;
			}
		}

		if (scA == SC.userPropID) {
			int r = userPropertyMiss(a, scB);
			if (r != 0)
				return r;
		}
		if (scB == SC.userPropID) {
			int r = userPropertyMiss(b, scA);
			if (r != 0)
				return r;

		}

		if (a.length == 1 && a[0] == Grammar.transitivePropID
				&& isNonTransProp(b))
			return BAD_TRANS_PROP;
		if (b.length == 1 && b[0] == Grammar.transitivePropID
				&& isNonTransProp(a))
			return BAD_TRANS_PROP;

		if (scA == SC.ontologyPropertyID && Q.subset(b, individualPropIDs))
			return ONTO_PROP_ON_INDIVIDUAL;
		if (scB == SC.ontologyPropertyID && Q.subset(a, individualPropIDs))
			return ONTO_PROP_ON_INDIVIDUAL;
		// not reached.
		/*
		 * if (debug()) { dumpc("Arg1:", oz, o); }
		 */
		throw new RuntimeException("Logic Error");
	}

	static int bad = 0;

	// The number of errors of each type.
	static int eCnt[] = new int[1000];

	// The first three examples found for each sort.
	static int eExamples[][][] = new int[1000][3][6];

	static void allCases(int s, int p, int o) {
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

	public static void foo(String tag,String desc) {
		System.err.println("{ new int[]{ Grammar.description5"+tag+ "},");
	    System.err.println(" \"a class description participating "+desc+"\",");
		System.err.println("},");
		System.err.println("{ new int[]{ Grammar.restriction6"+tag+ ",");
		System.err.println(" Grammar.restriction7"+tag+ ",");
		System.err.println(" Grammar.restriction8"+tag+ ",");
		System.err.println(		"},");
	    System.err.println(" \"a restriction participating "+desc+"\",");
		System.err.println("},");
		System.err.println("{ new int[]{ Grammar.description5"+tag+", Grammar.restriction6"+tag+ ",");
		System.err.println(" Grammar.restriction7"+tag+ ",");
		System.err.println(" Grammar.restriction8"+tag+ ",");
		System.err.println(		"},");
	    System.err.println(" \"a description or a restriction participating "+desc+"\",");
		System.err.println("},");
		
	}
	public static void main(String args[]) {

	/*	
		foo("object","as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith");
		foo("disjointWith","in an owl:disjointWith construct");
		foo("equivalentClass","in an owl:equivalentClass construct");
		foo("subClassOf","in an rdfs:subClassOf construct");
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

					// TODO meet stuff
				}
			}

		}
		System.out.println(bad + " cases considered.");
		dump("Codes:", eCnt);
		dump("Misses", miss);
		dump("MTypes", mTypes);

		for (int i = 0; i < SZ; i++)
			if (diffPreds[i] != 0)
				System.out.println(CategorySet.catString(i) + " "
						+ diffPreds[i]);
		System.out.println("TODO: " + eCnt[GENERIC] + "+" + eCnt[DIFFICULT]);
	}

	static private String fieldName[] = { "subj", "pred", "obj ", "S1  ",
			"P1  ", "01  " };

	static void dump(String s, int a[]) {
		boolean examples = s.startsWith("Codes") && false;
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
					System.out.println(fldsByN[i].getName());
					for (int j = 0; j < 3 && j < a[i]; j++) {
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