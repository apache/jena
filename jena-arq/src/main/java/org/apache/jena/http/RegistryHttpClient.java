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

package org.apache.jena.http;

import java.net.http.HttpClient;
import java.util.function.Function;

import org.apache.jena.http.sys.AbstractRegistryWithPrefix;

/**
 * A service registry is a collection of {@link HttpClient HttpClients} to use for
 * specific URLs.
 * <p>
 * The lookup ({@link #find}) is by longest prefix. e.g. a registration of
 * "http://someHost/" or "http://someHost/dataset" will apply to
 * "http://someHost/dataset/sparql" and "http://someHost/dataset/update" but not to
 * https://someHost/... which uses "https".
 * <p>
 * This is one way of managing authentication for particular remote services -
 * register a {@link HttpClient} with authentication credentials.
 */
public class RegistryHttpClient extends AbstractRegistryWithPrefix<String, HttpClient> {

    private static RegistryHttpClient singleton = new RegistryHttpClient();
    public static RegistryHttpClient get() { return singleton; }

    private RegistryHttpClient() {
        super(Function.identity());
    }
}
