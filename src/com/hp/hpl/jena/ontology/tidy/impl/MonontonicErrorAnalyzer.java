/*
   (c) Copyright 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: MonontonicErrorAnalyzer.java,v 1.6 2003-12-13 21:10:50 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;
import java.util.*;
/**
 * 
 * This class looks at particular triples and tries to
 * work out what went wrong, giving a specific anaylsis.
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class MonontonicErrorAnalyzer implements Constants {
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
	final static int BAD_DOMAIN_CLASS = 167;
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
	final static int NOT_ANNO_WITH_SUBJ_NON_INDIV = 117;
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
	final static int TYPE_NEEDS_ID = 180;
	final static int TYPE_NEEDS_BLANK = 181;

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
					case Grammar.badID :
					case Grammar.dlInteger :
					case Grammar.liteInteger :
					case Grammar.literal :
					case Grammar.userTypedLiteral :
						break;
					default :
						System.err.println("Builtin: " + Grammar.catNames[i]);
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

	static int getErrorCode(int s, int p, int o, int sx, int px, int ox) {
		int key = look.qrefine(sx, px, ox);
		if (key == Failure) {
			return singleTripleError(sx, px, ox);
		} else {
			int misses = 0;
			int sz = look.subject(sx, key);
			int pz = look.prop(px, key);
			int oz = look.object(ox, key);
			int rslt = 0;
			if (look.meet(sz, s) == Failure) {
				misses |= 1;
				rslt = catMiss(sz, s);
			}
			if (look.meet(pz, p) == Failure) {
				misses |= 2;
				rslt = catMiss(pz, p);
			}
			if (look.meet(oz, o) == Failure) {
				misses |= 4;
				rslt = catMiss(oz, o);
			}
			miss[misses]++;
			if (misses == 0) {
				return difficultCase(s, p, o, sz, pz, oz);
			}
			return rslt;
		}
	}	
	private static int difficultCase(
		int s,
		int p,
		int o,
		int sz,
		int pz,
		int oz) {
		int rslt = DIFFICULT;
		if (isNotAnnOrOntoProp(p) && isNotIndividual(s))
			return NOT_ANNO_ON_NON_INDIV;
		if (isNotAnnOrOntoProp(p) && isUserID[o] && isNotIndividual(o))
			return NOT_ANNO_WITH_SUBJ_NON_INDIV;

		if (isOntologyProp(p) && isNotOntology(s))
			return ONTPROP_BAD_SUBJ;
		if (isOntologyProp(p) && isBlank[o])
			return ONTPROP_BLANK_OBJ;

		if (isOntologyProp(p) && isNotOntology(o))
			return ONTPROP_BAD_OBJ;
		if (isObjectProperty(p) && isNotIndividual(o)) {
			/*
			if (isBlank[o])
			  return OBJPROP_WITH_NON_INDIV_BLANK_OBJ;
			*/
			if (isUserID[o])
				return OBJPROP_WITH_NON_INDIV_ID_OBJ;
			rslt = DIFFICULT;
		}

		if (debug()) {
			dumpc("DC-subj", s, sz);
			dumpc("DC-prop", p, pz);
			dumpc("DC-obj", o, oz);
		}
		return rslt;
	}
	private static boolean isNotOntology(int s) {
		int c[] = CategorySet.getSet(s);
		return !Q.intersect(ontos, c);
	}
	static int ontp2 =
		CategorySet.find(
			new int[] { Grammar.notype, Grammar.ontologyPropertyID },
			false);
	private static boolean isOntologyProp(int p) {
		return p == Grammar.ontologyPropertyID || p == ontp2;
	}
	static int class2 =
		CategorySet.find(new int[] { Grammar.notype, Grammar.classID }, false);
	private static boolean isClassID(int p) {
		return p == Grammar.classID || p == class2;
	}
	static int dp1 = Grammar.dataPropID;
	static int dp2 =
		CategorySet.find(
			new int[] { Grammar.notype, Grammar.dataPropID },
			false);
	private static boolean isDataProp(int p) {
		return p == dp1 || p == dp2;
	}
	static int op1 = Grammar.objectPropID;
	static int op2 = Grammar.transitivePropID;
	static int op3 =
		CategorySet.find(
			new int[] { Grammar.notype, Grammar.objectPropID },
			false);
	static int op4 =
		CategorySet.find(
			new int[] { Grammar.notype, Grammar.transitivePropID },
			false);
	static int op5 =
		CategorySet.find(
			new int[] { Grammar.objectPropID, Grammar.transitivePropID },
			false);
	static int op6 =
		CategorySet.find(
			new int[] {
				Grammar.objectPropID,
				Grammar.notype,
				Grammar.transitivePropID },
			false);
	private static boolean isObjectProperty(int p) {
		return p == op1
			|| p == op2
			|| p == op3
			|| p == op4
			|| p == op5
			|| p == op6;
	}
	private static boolean isLiteral(int o) {
		return o == Grammar.literal
			|| o == Grammar.liteInteger
			|| o == Grammar.dlInteger
			|| o == Grammar.userTypedLiteral;
	}
	private static boolean isNotLiteral(int o) {
		return !isLiteral(o);
	}
	static final int indv[] =
		new int[] { Grammar.unnamedIndividual, Grammar.individualID };
	static final int annOrOnt[] =
		new int[] {
			Grammar.annotationPropID,
			Grammar.dataAnnotationPropID,
			Grammar.ontologyPropertyID };
	static final int ontos[] =
		new int[] { Grammar.ontologyID, Grammar.unnamedOntology };

	static final int owlProps[] =
		new int[] {
			Grammar.transitivePropID,
			Grammar.objectPropID,
			Grammar.dataPropID };

	static {
		Arrays.sort(annOrOnt);
		Arrays.sort(indv);
		Arrays.sort(ontos);
		Arrays.sort(owlProps);
	}
	private static boolean isNotIndividual(int s) {
		int c[] = CategorySet.getSet(s);
		return !Q.intersect(indv, c);
	}
	private static boolean isNotAnnOrOntoProp(int p) {
		int c[] = CategorySet.getSet(p);
		return !Q.intersect(annOrOnt, c);
	}
	static int dbgCnt = 0;
	static boolean debug() {
		return (dbgCnt++ % 5 == 0 && dbgCnt < 100);
	}
	static void dumpc(String x, int c, int d) {
		System.out.println(x + "(a)" + CategorySet.catString(c));
		if (d != 0)
			System.out.println(x + "(b)" + CategorySet.catString(d));
	}
	private static int badBuiltinRange(int p, int o) {
		switch (p) {
			// classID
			case Grammar.rdfsrange :
			case Grammar.owlsomeValuesFrom :
				return isNotClassID(o)
					&& isNotDatatypeID(o)
					&& o != Grammar.dataRangeID ? BAD_RANGE_CLASS_DATATYPE : 0;

				// description restriction or classID
			case Grammar.owlequivalentClass :
			case Grammar.rdfssubClassOf :
			case Grammar.owldisjointWith :
			case Grammar.owlcomplementOf :
			case Grammar.rdfsdomain :
				return isNotDescription(o)
					&& isNotClassID(o)
					&& isNotRestriction(o) ? BAD_RANGE_GENERAL_CLASS : 0;

				// description or classID
			case Grammar.owlintersectionOf :
			case Grammar.owlunionOf :
				return isNotListOfDesc(o) ? BAD_RANGE_LIST_DESC : 0;
			case Grammar.owloneOf :
				return isNotListOfLit(o)
					&& isNotListOfIndividual(o) ? BAD_RANGE_ONEOF : 0;
			case Grammar.owldistinctMembers :
				return isNotListOfIndividual(o) ? BAD_RANGE_LIST_IND : 0;

				// propertyID (obj, trans or data)
			case Grammar.owlonProperty :
			case Grammar.rdfssubPropertyOf :
			case Grammar.owlequivalentProperty :
			case Grammar.owlinverseOf :
				return isNotOWLPropertyID(o) ? BAD_RANGE_PROP : 0;

				// list node
			case Grammar.rdffirst :
				return isNotIndividualID(o)
					&& isNotDescription(o)
					&& isNotClassID(o)
					&& isNotRestriction(o) ? BAD_RANGE_FIRST : 0;

			case Grammar.rdfrest :
				return isNotListNode(o)
					&& o != Grammar.rdfnil ? BAD_RANGE_LIST : 0;
				// indivID
			case Grammar.owldifferentFrom :
			case Grammar.owlsameAs :
				return isNotIndividualID(o) ? BAD_RANGE_INDIVIDUAL : 0;

			case Grammar.owlmaxCardinality :
				return o != Grammar.liteInteger
					&& o != Grammar.dlInteger ? BAD_RANGE_CARDINALITY : 0;
				// restriction
			case Grammar.owlhasValue :
				return isNotIndividualID(o)
					&& isNotLiteral(o) ? BAD_RANGE_HAS_VALUE : 0;
			case Grammar.rdftype :
				switch (o) {
					case Grammar.rdfProperty :
					case Grammar.rdfsClass :
					case Grammar.rdfsDatatype :
					case Grammar.rdfList :
					case Grammar.owlAllDifferent :
					case Grammar.owlAnnotationProperty :
					case Grammar.owlClass :
					case Grammar.owlDataRange :
					case Grammar.owlDatatypeProperty :
					case Grammar.owlDeprecatedClass :
					case Grammar.owlFunctionalProperty :
					case Grammar.owlInverseFunctionalProperty :
					case Grammar.owlObjectProperty :
					case Grammar.owlOntology :
					case Grammar.owlOntologyProperty :
					case Grammar.owlRestriction :
					case Grammar.owlSymmetricProperty :
					case Grammar.owlTransitiveProperty :
						return 0;
				}
				return isNotDescription(o)
					&& isNotClassID(o)
					&& isNotRestriction(o) ? BAD_RANGE_TYPE : 0;

			default :
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
			case Grammar.owlequivalentClass :
			case Grammar.rdfssubClassOf :
			case Grammar.owldisjointWith :
				return isNotDescription(s)
					&& isNotClassID(s)
					&& isNotRestriction(s) ? BAD_DOMAIN_GENERAL_CLASS : 0;

				// description or classID
			case Grammar.owlcomplementOf :
			case Grammar.owlintersectionOf :
			case Grammar.owlunionOf :
			case Grammar.owloneOf :
				return isNotDescription(s)
					&& isNotClassID(s) ? BAD_DOMAIN_CLASS_DESC : 0;

			case Grammar.rdftype :
				return isUserID[s] || isBlank[s] ? 0 : BAD_DOMAIN_TYPE;
				// propertyID (obj, trans or data)
			case Grammar.rdfssubPropertyOf :
			case Grammar.owlequivalentProperty :
			case Grammar.owlinverseOf :
			case Grammar.rdfsrange :
			case Grammar.rdfsdomain :
				return isNotOWLPropertyID(s) ? BAD_DOMAIN_PROP : 0;

				// list node
			case Grammar.rdffirst :
			case Grammar.rdfrest :
				return isNotListNode(s) ? BAD_DOMAIN_LIST : 0;
				// indivID
			case Grammar.owldifferentFrom :
			case Grammar.owlsameAs :
				return isNotIndividualID(s) ? BAD_DOMAIN_INDIVIDUAL : 0;

				// alldifferent
			case Grammar.owldistinctMembers :
				return isNotAllDifferent(s) ? BAD_DOMAIN_ALL_DIFF : 0;

				// restriction
			case Grammar.owlhasValue :
			case Grammar.owlmaxCardinality :
			case Grammar.owlonProperty :
			case Grammar.owlsomeValuesFrom :
				return isNotRestriction(s) ? BAD_DOMAIN_REST : 0;
			default :
				if (isOntologyProp(p))
					return isBlank[s]
						|| isOntologyID(s) ? 0 : BAD_DOMAIN_ONT_PROP;
				return 0;
		}
	}

	static int ont2 =
		CategorySet.find(
			new int[] { Grammar.notype, Grammar.ontologyID },
			false);
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
				case Grammar.rdfsDatatype :
				case Grammar.rdfProperty :
				case Grammar.owlAnnotationProperty :
				case Grammar.owlDatatypeProperty :
				case Grammar.owlFunctionalProperty :
				case Grammar.owlInverseFunctionalProperty :
				case Grammar.owlObjectProperty :
				case Grammar.owlOntologyProperty :
				case Grammar.owlSymmetricProperty :
				case Grammar.owlTransitiveProperty :
				case Grammar.owlDeprecatedProperty :
				case Grammar.owlOntology :
					if (isClassOnly(sx))
						return TYPE_OF_CLASS_ONLY_ID;
					// fall through
				case Grammar.owlDeprecatedClass :
					if (!isUserID[sx] && ox != Grammar.owlOntology)
						return TYPE_NEEDS_ID;

				case Grammar.rdfList :
				case Grammar.owlAllDifferent :
				case Grammar.owlDataRange :
				case Grammar.owlRestriction :
					if (!isBlank[sx])
						return TYPE_NEEDS_BLANK;
				default :
					if (isBlank[ox] || isClassOnly(ox)) {
						if (isClassOnly(sx))
							return TYPE_OF_CLASS_ONLY_ID;
						if (isPropertyOnly(sx))
							return TYPE_OF_PROPERTY_ONLY_ID;
					}
				  if (isClassOnly(sx)&&isUserID[ox])
				     return TYPE_OF_CLASS_ONLY_ID;
			}
			if (isPropertyOnly(sx)) {

				switch (ox) {
					case Grammar.rdfsDatatype :
					case Grammar.rdfsClass :
					case Grammar.owlClass :
					case Grammar.owlOntology :
					case Grammar.owlDeprecatedClass :
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
			case Grammar.annotationPropID :
			case Grammar.datatypeID :
			case Grammar.dataRangeID :
			case Grammar.dataAnnotationPropID :
			case Grammar.classID :
			case Grammar.ontologyPropertyID :
				builtin = true;
				break;
			default :
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
	private static int catMiss(int oz, int o) {
		// TODO Auto-generated method stub
		return GENERIC;
	}
	static int bad = 0;
	static int eCnt[] = new int[1000];
	static void allCases(int s, int p, int o) {
		for (int i = 0; i < start[s].length; i++)
			for (int j = 0; j < start[p].length; j++)
				for (int k = 0; k < start[o].length; k++) {
					bad++;
					int e =
						getErrorCode(
							s,
							p,
							o,
							start[s][i],
							start[p][j],
							start[o][k]);
					eCnt[e]++;
				}
	}
	public static void main(String args[]) {

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
	}
	static void dump(String s, int a[]) {
		System.out.println(s);
		for (int i = 0; i < a.length; i++)
			if (a[i] != 0)
				System.out.println(i + "\t" + a[i]);
	}

}

/*
   (c) Copyright 2003 Hewlett-Packard Development Company, LP
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