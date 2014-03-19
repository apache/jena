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

package com.hp.hpl.jena.sparql.engine.http;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.WebContent ;
import org.junit.Test ;

/** A few tests of QueryEngineHTTP - mostly it gets tested in Fuseki */
public class TestQueryEngineHTTP extends BaseTest {
    
    // Check the headers contain the standard types. 
    @Test public void selectHeader_01() {
        test(QueryEngineHTTP.defaultSelectHeader(), WebContent.contentTypeResultsJSON) ;
    }
    
    @Test public void selectHeader_02() {
        test(QueryEngineHTTP.defaultSelectHeader(), WebContent.contentTypeResultsXML) ;
    }
    
    @Test public void selectHeader_03() {
        test(QueryEngineHTTP.defaultSelectHeader(), WebContent.contentTypeTextTSV) ;
    }
    
    @Test public void constructHeader_01() {
        test(QueryEngineHTTP.defaultConstructHeader(), WebContent.contentTypeTurtle) ;
    }
    
    @Test public void constructHeader_02() {
        test(QueryEngineHTTP.defaultConstructHeader(), WebContent.contentTypeRDFXML) ;
    }
    
    @Test public void constructHeader_03() {
        test(QueryEngineHTTP.defaultConstructHeader(), WebContent.contentTypeNTriples) ;
    }
    
    @Test public void askHeader_01() {
        test(QueryEngineHTTP.defaultSelectHeader(), WebContent.contentTypeResultsJSON) ;
    }
    
    @Test public void askHeader_02() {
        test(QueryEngineHTTP.defaultSelectHeader(), WebContent.contentTypeResultsXML) ;
    }
    
    private static void test(String header, String content) {
        assertTrue(header.contains(content)) ;
    }
    
}

