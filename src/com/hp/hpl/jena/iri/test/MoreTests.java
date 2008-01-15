/*
 *  (c)     Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: MoreTests.java,v 1.3 2008-01-15 10:19:39 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.iri.test;


import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;

/**
 * @author jjc
 *  
 */
public class MoreTests extends TestCase {
	
	static public Test suite() {
		TestSuite suite = new TestSuite("Additional IRI Tests");

		
		suite.addTest(new MoreTests("testRelativizeFrag1"));
		suite.addTest(new MoreTests("testRelativizeFrag2"));
		suite.addTest(new MoreTests("testXPointer"));
		suite.addTest(new MoreTests("testNotIDN"));
		
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
	
	public void testXPointer() {
		IRIFactory f = IRIFactory.jenaImplementation();
		IRI base = f.create("http://example.org/");
		IRI frag = base.resolve("http://eg.com/test.txt#xpointer(/unit[5])");
		Iterator it = frag.violations(false);
		while (it.hasNext()) {
			System.err.println(((Violation)it.next()).getLongMessage());
		}
		
	}
	public void testNotIDN() {
		IRIFactory f = IRIFactory.jenaImplementation();
		IRI base = f.create("http://example.org/");
		IRI frag = base.resolve("outbind://4-00000000C45F478BF9F2A048A7A59DE"+
				"3AE35F7230700D3E3AEE226D20A49A390BCD779EC5D4700"+
				"00003DB3650000D3E3AEE226D20A49A390BCD779EC5D470"+
					"00001182DB0000/www.uconnectevent.org");
		Iterator it = frag.violations(false);
		while (it.hasNext()) {
			System.err.println(((Violation)it.next()).getLongMessage());
		}
		
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