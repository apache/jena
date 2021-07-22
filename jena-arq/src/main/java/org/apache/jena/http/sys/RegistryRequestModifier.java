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

package org.apache.jena.http.sys;

import java.util.function.Function;

/**
 * A service registry is a set of actions to take to modify an HTTP request before
 * sending it to a specific endpoint.
 *
 * The key can be a prefix which must end in "/"
 */
public class RegistryRequestModifier extends AbstractRegistryWithPrefix<String, HttpRequestModifier> {

    private static RegistryRequestModifier singleton = new RegistryRequestModifier();
    public static RegistryRequestModifier get() { return singleton; }

    public RegistryRequestModifier() {
        super(Function.identity());
    }
}
