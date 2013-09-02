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
import org.apache.jena.riot.web.HttpOp;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hp.hpl.jena.query.ARQ;

/**
 * Abstract tests for remote endpoint connection result sets
 * 
 */
public abstract class AbstractRemoteEndpointResultSetTests extends AbstractResultSetTests {

	static HttpClient defaultHttpClient = HttpOp.getDefaultHttpClient() ;
	// Used for all tests except auth tests.
	static HttpClient globalCachingClient = HttpOp.createCachingHttpClient() ;
	
	@BeforeClass public static void beforeClassAbstract1() {
    	ARQ.init();
        Fuseki.init();
		HttpOp.setDefaultHttpClient(globalCachingClient) ;
    }
	
	@AfterClass public static void afterClassAbstract1() {
		HttpOp.setDefaultHttpClient(defaultHttpClient) ;
    }
}
