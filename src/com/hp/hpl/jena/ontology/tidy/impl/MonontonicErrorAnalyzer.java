/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: MonontonicErrorAnalyzer.java,v 1.4 2003-12-09 18:57:23 jeremy_carroll Exp $
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
	final static int BADID_USE    = 133;
	final static int BUILTIN_NON_PRED = 134;
	final static int BUILTIN_NON_SUBJ = 135;
	final static int CLASS_AS_PROP    = 136;
	final static int DATA_ANN_PROP_BAD_OBJ = 137;
	final static int BUILTIN_WITH_RANGE_CLASS = 138;
	final static int BUILTIN_WITH_DOMAIN_CLASS = 139;
	final static int BUILTIN_WITH_RANGE_PROPERTY = 140;
	final static int BUILTIN_WITH_DOMAIN_PROPERTY = 141;
	
	static LookupTable look = (LookupTable)LookupTable.get();
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
				switch (i){
					case Grammar.badID:
					case Grammar.dlInteger:
					case Grammar.liteInteger:
					case Grammar.literal:
					case Grammar.userTypedLiteral:
					  break;
					default:
					System.err.println("Builtin: " + Grammar.catNames[i]);
					isBuiltin[i]=true;
				}
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
	/**
	 * @param s
	 * @param p
	 * @param o
	 * @param sz
	 * @param pz
	 * @param oz
	 * @return
	 */
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

	/**
	 * @param s
	 */
	private static boolean isNotOntology(int s) {
		int c[] = CategorySet.getSet(s);
		return !Q.intersect(ontos, c);
	}
	static int ontp2 =
		CategorySet.find(
			new int[] { Grammar.notype, Grammar.ontologyPropertyID },
			false);
	/**
	 * @param p
	 */
	private static boolean isOntologyProp(int p) {
		return p == Grammar.ontologyPropertyID || p == ontp2;
	}

	static int class2 =
		CategorySet.find(
			new int[] { Grammar.notype, Grammar.classID },
			false);
	/**
	 * @param p
	 */
	private static boolean isClassID(int p) {
		return p == Grammar.classID || p == class2;
	}
	static int dp1 = Grammar.dataPropID;
	static int dp2 =
		CategorySet.find(
			new int[] { Grammar.notype, Grammar.dataPropID },
			false);
	/**
	 * @param p
	 */
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

	/**
	 * @param p
	 */
	private static boolean isObjectProperty(int p) {
		return p == op1
			|| p == op2
			|| p == op3
			|| p == op4
			|| p == op5
			|| p == op6;
	}
	/**
	 * @param o
	 */
	private static boolean isLiteral(int o) {
		return o == Grammar.literal
			|| o == Grammar.liteInteger
			|| o == Grammar.dlInteger
			|| o == Grammar.userTypedLiteral;
	}
	/**
	 * @param o
	 */
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
		new int[] { Grammar.transitivePropID, Grammar.objectPropID,
			Grammar.dataPropID };

static {
	Arrays.sort(annOrOnt);
	Arrays.sort(indv);
	Arrays.sort(ontos);
	Arrays.sort(owlProps);
}
/**
 * @param s
 */
private static boolean isNotIndividual(int s) {
	int c[] = CategorySet.getSet(s);
	return !Q.intersect(indv, c);
}
/**
 * @param p
 */
private static boolean isNotAnnOrOntoProp(int p) {
	int c[] = CategorySet.getSet(p);
	return !Q.intersect(annOrOnt, c);
}
static int dbgCnt = 0;
static boolean debug() {
	return (dbgCnt++ % 100 == 0 && dbgCnt < 1000);
}
static void dumpc(String x, int c, int d) {
	System.out.println(x + "(a)" + CategorySet.catString(c));
	if (d!=0)
	  System.out.println(x + "(b)" + CategorySet.catString(d));
}

private static boolean badBuiltinDomain(int p,int s){
	switch (p){
		// classID
		case Grammar.rdfsrange:
		case Grammar.rdfsdomain:
		  return isNotClassID(s);
		
    // description restriction or classID
		case Grammar.owlequivalentClass:
		case Grammar.rdfssubClassOf:
		case Grammar.owldisjointWith:
		return isNotDescription(s) && isNotClassID(s)
		   && isNotRestriction(s);
		
		// description or classID
		case Grammar.owlcomplementOf:
		case Grammar.owlintersectionOf:
		case Grammar.owlunionOf:
		case Grammar.owloneOf:
		  return isNotDescription(s) && isNotClassID(s);
		
		// propertyID (obj, trans or data)
		case Grammar.rdfssubPropertyOf:
		case Grammar.owlequivalentProperty:
		case Grammar.owlinverseOf:
		  return isNotOWLPropertyID(s);
		
    // list node
		case Grammar.rdffirst:
		case Grammar.rdfrest:
		  return isNotListNode(s);
		// indivID
		case Grammar.owldifferentFrom:
		case Grammar.owlsameAs:
		  return isNotIndividualID(s);
		  
		// alldifferent
		case Grammar.owldistinctMembers:
      return isNotAllDifferent(s);
      
		// restriction
		case Grammar.owlhasValue:
		case Grammar.owlmaxCardinality:
		case Grammar.owlonProperty:
		case Grammar.owlsomeValuesFrom:
		  return isNotRestriction(s);
		default:
		  return false;
	}
}
/**
 * @param s
 * @return
 */
private static boolean isNotRestriction(int s) {
	return look.meet(s,Grammar.restrictions)==Failure;
}
/**
 * @param s
 * @return
 */
private static boolean isNotAllDifferent(int s) {
	return !Q.member(Grammar.allDifferent,CategorySet.getSet(s));
}
/**
 * @param s
 * @return
 */
private static boolean isNotIndividualID(int s) {
	return !Q.member(Grammar.individualID,CategorySet.getSet(s));
}
/**
 * @param s
 * @return
 */
private static boolean isNotListNode(int s) {
	return look.meet(s,Grammar.lists)==Failure;
}
/**
 * @param s
 * @return
 */
private static boolean isNotOWLPropertyID(int s) {
	return !Q.intersect(owlProps, CategorySet.getSet(s));
}
/**
 * @param s
 * @return
 */
private static boolean isNotDescription(int s) {
	return look.meet(s,Grammar.descriptions)==Failure;
}
/**
 * @param s
 * @return
 */
private static boolean isNotClassID(int s) {
	return !Q.member(Grammar.classID,CategorySet.getSet(s));
}
/**
 * @param sx
 * @param px
 * @param ox
 * @return
 */
private static int singleTripleError(int sx, int px, int ox) {
	if (isBlank[px])
	  return BLANK_PROP;
	if (isLiteral(px))
	  return LITERAL_PROP;
	if (isLiteral(sx))
	  return LITERAL_SUBJ;

  if (sx==Grammar.badID
     || px ==Grammar.badID)
     return BADID_USE;
  if (ox==Grammar.badID)
     return BADID_USE;
     
  if (isBuiltin[sx]&&!look.canBeSubj(sx))
    return BUILTIN_NON_SUBJ;
  if (isBuiltin[px]&&!look.canBeProp(px))
    return BUILTIN_NON_PRED;
  if (isClassID(px))
     return CLASS_AS_PROP;
  if ( px == Grammar.dataAnnotationPropID && isNotLiteral(ox))
     return DATA_ANN_PROP_BAD_OBJ;
  
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
/**
 * @param oz
 * @param o
 * @return
 */
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
	(c) Copyright Hewlett-Packard Company 2003
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