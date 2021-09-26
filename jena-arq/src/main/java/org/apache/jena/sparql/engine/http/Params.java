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

package org.apache.jena.sparql.engine.http;

/** A collection of parameters for protocol use.
 * @deprecated Provides for use of old Apache HttpClient related code. To be removed.
 */
@Deprecated
public class Params extends org.apache.jena.sparql.exec.http.Params {
    public Params() {
        super();
    }

    public Params(Params other) {
        super();
        merge(other);
    }

    /** @deprecated Use {@link #add(String,String)} */
    @Deprecated
    public Params addParam(String name, String value) {
        add(name, value);
        return this;
    }

    /** @deprecated Use {@link #add(String)} */
    @Deprecated
    public Params addParam(String name) {
        add(name);
        return this;
    }
}
