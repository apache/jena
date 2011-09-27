/**
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
import org.openjena.riot.lang.TestIRI ;
import org.openjena.riot.lang.TestLang ;
import org.openjena.riot.lang.TestLangNQuads ;
import org.openjena.riot.lang.TestLangNTriples ;
import org.openjena.riot.lang.TestLangTrig ;
import org.openjena.riot.lang.TestLangTurtle ;
import org.openjena.riot.lang.TestNodeAllocator ;
import org.openjena.riot.lang.TestParserFactory ;
import org.openjena.riot.lang.TestSuiteTrig ;
import org.openjena.riot.lang.TestSuiteTurtle ;
import org.openjena.riot.lang.TestTurtleTerms ;
import org.openjena.riot.out.TestNodeFmt ;
import org.openjena.riot.out.TestNodeFmtLib ;
import org.openjena.riot.out.TestOutput ;
import org.openjena.riot.out.TestOutputNTriples ;
import org.openjena.riot.pipeline.TestNormalization ;
import org.openjena.riot.tokens.TestTokenForNode ;
import org.openjena.riot.tokens.TestTokenizer ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
      TestTokenizer.class
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
    , TestSuiteTurtle.class

    , TestLangTrig.class
    , TestSuiteTrig.class

    , TestParserFactory.class

    , TestNodeFmt.class
    , TestNodeFmtLib.class
    , TestOutput.class
    , TestOutputNTriples.class
    , TestLangTag.class
    , TestNormalization.class
})

public class TS_Riot
{
    @BeforeClass public static void beforeClass()
    { 
        ErrorHandlerFactory.setTestLogging(false) ;
    }

    @AfterClass public static void afterClass()
    { 
        ErrorHandlerFactory.setTestLogging(true) ;
    }

}
