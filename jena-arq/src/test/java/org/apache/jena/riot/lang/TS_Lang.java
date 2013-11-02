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

package org.apache.jena.riot.lang;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
     TestIRI.class
    , TestLang.class
    , TestBlankNodeAllocator.class
    , TestNodeAllocator.class       // Older tests 
    , TestLabelToNode.class         // Newer tests
    , TestNodeToLabel.class
    , TestTurtleTerms.class
    , TestLangNTriples.class
    , TestLangNQuads.class
    , TestLangTurtle.class
    , TestLangTrig.class
    , TestLangRdfJson.class
    , TestParserFactory.class
    , TestPipedRDFIterators.class
    , TestCollectorStream.class
})


public class TS_Lang
{
    @BeforeClass public static void beforeClass()   { BaseTest.setTestLogging() ; } 
    @AfterClass public static void afterClass()     { BaseTest.unsetTestLogging() ; }
}

