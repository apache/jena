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

package org.apache.jena.assembler.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    // Convert to JUnit5
    TestMode.class,

    // JUnit3
    TestModelExpansion.class,
    TestImportManager.class,
    TestOntModelAcceptance.class,

//    // Was "TestAssemblers" : 19
     TestRuleSet.class ,
     TestAssemblerHelp.class ,
     TestDefaultModelAssembler.class ,
     TestMemoryModelAssembler.class ,
     TestAssemblerVocabulary.class ,
     TestRuleSetAssembler.class ,
     TestInfModelAssembler.class ,
     TestAssemblerGroup.class ,
     TestAssemblerGroupTracing.class ,
     TestReasonerFactoryAssembler.class ,
     TestContentAssembler.class ,
     TestModelContent.class ,
     TestUnionModelAssembler.class ,
     TestPrefixMappingAssembler.class ,
     TestBuiltinAssemblerGroup.class ,
     TestModelAssembler.class ,
     TestDocumentManagerAssembler.class,
     TestOntModelSpecAssembler.class,
     TestOntModelAssembler.class
})

public class TS3_Assembler {}

//    public static TestSuite suite() {
//        TestSuite result = new TestSuite();
//        result.addTestSuite(TestMode.class);
//        result.addTestSuite(TestModelExpansion.class);
//        result.addTestSuite(TestImportManager.class);
//        result.addTestSuite(TestOntModelAcceptance.class);
//
//        // Was "TestAssemblers" : 19
//        result.addTestSuite( TestRuleSet.class );
//        result.addTestSuite( TestAssemblerHelp.class );
//        result.addTestSuite( TestDefaultModelAssembler.class );
//        result.addTestSuite( TestMemoryModelAssembler.class );
//        result.addTestSuite( TestAssemblerVocabulary.class );
//        result.addTestSuite( TestRuleSetAssembler.class );
//        result.addTestSuite( TestInfModelAssembler.class );
//        result.addTestSuite( TestAssemblerGroup.class );
//        result.addTestSuite( TestAssemblerGroupTracing.class );
//        result.addTestSuite( TestReasonerFactoryAssembler.class );
//        result.addTestSuite( TestContentAssembler.class );
//        result.addTestSuite( TestModelContent.class );
//        result.addTestSuite( TestUnionModelAssembler.class );
//        result.addTestSuite( TestPrefixMappingAssembler.class );
//        result.addTestSuite( TestBuiltinAssemblerGroup.class );
//        result.addTestSuite( TestModelAssembler.class );
//        result.addTestSuite( TestDocumentManagerAssembler.class );
//        result.addTest( TestOntModelSpecAssembler.suite() );
//        result.addTest( TestOntModelAssembler.suite() );
//
//        return result;
//    }
