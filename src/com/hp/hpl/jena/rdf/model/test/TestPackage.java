/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPackage.java,v 1.35 2005-02-17 16:19:07 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import junit.framework.*;

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
        super("Model");
        addTest( "TestModel", TestModelFactory.suite() );
        addTest( "TestModelFactory", TestModelFactory.suite() );
        addTest( "TestSimpleListStatements", TestSimpleListStatements.suite() );
        addTest( "TestModelPolymorphism", TestModelPolymorphism.suite() );
        addTest( "TestSimpleSelector", TestSimpleSelector.suite() );
        addTest( "TestStatements", TestStatements.suite() );
        addTest( "TestRDFNodes", TestRDFNodes.suite() );
        addTest( "TestReifiedStatements", TestReifiedStatements.suite() );
        addTest( "TestIterators", TestIterators.suite() );
        addTest( "TestContains", TestContains.suite() );
        addTest( "TestLiteralImpl", TestLiteralImpl.suite() );
        addTest( "TestResourceImpl", TestResourceImpl.suite() );
        addTest( "TestHiddenStatements", TestHiddenStatements.suite() );
        addTest( "TestNamespace", TestNamespace.suite() );
        addTest( "TestModelBulkUpdate", TestModelBulkUpdate.suite() );
        addTest( "TestConcurrency", TestConcurrency.suite() ) ;
        addTest( "TestModelMakerImpl", TestModelMakerImpl.suite() );
        addTest( "TestModelPrefixMapping", TestModelPrefixMapping.suite() );
        addTest( "TestModelSpec", TestModelSpec.suite() );
        addTest( "TestModelSpecMore", TestModelSpecMore.suite() );
        addTest( "TestModelSpecRevised", TestModelSpecRevised.suite() );
        addTest( TestModelSpecFactory.suite() );
        addTest( TestModelSource.suite() );
        addTest( TestContainers.suite() );
        addTest( "TestStandardModels", TestStandardModels.suite() );
        addTest( "TestQuery", TestQuery.suite() );
        addTest( "TestSelectors", TestSelectors.suite() );
        addTest( "TestModelEvents", TestModelEvents.suite() );
        addTest( "TestReaderEvents", TestReaderEvents.suite() );
        addTest( "TestList", TestList.suite() );
        addTest( "TestAnonID", TestAnonID.suite() );
        addTest( TestListSubjectsEtc.suite() );
        addTest( TestModelExtract.suite() );
        addTest( TestModelRead.suite() );
        }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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