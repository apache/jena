/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: CategorySet.java,v 1.9 2003-09-25 16:01:52 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

import java.util.*;
/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class CategorySet implements Comparable {

    static private final int cycles[] = new int[]{
    	Grammar.cyclic,
    	Grammar.cyclicFirst,
    	Grammar.cyclicRest
    };
    static private final int untyped[] = new int[]{
		Grammar.badRestriction,
		Grammar.notype
    };
	/**
	 * 
	 * @return The ids of all CategorySet which might be cyclic. 
	 */
	static final Q cyclicSets = new Q() {
	   boolean test(int a[]) {
	   	  return intersect(cycles,a);
	   }
	};

	/**
	 * 
	 * @return The ids of all CategorySet which are of illegal 
	 * untyped nodes  in the graph. 
	 */
	static final Q untypedSets= new Q() {
		boolean test(int all[]){
			return intersect(untyped,all);
		}
	};
	/**
	 * 
	 * @return The ids of all CategorySet which may be the subject
	 * or object of disjointWith. 
	 */
	static final Q disjointWithSets= new Q() {
		boolean test(int all[]){
			return all[0] != Grammar.orphan &&
			  intersect(Grammar.disjointWithX,all);
		}
	};

    static final private int orphanTypes[] = new int[]{
    	Grammar.owlOntologyProperty,
    	Grammar.rdfList
    };
	/**
	 * 
	 * @return The ids of all CategorySet which are of illegal orphans
	 *  in the graph. 
	 */
	static final Q orphanSets = new Q() {
		boolean test(int all[]){
			return all[0]==Grammar.orphan
			  && intersect(orphanTypes,all);
		}
	};
	/**
		 * 
		 * @return The ids of all CategorySet which are DL only orphans
		 *  in the graph. 
		 */
	static final Q dlOrphanSets = new Q() {
	boolean test(int all[]){
		return all[0]==Grammar.orphan
		  && ( intersect(Grammar.restrictionsX,all)
		  || intersect(Grammar.descriptionsX,all) );
	} 
};
	/**
	 * The ids of all orphaned unnamed individuals, which are
	 * not known not to be cyclic.
	 * In fact, these are not cyclic.
	 * @return
	 */
	static final Q cyclicOrphanSets= new Q() {
	boolean test(int all[]){
		return all[0]==Grammar.orphan
		  && member(Grammar.unnamedIndividual,all)
		  && intersect(cycles,all);
	} 
};

	/**
	 * @return the ids of all categories for which the node must be structured
	 * with one member.
	 */
	static final Q structuredOne = new Q() {
	boolean test(int all[]){
		return member(Grammar.allDifferent,all)
          || member(Grammar.unnamedDataRange,all)
		  || intersect(Grammar.descriptionsX,all);
	} 
};
	/**
		 * @return the ids of all categories for which the node must be structured
		 * with two members.
		 */
	static final Q structuredTwo= new Q() {
	boolean test(int all[]){
		return  intersect(Grammar.listsX,all)
		  || intersect(Grammar.restrictionsX,all);
	} 
};
   static private Q various[] = new Q[]{
   	  cyclicSets,
   	  untypedSets,
   	  structuredOne,
   	  structuredTwo,
   	  disjointWithSets
   };
   static private Q orphaned[] = new Q[]{
   	orphanSets,
   	dlOrphanSets,
   	cyclicOrphanSets
   };
	
	
	static private final SortedSet sorted = new TreeSet();
	static private final Vector unsorted = new Vector();
	/**
	 * 
	 * @param k A sorted array of integers, each reflecting a category.
	 */
	private CategorySet(int k[]) {
		cats = k;
	}
	private int cats[];
	private int id;
	public int compareTo(Object o) {
		CategorySet c = (CategorySet) o;
		int diff = cats.length - c.cats.length;
		for (int j = 0; diff == 0 && j < cats.length; j++)
			diff = cats[j] - c.cats[j];
		return diff;
	}
	public boolean equals(Object o) {
		return compareTo(o) == 0;
	}
	public int hashCode() {
		int rslt = 0;
		for (int i = 0; i < cats.length; i++) {
			rslt ^= cats[i] << (i % 4);
		}
		return rslt;
	}
	/**
	 * This is intended for use by Grammar.java
	 * and SubCategorize.java
	 * @param s
	 * @param isSorted True if s is known to already be in sort order.
	 * @return
	 */
	synchronized static int find(int s[], boolean isSorted) {
		if (!isSorted)
			Arrays.sort(s);
		CategorySet cs = new CategorySet(s);
		SortedSet tail = sorted.tailSet(cs);
		if ( !tail.isEmpty() ) {
			CategorySet close = (CategorySet) tail.first();
			if (close.equals(cs)) {
			//	System.err.println("Close enough.");
				return close.id;
			}
		}
		cs.id = unsorted.size();
		unsorted.add(cs);
		sorted.add(cs);
		for (int i=0;i<various.length;i++)
		   various[i].testAdd(cs.id,s);
        if ( s[0]==Grammar.orphan )
		for (int i=0;i<orphaned.length;i++)
		   orphaned[i].testAdd(cs.id,s);
		return cs.id;
	}
	static int[] getSet(int id) {
		return ((CategorySet) unsorted.elementAt(id)).cats;
	}
	/*
	 * toString(int)
	 * setName(int,String)
	 * 
	 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
	 *
	 */

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