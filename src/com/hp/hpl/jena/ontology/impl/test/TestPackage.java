/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestPackage.java,v 1.12 2003-06-16 13:40:14 ian_dickinson Exp $
*/

package com.hp.hpl.jena.ontology.impl.test;


import junit.framework.*;

/**
    Collected test suite for the .ontology.impl package.
    @author  Ian Dickinson
*/

public class TestPackage extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("ontology.impl");
        addTest( "TestResource", TestResource.suite() );
        addTest( "TestAxioms", TestAxioms.suite() );
        addTest( "TestClassExpression", TestClassExpression.suite() );
        addTest( "TestOntDocumentManager", TestOntDocumentManager.suite() );
        addTest( "TestOntology", TestOntology.suite() );
        addTest( "TestProperty", TestProperty.suite() );
        addTest( "TestListSyntaxCategories", TestListSyntaxCategories.suite() );
        addTest( "TestCreate", TestCreate.suite() );
        addTest( "TestIndividual", TestIndividual.suite() );
        addTest( "TestAllDifferent", TestAllDifferent.suite() );
        addTest( new TestSuite( TestOntReasoning.class  ) );
    }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}

/*
    (c) Copyright Hewlett-Packard Company 2002
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