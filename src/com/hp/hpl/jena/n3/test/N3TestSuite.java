/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.test;

import com.hp.hpl.jena.n3.* ;
import junit.framework.* ;
import java.util.* ;

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3TestSuite.java,v 1.1.1.1 2002-12-19 19:14:47 bwm Exp $
 */
public class N3TestSuite extends TestSuite
{
	/* JUnit swingUI needed this */
    static public TestSuite suite() {
        return new N3TestSuite() ;
    }
	
	
	public N3TestSuite()
	{
		super("N3 Parser") ;
		addTest(new N3InternalTests()) ;
		addTest(new N3ExternalTests()) ;
		addTest(new N3JenaReaderTests()) ;
		addTest(new N3JenaWriterTests()) ;
	}
	
	
	public static void main(String[] args)
	{
		boolean verboseTests = false ;
		N3JenaReaderTests.VERBOSE = verboseTests ;
		N3ExternalTests.VERBOSE = verboseTests ;
		
		junit.textui.TestRunner.run(new N3TestSuite()) ;
	}
}

/*
 *  (c) Copyright Hewlett-Packard Company 2002
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
