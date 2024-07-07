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

import org.apache.jena.fuseki.servlets.ActionPrefixesRW;
import org.apache.jena.fuseki.servlets.HttpAction;

/**
 * An {@linkActionREST} that provides a all the HTTP services over a fixed {@link PrefixesAccess}.
 * <p>
 * This is in support of testing.
 * Instead of taking the prefixes from the action, this classes uses a provided {@link PrefixesAccess}.
 */
public class ActionProcPrefixes extends ActionPrefixesRW {

    private final PrefixesAccess storage;

    public ActionProcPrefixes(PrefixesAccess storage) {
        this.storage = storage;
    }

    // Instead of taking the prefixes from the action, this classes uses a provided PrefixesAccess.
    @Override
    protected PrefixesAccess prefixes(HttpAction action) {
        return storage;
    }
}
