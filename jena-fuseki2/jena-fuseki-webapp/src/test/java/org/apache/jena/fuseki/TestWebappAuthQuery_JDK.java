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

package org.apache.jena.fuseki;

import java.net.URI;

import org.apache.jena.fuseki.test.HttpTest;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestWebappAuthQuery_JDK extends AbstractTestWebappAuth_JDK {
    @Test
    public void query_auth_jdk_01_no_auth() {
        QueryExecutionHTTP qe = QueryExecutionHTTP.create().endpoint(authServiceQuery).query("ASK { }").build();
        // No auth credentials should result in an error
        HttpTest.expectQuery401(()->qe.execAsk());
    }

    @Test
    public void query_auth_jdk_02_bad_auth() {
        // Auth - bad password
        QueryExecutionHTTP qe = withAuthJDK(QueryExecutionHTTP.create().endpoint(authServiceQuery).query("ASK { }"),
                                              "allowed", "incorrect");
        HttpTest.expectQuery401(()->qe.execAsk());
    }

    @Test
    public void query_auth_jdk_03_good_auth() {
        // Auth credentials for valid user with correct password
        QueryExecutionHTTP qe = withAuthJDK(QueryExecutionHTTP.create().endpoint(authServiceQuery).query("ASK { }"),
                                              "allowed", "password");
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_authenv_01_good() {
        // Auth credentials for valid user with correct password
        QueryExecutionHTTP qe = QueryExecutionHTTP.create().endpoint(authServiceQuery).query("ASK { }").build();
        String dsURL = authServiceQuery;
        URI uri = URI.create(dsURL);
        AuthEnv.get().registerUsernamePassword(uri, "allowed", "password");
        try {
            Assert.assertTrue(qe.execAsk());
        } finally {
            AuthEnv.get().unregisterUsernamePassword(uri);
            //AuthEnv.get().clearAuthRequestModifiers();
        }
    }

    @Test
    public void query_authenv_02_prefix_good() {
        QueryExecutionHTTP qe = QueryExecutionHTTP.create().endpoint(authServiceQuery).query("ASK { }").build();
        // Dataset URL.
        String dsURL = "http://localhost:"+authPort+authDatasetPath;
        URI uri = URI.create(dsURL);
        AuthEnv.get().registerUsernamePassword(uri, "allowed", "password");
        try {
            Assert.assertTrue(qe.execAsk());
        } finally {
            AuthEnv.get().unregisterUsernamePassword(uri);
        }
    }

    @Test
    public void query_authenv_03_bad_endpoint() {
        QueryExecutionHTTP qe = QueryExecutionHTTP.create().endpoint(authServiceQuery).query("ASK { }").build();
        // Wrong registration
        String dsURL = "http://localhost:"+authPort+"/anotherPlace";
        URI uri = URI.create(dsURL);
        AuthEnv.get().registerUsernamePassword(uri, "allowed", "password");
        try {
            HttpTest.expectQuery401(()->qe.execAsk());
        } finally {
            AuthEnv.get().unregisterUsernamePassword(uri);
        }
    }
}
