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

package org.apache.jena.iri.impl;

public interface Force {
    static final int SHOULD = 0;
    static final int DNS = 1;
    static final int MINTING = 2;
    static final int SECURITY = 3;
//    static final int SCHEME_SPECIFIC = 4;
    static final int MUST = 4;
    static final int SIZE = 5;
    static final int minting = 1 << MINTING;
    static final int must = 1 << MUST;
    static final int should = 1 << SHOULD;
    static final int dns = 1 << DNS;
    static final int security = 1 << SECURITY;

}
