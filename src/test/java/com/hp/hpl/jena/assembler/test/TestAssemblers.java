/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.Assembler;
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
        result.addTestSuite( TestAssemblerGroupTracing.class );
        result.addTestSuite( TestReasonerFactoryAssembler.class );
        result.addTestSuite( TestContentAssembler.class );
        result.addTestSuite( TestModelContent.class );
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

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { throw new BrokenException( "TestAssemblers does not need this method" ); }
    }
