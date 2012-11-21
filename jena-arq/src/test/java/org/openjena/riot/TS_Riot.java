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

package org.openjena.riot;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.lang.* ;
import org.openjena.riot.out.* ;
import org.openjena.riot.process.TestNormalization ;
import org.openjena.riot.tokens.TestTokenForNode ;
import org.openjena.riot.tokens.TestTokenizer ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestWebContent.class
    
    , TestTokenizer.class
    , TestTokenForNode.class
    , TestPrefixMap.class
    , TestIRI.class
    , TestChecker.class
    , TestLang.class
    , TestNodeAllocator.class
    
    , TestTurtleTerms.class
    , TestLangNTriples.class
    , TestLangNQuads.class
    , TestLangTurtle.class
    , TestLangTrig.class
    , TestLangRdfJson.class
    , TestParserFactory.class

    , TestNodeFmt.class
    , TestNodeFmtLib.class
    , TestOutput.class
    , TestOutputNTriples.class
    , TestOutputRDFJSON.class
    , TestLangTag.class
    , TestNormalization.class
})

public class TS_Riot
{
    @BeforeClass public static void beforeClass()
    { 
        BaseTest.setTestLogging() ;
    }

    @AfterClass public static void afterClass()
    { 
        BaseTest.unsetTestLogging() ;
    }
}
