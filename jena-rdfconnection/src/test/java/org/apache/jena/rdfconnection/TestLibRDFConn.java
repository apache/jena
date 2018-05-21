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

package org.apache.jena.rdfconnection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestLibRDFConn {
    
    @Test public void service_url_01() {
        testServiceName(null, "XYZ", "XYZ"); 
    }
    
    @Test public void service_url_02() {
        testServiceName("http://example/", "XYZ", "http://example/XYZ"); 
    }

    @Test public void service_url_03() {
        testServiceName("http://example/abc", "XYZ", "http://example/abc/XYZ"); 
    }

    @Test public void service_url_04() {
        testServiceName("http://example/abc/", "XYZ", "http://example/abc/XYZ"); 
    }
    
    @Test public void service_url_05() {
        testServiceName("http://example/abc?param=value", "XYZ", "http://example/abc/XYZ?param=value"); 
    }

    @Test public void service_url_06() {
        testServiceName("http://example/dataset", "http://other/abc/", "http://other/abc/"); 
    }

    @Test public void service_url_07() {
        testServiceName("http://example/dataset", "http://example/abc/XYZ?param=value", "http://example/abc/XYZ?param=value"); 
    }
    
    private static void testServiceName(String destination, String service, String expected) {
        String x = LibRDFConn.formServiceURL(destination, service);
        assertEquals(expected, x);
    }
    
    // Assumes service name constructed correctly (see above). 
    
    @Test public void gsp_url_01() {
        testGSP("http://example/", null, "http://example/?default");  
    }

    @Test public void gsp_url_02() {
        testGSP("http://example/", "default", "http://example/?default");  
    }

    @Test public void gsp_url_03() {
        testGSP("http://example/dataset", null, "http://example/dataset?default");  
    }

    @Test public void gsp_url_04() {
        testGSP("http://example/dataset", "default", "http://example/dataset?default");  
    }
    
    @Test public void gsp_url_05() {
        testGSP("http://example/dataset?param=value", "default", "http://example/dataset?param=value&default");  
    }
    
    @Test public void gsp_url_06() {
        testGSP("http://example/?param=value", "default", "http://example/?param=value&default");  
    }

    @Test public void gsp_url_07() {
        testGSP("http://example/dataset?param=value", "default", "http://example/dataset?param=value&default");  
    }
    
    @Test public void gsp_url_08() {
        testGSP("http://example/dataset/?param=value", "default", "http://example/dataset/?param=value&default");  
    }

    @Test public void gsp_url_11() {
        testGSP("http://example/dataset", "name", "http://example/dataset?graph=name");  
    }

    @Test public void gsp_url_12() {
        testGSP("http://example/", "name", "http://example/?graph=name");  
    }
    
    @Test public void gsp_url_13() {
        testGSP("http://example/dataset/", "name", "http://example/dataset/?graph=name");  
    }

    @Test public void gsp_url_20() {
        testGSP("http://example/dataset?param=value", null, "http://example/dataset?param=value&default");  
    }

    @Test public void gsp_url_21() {
        testGSP("http://example/?param=value", null, "http://example/?param=value&default");  
    }

    @Test public void gsp_url_16() {
        testGSP("http://example/dataset?param=value", "name", "http://example/dataset?param=value&graph=name");  
    }

    @Test public void gsp_url_17() {
        testGSP("http://example/?param=value", "name", "http://example/?param=value&graph=name");  
    }

    private void testGSP(String gsp, String graphName, String expected) {
        String x = LibRDFConn.urlForGraph(gsp, graphName);
        assertEquals(expected, x);
    }
    
}
