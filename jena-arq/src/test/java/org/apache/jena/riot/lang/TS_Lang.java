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

package org.apache.jena.riot.lang;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.atlas.legacy.BaseTest2;
import org.apache.jena.riot.lang.rdfxml.TC_RIOT_RDFXML;

@Suite
@SelectClasses({
     TestIRI.class
    , TestRDFLanguages.class
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

    , TestLangJsonLD.class
    , TestLangJsonLD_DocLoader.class
    , TestLangJsonLD_prefixes.class

    , TestRDFXML_ReaderProperties.class
    , TestTriXReader.class
    , TestTriXBad.class
    // Protobuf is done in the "protobuf" package
    // Thrift is done in the "thrift" package
    , TestParserFactory.class
    , TestCollectorStream.class

    , TC_RIOT_RDFXML.class
})

public class TS_Lang
{
    @BeforeAll public static void beforeClass()   { BaseTest2.setTestLogging(); }
    @AfterAll public static void afterClass()     { BaseTest2.unsetTestLogging(); }
}

