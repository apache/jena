/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestListSubjectsEtc.java,v 1.1 2004-12-03 14:56:42 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestSuite;

/**
     TestListSubjectsEtc - tests for listSubjects, listObjects [and listPredicates, if
     it were to exist]
     TODO make preperly generic, add missing test cases [we're relying, at root,
     on SimpleQueryHandler]
     
     @author kers
 */
public class TestListSubjectsEtc extends ModelTestBase
    {
    public TestListSubjectsEtc( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestListSubjectsEtc.class ); }
    
    public void testListSubjectsNoRemove()
        {
        Model m = modelWithStatements( "a P b; b Q c; c R a" );
        ResIterator it = m.listSubjects();
        it.next();
        try { it.remove(); fail( "listSubjects should not support .remove()" ); }
        catch (UnsupportedOperationException e) { pass(); }
        }
    
    public void testListObjectsNoRemove()
        {
        Model m = modelWithStatements( "a P b; b Q c; c R a" );
        NodeIterator it = m.listObjects();
        it.next();
        try { it.remove(); fail( "listObjects should not support .remove()" ); }
        catch (UnsupportedOperationException e) { pass(); }
        }
    }

/*
	(c) Copyright 2004, Hewlett-Packard Development Company, LP
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