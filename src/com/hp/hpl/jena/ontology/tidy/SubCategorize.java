/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SubCategorize.java,v 1.3 2003-04-17 20:16:24 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

import java.util.*;

/**
 * This file is a front-end onto the Grammar.java file.
 * Accesses to the data tables in Grammar.java
 * are facilitated.
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class SubCategorize {
	/* In Grammar.java
	static final int propertyOnly = saveSet(new int[]{});
	
	static final int classOnly = saveSet(new int[]{});
	
	static final int blank = saveSet(new int[]{});
	
	static final int userID = saveSet(new int[]{});
	*/
	/**
	 * This triple should be recorded in the first slot
	 * of the subject which is a one-slot structured node.
	 */
	static final int FirstOfOne = Grammar.FirstOfOne;
	/**
	 * This triple should be recorded in the first slot
	 * of the subject which is a two-slot structured node.
	 */
	static final int FirstOfTwo = Grammar.FirstOfTwo;
	/**
	* This triple should be recorded in the second slot
	* of the subject which is a two-slot structured node.
	*/
	static final int SecondOfTwo = Grammar.SecondOfTwo;

	static private final int DL = Grammar.DL;
	
	static private final int ObjectAction = Grammar.ObjectAction;

	static final long FAILURE = -1;

	static final private int notType[] =
		{
			Grammar.rdfProperty,
			Grammar.rdfsClass,
			Grammar.owlDeprecatedProperty,
			Grammar.owlFunctionalProperty,
			Grammar.owlDeprecatedClass };
	static private int ActionMask = (1 << Grammar.ActionShift) - 1;
	/**
	 *  Amount to shift.
	 *  16 bits for s,p,o and 15bits for action.
	 */
	static private final int W = 16;
	static private final int M = (1<<W)-1;
	static private boolean COMPARATIVE(int prop) {
		return prop == Grammar.rdfssubClassOf
			|| prop == Grammar.owldisjointWith
			|| prop == Grammar.owlequivalentClass;
	}
	static private boolean SPECIALSYM(int i) {
		return i < 5;
		// i in { orphan, notype, cycle, cyclicRest, cyclicFirst } 
	}

	static boolean pseudotriple(int subj, int prop, int obj) {
		switch ((SPECIALSYM(subj) ? 1 : 0)
			+ (SPECIALSYM(prop) ? 2 : 0)
			+ (SPECIALSYM(obj) ? 4 : 0)) {
			case 0 :
				return false;
			case 1 :
				if (subj == Grammar.orphan)
					return true;
				if (subj == Grammar.notype) {
					if (prop != Grammar.rdftype)
						return false;
					for (int i = 0; i < notType.length; i++)
						if (obj == notType[i])
							return true;
					return false;
				}


                if ( (
				prop == Grammar.rdftype && obj == Grammar.owlOntology)
						
				// Block restrictions
								|| prop == Grammar.owlcardinality
								|| prop == Grammar.owlminCardinality
								|| prop == Grammar.owlmaxCardinality
								|| prop == Grammar.owlhasValue
								|| prop == Grammar.owlallValuesFrom
								|| prop == Grammar.owlsomeValuesFrom
								// Block descriptions
								|| prop == Grammar.owlunionOf
								|| prop == Grammar.owlintersectionOf
								|| prop == Grammar.owlcomplementOf
								|| prop == Grammar.owloneOf
								// Block alldifferent
								|| prop == Grammar.owldistinctMembers
								// block comparison of restrictions
								|| COMPARATIVE(prop) )
						return false;
				
                

				if (subj == Grammar.cyclicRest)
					return prop != Grammar.rdfrest;

				if (subj == Grammar.cyclicFirst)
					return prop != Grammar.rdffirst;

				if (subj == Grammar.cyclic) {
					if (prop == Grammar.rdftype && obj == Grammar.owlOntology)
						return false;
					return
					// Block list nodes - handled specially
					prop != Grammar.rdfrest && prop != Grammar.rdffirst;
				}
				throw new SyntaxException("Logic error - should not happen.");
			case 2 :
				return prop == Grammar.notype;
			case 4 :
				return obj == Grammar.notype;
			case 3 :
			case 6 :
			case 7 :
				return false;
			case 5 :
				if (subj == Grammar.notype || subj == Grammar.orphan)
					return false;
				if (obj == Grammar.notype || obj == Grammar.orphan)
					return false;
				if (COMPARATIVE(prop))
					return false;
				if (obj == Grammar.cyclic)
					return true;
				return true;
		}
		throw new SyntaxException("Logic error - unhandled case in switch.");
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
		int i, j, k;
		boolean bad = true;
		boolean dl = true;
		boolean objectAction = true;
		int structuredAction = -1;
		for (i = 0; i < s.length; i++)
			for (j = 0; j < p.length; j++)
				for (k = 0; k < o.length; k++)
					// if ( !(oks[i]&&okp[j]&&oko[k]) ) - action semantics needs
					// to check even when we don't really need it.
					{
					int w = Grammar.CategoryShift;
					int triple =
						((((s[i] << w) | p[j]) << w) | o[k])
							<< Grammar.ActionShift;
					int ix = Arrays.binarySearch(Grammar.triples, triple);
					if (ix < 0) {
						if ( -ix-1 == Grammar.triples.length )
						   continue;
						if ((Grammar.triples[-ix - 1] & (~ActionMask))
							== triple) {
							int action = Grammar.triples[-ix - 1] & ActionMask;
							dl = dl && ( (action & DL) == DL );
							objectAction = objectAction 
							  && ( (action & ObjectAction) == ObjectAction);
						    int sAction = action & ~(DL|ObjectAction);
						    if ( structuredAction == -1)
						       structuredAction = sAction;
						    else if ( structuredAction != sAction )
						       structuredAction = 0;
						} else 
							continue;
					} else {
						dl = false;
						structuredAction = 0;
						objectAction = false;
					}

					oks[i] = okp[j] = oko[k] = true;
					bad = false;
				}
		if (bad) {
			return FAILURE;
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
		int action = (dl?DL:0)|(objectAction?ObjectAction:0)|
		              structuredAction;
		return (((long) action) << (3 * W))
			| (((long) s2) << (2 * W))
			| (((long) p2) << (1 * W))
			| (((long) o2) << (0 * W));
	}

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
	/**
	 * 
	 * @param refinement The result of {@link #refineTriple(int,int,int)}
	 * @param subj The old subcategory for the subject.
	 * @return The new subcategory for the subject.
	 */
	static int subject(long refinement, int subj) {
		return  (int)(refinement>>(2*W))&M;
	}
	/**
	 * 
	 * @param refinement The result of {@link #refineTriple(int,int,int)}
	 * @param prop The old subcategory for the property.
	 * @return The new subcategory for the property.
	 */
	static int prop(long refinement, int prop) {
		return  (int)(refinement>>(1*W))&M;
	}
	/**
	 * 
	 * @param refinement The result of {@link #refineTriple(int,int,int)}
	 * @param obj The old subcategory for the object.
	 * @return The new subcategory for the object.
	 */
	static int object(long refinement, int obj) {
		return (int)(refinement>>(0*W))&M;
	}
	/**
	 * 
	 * @param refinement The result of {@link #refineTriple(int,int,int)}
	 * @return An integer reflecting an action needed in response to this triple.
	 */
	static int action(long refinement) {
		return (int)(refinement>>(3*W))& ~(DL|ObjectAction);
	}
	/**
	* 
	* @param refinement The result of {@link #refineTriple(int,int,int)}
	* @return True if this triple is <em>the</em> triple for the blank node object.
	*/
   static boolean tripleForObject(long refinement) {
	   return ((refinement>>(3*W))&ObjectAction) == ObjectAction;
   }
	/**
	 *@param refinement The result of {@link #refineTriple(int,int,int)}
	 * @return Is this triple in DL?.
	 */
	static boolean dl(long refinement) {
		return ((refinement>>(3*W))&DL) == DL;
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