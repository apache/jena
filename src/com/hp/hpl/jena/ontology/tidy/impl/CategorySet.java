/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: CategorySet.java,v 1.8 2004-12-06 13:50:14 andy_seaborne Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;

import java.util.*;
import java.io.Serializable;
import com.hp.hpl.jena.shared.*;
/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class CategorySet implements Comparable, Serializable, Constants {
	//com.hp.hpl.jena.ontology.tidy.CategorySet:
	  static final long serialVersionUID = -1280155302467590202L;

    static private final int cycles[] = new int[]{
    //	Grammar.cyclic,
    	Grammar.cyclicRest,
		  Grammar.cyclicFirst
    };
    static private final int untyped[] = new int[]{
		Grammar.notype,
		Grammar.badRestriction
    };

	static final private int orphanTypes[] = new int[]{
   // 	Grammar.owlOntologyProperty,
		Grammar.rdfList
	};	
	
	static private final SortedSet sorted = new TreeSet();
	public static final Vector unsorted = new Vector();
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
	public synchronized static int find(int s[], boolean isSorted) {
		if (s.length == 0)
		  return Failure;
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
		if ( closed )
		  return Failure;
		cs.id = unsorted.size();
		cs.init();
		return cs.id;
	}
	static final int flags[] = new int[1000];
	static final int CYCLIC  = 1;
	static final int UNTYPED = 2;
	static final int ORPHAN  = 4;
	static final int DLORPHAN = 8;
	static final int STRUCT1  = 16;
	static final int STRUCT2  = 32;
  static	boolean closed = false;
	private void init() {
		int flag = 0;
		unsorted.add(this);
		sorted.add(this);
		
		if ( Q.intersect(cycles,cats) )
		   flag |= CYCLIC;
		if (Q.intersect(untyped,cats) )
		   flag |= UNTYPED;
		if (cats[0] == Grammar.orphan
		    && Q.intersect(orphanTypes,cats)) 
		    flag |= ORPHAN;
		if (cats[0] == Grammar.orphan
			&& ( Q.intersect(Grammar.restrictionsX,cats)
			    || Q.intersect(Grammar.descriptionsX,cats) )) 
			flag |= DLORPHAN;
	 if (Q.member(Grammar.allDifferent,cats)
	 || Q.member(Grammar.unnamedDataRange,cats)
	 || Q.intersect(Grammar.descriptionsX,cats))
	   flag |= STRUCT1;
	   if (Q.intersect(Grammar.listsX,cats)
	   || Q.intersect(Grammar.restrictionsX,cats))
		 flag |= STRUCT2;
	   flags[id] = flag;
		   
		   
	}
	public static int[] getSet(int id) {
		return ((CategorySet) unsorted.elementAt(id)).cats;
	}

	/**
	 * @param i
	 * @return
	 */
	public static String catString(int j) {
		int c[] = getSet(j);
		StringBuffer rslt = new StringBuffer("{");
		rslt.append(Grammar.catNames[c[0]]);
		for (int i=1;i<c.length;i++) {
			rslt.append(" ,");
			rslt.append(Grammar.catNames[c[i]]);
		}		
		return rslt + " }";
	}
	/**
	 * 
	 */
	public void restore() {
		if (id < unsorted.size()) {
			if ( compareTo(unsorted.get(id))!=0 )
			throw new BrokenException("Problems during restore of constants.");
		} else if (id==unsorted.size()){
		  init();
		} else {
			throw new BrokenException("Problems during restore.");
		}
		
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
	(c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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