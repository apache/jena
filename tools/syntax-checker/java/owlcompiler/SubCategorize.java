/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SubCategorize.java,v 1.8 2005-01-03 08:17:49 jeremy_carroll Exp $
*/
package owlcompiler;

import java.util.*;

import com.hp.hpl.jena.ontology.tidy.impl.CategorySet;
import com.hp.hpl.jena.ontology.tidy.impl.AbsLookup;
import com.hp.hpl.jena.shared.BrokenException;

/**
 * This file is a front-end onto the Grammar.java file.
 * Accesses to the data tables in Grammar.java
 * are facilitated.
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class SubCategorize extends AbsLookup implements Constants  {
	static final int ActionMask = (1<<ActionShift)-1;
	static final private int notType[] =
		{
			Grammar.rdfProperty,
			Grammar.rdfsClass,
			Grammar.owlDeprecatedProperty,
			Grammar.owlFunctionalProperty,
			Grammar.owlDeprecatedClass };

	static int getSubSet(int s[], boolean oks[]) {
		int cnt = 0;
		for (int i = 0; i < oks.length; i++)
			if (oks[i])
				cnt++;
		int s2[] = new int[cnt];
		int j = 0;
		for (int i = 0; i < oks.length; i++)
			if (oks[i])
				s2[j++] = s[i];
		return CategorySet.find(s2, true);
	}

	static private boolean COMPARATIVE(int prop) {
		return prop == Grammar.rdfssubClassOf
			|| prop == Grammar.owldisjointWith
			|| prop == Grammar.owlequivalentClass;
	}
	static private boolean SPECIALSYM(int i) {
		return Grammar.isPseudoCategory(i);
		// i in { orphan, notype, cycle, cyclicRest, cyclicFirst, badRestriction } 
	}
/*- Discussion of cycles:
 * 
 * Possible cycles that are not excluded by other aspects
 * of grammar.
 * 
 * A list forming a cycle
 *   _:b rdf:rest _:b
 * 
 * A class description depending on itself
 *   _:b owl:complementof _:b
 * 
 * or
 *   _:b owl:allValuesFrom _:c
 *   _:c owl:someValueFrom _:b
 * 
 * A class description depending on itself via a list
 *   _:b owl:unionOf _:y
     _:y rdf:rest    _:x
 *   _:x rdf:first   _:b
 * 
 * An unnamed individual cycle
 *  _:a eg:foo _:b
 *  _:b eg:bar _:a
 * 
 * The prop of a triple is not cyclic
 * 
 * The obj of a triple is not cyclic if its subj
 * is not cylic and the prop is not a comparative.
 * 
 * The subj of a triple is not cyclic
 *   if (a) the triple forces it to be unnamedontology
 *         or an all different
 *      (b) the prop is owl:hasValue or owl:{min,max}cardinality
 *      (c) the prop is owl:onProperty
 *          and the obj is a dataPropID
 *      (d) the prop is owl:complementOf, owl:unionOf,
 *                      owl:intersectionOf
 *                      owl:someValuesFrom
 *                      owl:allValuesFrom
 *          and the obj is not cyclic
 * 
 * A blank node is in the pseudoCat cyclicFirst
 * unless it is known that it is not in a cycle
 * (excluding the case where the cycle starts with an rdf:rest edge)
 * 
 * A blank node is in the pseudoCat cyclicRest
 * unless it is known that it is not in a cycle
 * (excluding the case where the cycle starts with an rdf:first edge)
 * 
 */
	static boolean pseudotriple(int subj, int prop, int obj) {
		switch ((SPECIALSYM(subj) ? 1 : 0)
			+ (SPECIALSYM(prop) ? 2 : 0)
			+ (SPECIALSYM(obj) ? 4 : 0)) {
			case 0 :
				return false;
			case 1 :
				if (subj == Grammar.orphan) {
					if (COMPARATIVE(prop))
					  return false;
					if ( isUnnamedOntologyOrAllDifferent(prop, obj))
					   return false;
					return true;
				}
				if (subj == Grammar.notype) {
					if (prop == Grammar.rdfrest)
						return false;
					if (prop == Grammar.rdffirst)
						return false;
					if (prop != Grammar.rdftype)
						return true;
					for (int i = 0; i < notType.length; i++)
						if (obj == notType[i])
							return true;
					return false;
				}

				if (subj == Grammar.badRestriction) {
					if (prop == Grammar.rdftype)
						return obj == Grammar.owlClass;

					if (prop == Grammar.owlonProperty
					//	|| prop == Grammar.owlcardinality
						|| prop == Grammar.owlhasValue
					//	|| prop == Grammar.owlminCardinality
						|| prop == Grammar.owlmaxCardinality
						|| prop == Grammar.owlsomeValuesFrom
						//|| prop == Grammar.owlallValuesFrom
						|| COMPARATIVE(prop))
						return true;
					return false;
				}

				if ((prop == Grammar.rdftype
					&& obj == Grammar.owlOntology) // Block restrictions
			//		|| prop == Grammar.owlcardinality
			//		|| prop == Grammar.owlminCardinality
					|| prop == Grammar.owlmaxCardinality
					|| prop == Grammar.owlhasValue
			//		|| prop == Grammar.owlallValuesFrom
					|| prop == Grammar.owlsomeValuesFrom // Block descriptions
					|| prop == Grammar.owlunionOf
					|| prop == Grammar.owlintersectionOf
					|| prop == Grammar.owlcomplementOf
					|| prop == Grammar.owloneOf // Block alldifferent
					|| prop == Grammar.owldistinctMembers
					// block comparison of restrictions
					|| COMPARATIVE(prop))
					return false;
					
				if (subj == Grammar.cyclicRest && prop == Grammar.rdfrest )
				    return false;

				if (subj == Grammar.cyclicFirst && prop == Grammar.rdffirst)
					return false;
				
				if ( subj==Grammar.cyclicFirst || subj==Grammar.cyclicRest) {
					/*
					*   if (a) the triple forces it to be unnamedontology
					*         or an all different
					*/
					if (isUnnamedOntologyOrAllDifferent(prop, obj))
					  return false; 
					
					
					/*
					*      (b) the prop is owl:hasValue or owl:{min,max}cardinality
					*/
					if ( prop == Grammar.owlhasValue || prop == Grammar.owlmaxCardinality)
					  return false;
					
					
					/*      (c) the prop is owl:onProperty
					*          and the obj is a dataPropID
					*/
					if ( prop == Grammar.owlonProperty && obj==Grammar.dataPropID)
					  return false;
					  
					/*
					*      (d) the prop is owl:complementOf, owl:unionOf,
					*                      owl:intersectionOf
					*                      owl:someValuesFrom
					*                      owl:allValuesFrom
					*          and the obj is not cyclic
					* */
					if ( prop == Grammar.owlcomplementOf
					    || prop == Grammar.owlintersectionOf
					    || prop == Grammar.owlunionOf
					    || prop == Grammar.owlsomeValuesFrom)
					  return false;  // this is dealt with under case 5:
					
					return true;
				}
					
			 
/* OLD
				if (subj == Grammar.cyclicRest)
					return prop != Grammar.rdfrest;

				if (subj == Grammar.cyclicFirst)
					return prop != Grammar.rdffirst;

				if (subj == Grammar.cyclic) {
					if (prop == Grammar.rdftype && obj == Grammar.owlOntology)
						return false;
					if ( prop == Grammar.owldistinctMembers)
					  return false;
					  if ( prop == Grammar.ontologyPropertyID)
						return false;
					return
					// Block list nodes - handled specially
					prop != Grammar.rdfrest && prop != Grammar.rdffirst;
				}
*/
				throw new BrokenException("Logic error - should not happen.");
			case 2 :
				return prop == Grammar.notype;
			case 4 :
				return ( obj == Grammar.notype && prop != Grammar.annotationPropID )
				       || obj == Grammar.badRestriction;
			case 3 :
			case 6 :
			case 7 :
				return false;
			case 5 :
				if (subj == Grammar.notype
					|| subj == Grammar.orphan
					|| subj == Grammar.badRestriction)
					return false;
				if (obj == Grammar.notype
					|| obj == Grammar.orphan
					|| obj == Grammar.badRestriction)
					return false;
				if (COMPARATIVE(prop))
					return false;
			  if ( prop == Grammar.owldistinctMembers)
			    return false;
				if ( prop == Grammar.ontologyPropertyID)
				  return false;
				return true;
		}
		throw new BrokenException("Logic error - unhandled case in switch.");
	}

	private static boolean isUnnamedOntologyOrAllDifferent(int prop, int obj) {
		return (prop==Grammar.rdftype
		    && (obj == Grammar.owlOntology
		    || obj == Grammar.owlAllDifferent))
		    || prop == Grammar.owldistinctMembers
		  || prop == Grammar.ontologyPropertyID;
	}

	/**
	 * This method should be called for every triple
	 * in the graph. The return value then needs to be
	 * used in further calls to identify the actions to
	 * be taken.
	 * @param subj The subcategory of subj
	 * @param pred The subcategory of pred
	 * @param obj The subcategory of obj
	 * @return A <code>refinement</code> for use in further calls.
	 */
	static long refineTriple(int subj, int pred, int obj) {
		int s[] = CategorySet.getSet(subj);
		int p[] = CategorySet.getSet(pred);
		int o[] = CategorySet.getSet(obj);
		boolean oks[] = new boolean[s.length];
		boolean okp[] = new boolean[p.length];
		boolean oko[] = new boolean[o.length];
	/*	boolean dbgMe = Arrays.binarySearch(o, Grammar.badRestriction) >= 0;
		boolean dbgMe2 = Arrays.binarySearch(s, Grammar.badRestriction) >= 0;

		if (dbgMe ) {
			for (int zz = 0; zz < p.length; zz++)
					System.err.print(p[zz] + " ");
			System.err.println("XX " + obj);
		}		
		if ( dbgMe2) {
		for (int zz = 0; zz < p.length; zz++)
				System.err.print(p[zz] + " ");
		System.err.println("XX2 " + subj);
	}
	*/
		int i, j, k;
		boolean bad = true;
		boolean dl = true;
		boolean objectAction = true;
		boolean subjectAction = true;
		int structuredAction = -1;
		for (i = 0; i < s.length; i++)
			for (j = 0; j < p.length; j++)
				for (k = 0; k < o.length; k++)
					// if ( !(oks[i]&&okp[j]&&oko[k]) ) - action semantics needs
					// to check even when we don't really need it.
					{
					int w = WW;
					int triple =
						((((s[i] << w) | p[j]) << w) | o[k])
							<< ActionShift;
					int ix = Arrays.binarySearch(Grammar.triples, triple);
					if (ix < 0) {
						if (-ix - 1 == Grammar.triples.length)
							continue;
						if ((Grammar.triples[-ix - 1] & (~ActionMask))
							== triple) {
							int action = Grammar.triples[-ix - 1] & ActionMask;
							dl = dl && ((action & DL) == DL);
							objectAction =
								objectAction
									&& ((action & ObjectAction) == ObjectAction);	
							subjectAction =
							subjectAction
							&& ((action & SubjectAction) == SubjectAction);
							int sAction = action & ~(DL | ObjectAction | SubjectAction);
							if (structuredAction == -1)
								structuredAction = sAction;
							else if (structuredAction != sAction)
								structuredAction = 0;
						} else
							continue;
					} else {
						dl = false;
						structuredAction = 0;
						objectAction = false;
						subjectAction = false;
					}

					oks[i] = okp[j] = oko[k] = true;
					bad = false;
				}
		if (bad) {
			/*
			if (dbgMe)
				System.err.println("Z");
				*/
			return Failure;
		}
		for (i = 0; i < s.length; i++)
			if (oks[i] || SPECIALSYM(s[i]))
				for (j = 0; j < p.length; j++)
					if (okp[j] || SPECIALSYM(p[j]))
						for (k = 0; k < o.length; k++)
							if (oko[k] || SPECIALSYM(o[k])) {
								if (pseudotriple(s[i], p[j], o[k])) {
									oks[i] = okp[j] = oko[k] = true;
								}
							}
		int s2 = getSubSet(s, oks);
		int p2 = getSubSet(p, okp);
		int o2 = getSubSet(o, oko);
		int action =
			(dl ? DL : 0)
				| (objectAction ? ObjectAction : 0)	
		        | (subjectAction ? SubjectAction : 0)
				| structuredAction;
/*
		if (dbgMe && Arrays.binarySearch(CategorySet.getSet(o2), Grammar.badRestriction) < 0) {
			for (int zz = 0; zz < p.length; zz++)
				if (okp[zz])
					System.err.print(p[zz] + " ");
			System.err.println();
		} else if (dbgMe) {
			for (int zz = 0; zz < p.length; zz++)
				if (okp[zz])
					System.err.print(p[zz] + " ");
			System.err.println(" OK " + o2);
		}
		if (dbgMe2 && Arrays.binarySearch(CategorySet.getSet(s2), Grammar.badRestriction) < 0) {
			for (int zz = 0; zz < p.length; zz++)
				if (okp[zz])
					System.err.print(p[zz] + "*");
			System.err.println();
		} else if (dbgMe2) {
			for (int zz = 0; zz < p.length; zz++)
				if (okp[zz])
					System.err.print(p[zz] + " ");
			System.err.println(" OK2 " + s2);
		}
		*/
		return toLong(s2, p2, o2, action);
	}
	static long toLong(int s2, int p2, int o2, int action) {
		return (((long) action) << (3 * W))
			| (((long) s2) << (2 * W))
			| (((long) p2) << (1 * W))
			| (((long) o2) << (0 * W));
	}
	static long toLong(int s2, int p2, int o2) {
		return 
			(((long) s2) << (2 * W))
			| (((long) p2) << (1 * W))
			| (((long) o2) << (0 * W));
	}


  /* Implementation of Lookup */
  /* This is not meant for serious use, but is intended for
   * easy performance comparison between compiled and non-compiled 
   * form.
   */
   
  long lookups[] = new long[2048];
  int ix = 0;
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#done(int)
	 */
	public void done(int key) {
		if ( key != Failure )
		lookups[key] = 0;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#qrefine(int, int, int)
	 */
	public int qrefine(int s, int p, int o) {
		// can infinite loop !!!
		while ( lookups[ix] != 0 ) 
		  if ( ++ix == lookups.length )
		     ix = 0;
		lookups[ix] = refineTriple(s,p,o);
		if ( lookups[ix] == Failure )
		  return Failure;
		return ix;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#subject(int)
	 */
	public int subject(int old, int k) {
		
		return (int)(lookups[k]>>(2*W))&M;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#prop(int)
	 */
	public int prop(int old, int k) {
		return (int)(lookups[k]>>(W))&M;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#object(int)
	 */
	public int object(int old, int k) {
		return (int)(lookups[k])&M;
	}


	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#allActions(int)
	 */
	public byte allActions(int k) {
		return (byte)((lookups[k]>>(3*W))&M);
	}

	static final int[] intersection(int a[], int b[]) {
		int rslt0[] = new int[a.length];
		int k = 0;
		for (int i = 0; i < a.length; i++)
			if (Arrays.binarySearch(b, a[i]) >= 0)
				rslt0[k++] = a[i];
		int rslt1[] = new int[k];
		System.arraycopy(rslt0, 0, rslt1, 0, k);
		return rslt1;
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#meet(int, int)
	 */
	public int meet(int c0, int c1) {
		int cc0[] = CategorySet.getSet(c0);
		int cc1[] = CategorySet.getSet(c1);
		int xx[] = intersection(cc0,cc1);
		boolean allPseudo = true;
		for (int i=0;i<xx.length;i++)
		  allPseudo = allPseudo&&Grammar.isPseudoCategory(xx[i]);
		if ( allPseudo )
		  return Failure;
		return CategorySet.find(xx,true);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#domain(int)
	 */
	public int[] domain(int p) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#range(int)
	 */
	public int[] range(int p) {
		throw new UnsupportedOperationException();
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