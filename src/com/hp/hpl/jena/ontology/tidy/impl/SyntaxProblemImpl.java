/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SyntaxProblemImpl.java,v 1.1 2003-11-30 21:12:58 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;

import com.hp.hpl.jena.enhanced.EnhNode;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.tidy.SyntaxProblem;

/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class SyntaxProblemImpl extends SyntaxProblem {

	/**
	 * @param shortD
	 * @param en
	 * @param lvl
	 */
	public SyntaxProblemImpl(String shortD, EnhNode en, int lvl) {
		super(shortD, en, lvl);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param shortD
	 * @param n
	 * @param lvl
	 */
	public SyntaxProblemImpl(String shortD, Node n, int lvl) {
		super(shortD, n, lvl);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param shortD
	 * @param g
	 * @param lvl
	 */
	public SyntaxProblemImpl(String shortD, Graph g, int lvl) {
		super(shortD, g, lvl);
		// TODO Auto-generated constructor stub
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