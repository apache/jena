/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Q.java,v 1.6 2005-02-21 12:08:33 andy_seaborne Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;
import java.util.*;
/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
abstract public class Q {
	private Q(){}
	public static final boolean member(int m, int a[]) {
		return Arrays.binarySearch(a, m) >= 0;
	}
	public static final boolean intersect(int a[], int b[]) {
		for (int i = 0; i < a.length; i++)
			if (member(a[i], b))
				return true;
		return false;
	}
	public static final int[] intersection(int a[], int b[]) {
	    int rslt[] = new int[a.length];
	    int ix = 0;
		for (int i = 0; i < a.length; i++)
			if (member(a[i], b))
				rslt[ix++]=a[i];
		if (ix==rslt.length)
		    return rslt;
		int r[] = new int[ix];
		System.arraycopy(rslt,0,r,0,ix);
		return r;
	}
	public static final boolean subset(int a[], int b[]) {
		for (int i = 0; i < a.length; i++)
			if (!member(a[i], b))
				return false;
		return true;
	}
}

/*
	(c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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