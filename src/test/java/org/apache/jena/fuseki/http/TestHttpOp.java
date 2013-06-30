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

package org.apache.jena.fuseki.http;

import java.io.IOException ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.fuseki.ServerTest ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.web.HttpSC ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.util.FileUtils ;

public class TestHttpOp extends BaseTest {
    // Test HttpOp from ARQ
    
    static String pingURL = ServerTest.urlRoot+"ping.txt" ;
    @BeforeClass public static void beforeClass() { ServerTest.allocServer() ; }
    @AfterClass  public static void afterClass()  { ServerTest.freeServer() ; }

    @Test public void httpGet_01() {
        HttpOp.execHttpGet(pingURL) ;
    }
    
    @Test public void httpGet_02() {
        try {
            HttpOp.execHttpGet(ServerTest.urlRoot+"does-not-exist") ;
            fail("No exception") ;
        } catch(HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
    }

    @Test public void httpGet_03() throws IOException {
        String x = HttpOp.execHttpGetString(pingURL) ;
        String y = FileUtils.readWholeFileAsUTF8("pages/ping.txt") ;
        assertEquals(y,x) ;
    }   
    
    @Test public void httpGet_04() {
        String x = HttpOp.execHttpGetString(ServerTest.urlRoot+"does-not-exist") ;
        assertNull(x) ;
    }
    
}

