/*
 *  (c)     Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: MoreTests.java,v 1.2 2007-06-04 18:39:12 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.iri.test;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

/**
 * @author jjc
 *  
 */
public class MoreTests extends TestCase {
	
	static public Test suite() {
		TestSuite suite = new TestSuite("Additional IRI Tests");

		
		suite.addTest(new MoreTests("testRelativizeFrag1"));
		suite.addTest(new MoreTests("testRelativizeFrag2"));
		
		return suite;
	}

	public MoreTests(String s) {
		super(s);
	}

	
	public void testRelativizeFrag1() {
		IRIFactory f = IRIFactory.jenaImplementation();
		IRI base = f.create("http://example.org/somefolder/mybase.rdf");
		IRI frag = f.create("http://example.org#foo");
		IRI rel = base.relativize(frag);
		assertEquals(frag,rel);
//		System.err.println(rel.toString());
		IRI back = base.resolve(rel);
		assertEquals(frag,back);
	}

	public void testRelativizeFrag2() {
		IRIFactory f = IRIFactory.jenaImplementation();
		IRI base = f.create("http://example.org/somefolder/mybase.rdf");
		IRI frag = f.create("http://example.org/#foo");
		IRI rel = base.relativize(frag);
		assertEquals("/#foo",rel.toString());
		IRI back = base.resolve(rel);
		assertEquals(frag,back);
	}
}

/*
 * (c) Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */