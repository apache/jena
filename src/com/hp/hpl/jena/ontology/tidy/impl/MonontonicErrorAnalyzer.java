/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: MonontonicErrorAnalyzer.java,v 1.1 2003-12-02 06:21:10 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;

/**
 * 
 * This class looks at particular triples and tries to
 * work out what went wrong, giving a specific anaylsis.
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class MonontonicErrorAnalyzer {
	static Lookup look = new LookupTable();
	static final int SZ = CategorySet.unsorted.size();
  static final boolean isClassOnly[] = new boolean[SZ];
  static final boolean isPropertyOnly[] = new boolean[SZ];
  static final boolean isUserID[] = new boolean[SZ];
  static final boolean isBlank[] = new boolean[SZ];
  static final boolean isBuiltin[] = new boolean[SZ];
	static {
		for (int i=7;i<SZ;i++) {
			if (look.meet(i,Grammar.classOnly)==i) {
				isClassOnly[i] = true;
			}
			if (look.meet(i,Grammar.propertyOnly)==i) {
				isPropertyOnly[i] = true;
			}
			if (look.meet(i,Grammar.userID)==i) {
				isUserID[i] = true;
			}
			if (look.meet(i,Grammar.blank)==i) {
				isBlank[i] = true;
			}
		}
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