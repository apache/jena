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

package org.apache.jena.fuseki;

import static org.apache.jena.fuseki.ServerTest.serviceGSP ;
import static org.apache.jena.fuseki.ServerTest.serviceQuery ;
import static org.apache.jena.fuseki.ServerTest.serviceUpdate ;
import static org.apache.jena.fuseki.ServerTest.urlDataset ;

import org.junit.Test ;

public class TestHttpOptions extends AbstractFusekiTest
{
    @Test
    public void options_query() {
        String v = FusekiTest.execOptions(serviceQuery) ;
        FusekiTest.assertStringList(v, "GET", "OPTIONS", "POST") ;
    }
    
    @Test
    public void options_update() {
        String v = FusekiTest.execOptions(serviceUpdate) ;
        FusekiTest.assertStringList(v, "OPTIONS", "POST") ;
    }
    
    @Test
    public void options_dataset_01() {
        String v = FusekiTest.execOptions(urlDataset) ;
        // Not DELETE
        FusekiTest.assertStringList(v, "HEAD", "GET", "OPTIONS", "POST", "PUT") ;
    }

    @Test
    public void options_dataset_02() {
        //"quads"
    }
    
    @Test
    public void options_gsp_rw() {
        String v = FusekiTest.execOptions(serviceGSP+"?default") ;
        FusekiTest.assertStringList(v, "GET", "OPTIONS", "HEAD", "POST", "PUT", "DELETE") ;
    }

}

