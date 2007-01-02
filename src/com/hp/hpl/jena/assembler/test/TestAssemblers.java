/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestAssemblers.java,v 1.3 2007-01-02 11:52:50 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.shared.BrokenException;

import junit.framework.TestSuite;

public class TestAssemblers extends AssemblerTestBase
    {
    public TestAssemblers( String name )
        { super( name ); }
    
    public static TestSuite suite()
        {
        TestSuite result = new TestSuite( TestAssemblers.class );
        result.addTestSuite( TestRuleSet.class );
        result.addTestSuite( TestAssemblerHelp.class );
        result.addTestSuite( TestDefaultModelAssembler.class );
        result.addTestSuite( TestMemoryModelAssembler.class );
        result.addTestSuite( TestAssemblerVocabulary.class );
        result.addTestSuite( TestRuleSetAssembler.class );
        result.addTestSuite( TestInfModelAssembler.class );
        result.addTestSuite( TestAssemblerGroup.class );
        result.addTestSuite( TestReasonerFactoryAssembler.class );
        result.addTestSuite( TestContentAssembler.class );
        result.addTestSuite( TestModelContent.class );
        result.addTestSuite( TestConnectionAssembler.class );
        result.addTestSuite( TestRDBModelAssembler.class );
        result.addTestSuite( TestFileModelAssembler.class );
        result.addTestSuite( TestUnionModelAssembler.class );
        result.addTestSuite( TestPrefixMappingAssembler.class );
        result.addTestSuite( TestBuiltinAssemblerGroup.class );
        result.addTestSuite( TestModelAssembler.class );
        result.addTestSuite( TestModelSourceAssembler.class );
        result.addTestSuite( TestLocationMapperAssembler.class );
        result.addTestSuite( TestFileManagerAssembler.class );
        result.addTestSuite( TestDocumentManagerAssembler.class );
        result.addTest( TestOntModelSpecAssembler.suite() );
        result.addTest( TestOntModelAssembler.suite() );
        return result;
        }
    
    public void testToSilenceJUnit() {}

    protected Class getAssemblerClass()
        { throw new BrokenException( "TestAssemblers does not need this method" ); }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
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