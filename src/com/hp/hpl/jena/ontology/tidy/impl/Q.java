/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Q.java,v 1.1 2003-11-28 07:46:59 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;
import com.hp.hpl.jena.rdql.*;
import java.util.*;
/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
abstract class Q {
	private Vector v = new Vector();
	private Query rdql = null;
	private int[] a = null;
/*
	Query asRDQL() {
		if (rdql == null) {
			StringBuffer b = new StringBuffer(600);
			b.append("SELECT ?x ");
			b.append("WHERE ( ?x <");
			b.append(Vocab.category.getURI());
			b.append("> ?y ) AND ( ?y eq -2");
			Iterator it = v.iterator();
			while (it.hasNext()) {

				b.append(" || ?y eq ");
				b.append(it.next().toString());
			}
			b.append(")");
			rdql = new Query(b.toString());
		}
		return rdql;
	}
	*/

	int[] asInt() {
		if (a == null) {
			a = new int[v.size()];
			Iterator it = v.iterator();
			int i = 0;
			while (it.hasNext())
				a[i++] = ((Integer) it.next()).intValue();
		  Arrays.sort(a);
		}
		return a;
	}

	void add(int c) {
		v.add(new Integer(c));
		a = null;
		rdql = null;
	}
	abstract boolean test(int c[]);

	public static final boolean member(int m, int a[]) {
		return Arrays.binarySearch(a, m) >= 0;
	}
	/*
	final boolean subset(int a[], int b[]) {
		for (int i = 0; i < a.length; i++)
			if (!member(a[i], b))
				return false;
		return true;
	}
	*/
	public static final boolean intersect(int a[], int b[]) {
		for (int i = 0; i < a.length; i++)
			if (member(a[i], b))
				return true;
		return false;
	}
	/*
	final boolean anySubSet(int a[][],int b[]) {
		for (int i=0;i<a.length;i++)
		  if (subset(a[i],b))
		    return true;
		return false;
	}
	*/

	void testAdd(int c, int cat[]) {
		if (test(cat))
			add(c);
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