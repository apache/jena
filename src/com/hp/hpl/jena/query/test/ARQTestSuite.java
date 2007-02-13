/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query.test;


import junit.framework.*;

import com.hp.hpl.jena.query.expr.E_Function;
import com.hp.hpl.jena.query.expr.NodeValue;
import com.hp.hpl.jena.query.junit.QueryTestSuiteFactory;
import com.hp.hpl.jena.query.test.suites.*;

/**
 * All the ARQ tests 
 * @author		Andy Seaborne
 * @version 	$Id: ARQTestSuite.java,v 1.42 2007/01/29 09:43:04 andy_seaborne Exp $
 */

public class ARQTestSuite extends TestSuite
{
    public static final String testDirARQ = "testing/ARQ" ;
    
    static public TestSuite suite() {
        return new ARQTestSuite();
    }

	private ARQTestSuite()
	{
        super("ARQ");
        // Tests should be silent.
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;

        // Internal
        addTest(TS.suite() );

        // Scripted tests for SPARQL and ARQ.
        addTest(QueryTestSuiteFactory.make(testDirARQ+"/manifest-arq.ttl")) ;
        // Syntax beyond SPARQL
        addTest(QueryTestSuiteFactory.make(testDirARQ+"/manifest-ext.ttl")) ;
        
        // ARQ + Lucene
        addTest(TestLARQ.suite()) ;
        
        // The DAWG official tests (some may be duplicated in ARQ test suite
        // but this should be the untouched versions, just changes to
        // the manifests for rdfs:labels).
        addTest(TS_DAWG.suite()) ;
        
        // The RDQL engine ported to ARQ
        addTest(TS_RDQL.suite()) ;
        
        addTest(TestAPI.suite()) ;

    }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
