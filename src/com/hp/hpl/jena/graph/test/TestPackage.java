/*
  (c) Copyright 2002, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPackage.java,v 1.30 2008-02-11 09:32:44 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import junit.framework.*;
import com.hp.hpl.jena.graph.query.test.*;

/**
    Collected test suite for the .graph package.
    @author  jjc + kers
*/

public class TestPackage extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("graph");
        addTest( TestFindLiterals.suite() );
        addTest( TestLiteralLabels.suite() );
        addTest( TestTripleCache.suite() );
        addTest( TestNodeCache.suite() );
        addTest( TestNode.suite() );
        addTest( TestTriple.suite() );
        addTest( TestTripleField.suite() );
        addTest( TestNodeToTriplesMap.suite() );
        addTest( TestSimpleReifierTripleMap.suite() );   
        addTest( TestSimpleReifierFragmentsMap.suite() );   
        addTest( TestReifier.suite() );   
        addTestSuite( TestReifierSearch.class );
        addTest( TestReificationPredicates.suite() );
        addTest( TestTypedLiterals.suite() );
        addTest( TestGraphQueryPackage.suite() );
        addTest( TestFactory.suite() );
        addTest( TestGraph.suite() );
        addTest( TestSimpleGraphMaker.suite() );
        addTest( TestFileGraph.suite() );
        addTest( TestFileGraphMaker.suite() );
        addTest( TestGraphExtract.suite() );
        addTest( TestCapabilities.suite() );
        addTest( TestGraphUtils.suite() );
        addTest( TestGraphPrefixMapping.suite() );       
        addTest( TestGraphMatchWithInference.suite());
        addTestSuite( TestGraphEvents.class );
    }
}

/*
    (c) Copyright 2002, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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