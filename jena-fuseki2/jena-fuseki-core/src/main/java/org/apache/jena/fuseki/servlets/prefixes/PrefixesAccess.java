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

package org.apache.jena.fuseki.servlets.prefixes;


import org.apache.jena.sparql.core.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

//interface for interacting with the storages
public interface PrefixesAccess {

    Optional<String> fetchURI(String prefix);

    Transactional transactional();

    void updatePrefix(String prefix, String uri);
    void removePrefix(String prefixToRemove);

    Map<String, String> getAll();

    /** Fetches the prefixes assigned to the provided URI. There can be multiple in the List.**/
    List<String> fetchPrefix(String uri);
}
