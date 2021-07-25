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

package org.apache.jena.jdbc.remote.results;

import org.apache.http.client.HttpClient;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.jdbc.results.AbstractResultSetTests;
import org.apache.jena.riot.web.HttpOp1;
import org.apache.jena.sys.JenaSystem ;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Abstract tests for remote endpoint connection result sets
 * 
 */
public abstract class AbstractRemoteEndpointResultSetTests extends AbstractResultSetTests {

	static HttpClient defaultHttpClient = HttpOp1.getDefaultHttpClient() ;
	// Used for all tests except auth tests.
	static HttpClient globalPoolingClient = HttpOp1.createPoolingHttpClient() ;
	
	@BeforeClass public static void beforeClassAbstract1() {
        JenaSystem.init() ;
        Fuseki.init();
		HttpOp1.setDefaultHttpClient(globalPoolingClient) ;
    }
	
	@AfterClass public static void afterClassAbstract1() {
		HttpOp1.setDefaultHttpClient(defaultHttpClient) ;
    }
}
