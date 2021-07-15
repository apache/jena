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

package org.apache.jena.fuseki.main.examples;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.time.Duration;

import org.apache.jena.http.auth.AuthLib;

/** Shared code in examples. */
public class ExamplesLib {
    /** HttpClient with user/password */
    static HttpClient httpClient(String user, String password) {
        Authenticator authenticator = AuthLib.authenticator(user, password);
        return  HttpClient.newBuilder().authenticator(authenticator).connectTimeout(Duration.ofSeconds(10)).build();
        // Apache HttpClient
//        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
//        Credentials credentials = new UsernamePasswordCredentials(user, password);
//        credsProvider.setCredentials(AuthScope.ANY, credentials);
//        HttpClient client = HttpOp.createPoolingHttpClientBuilder().setDefaultCredentialsProvider(credsProvider).build();
//        return client;
    }


}
