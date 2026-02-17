/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.riot.system.jsonld;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdOptions;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Get the (jsonld) options from the jena context if exists or create default
 */
public class TitaniumJsonLdOptions {

    private static final String SYMBOLS_NS = "http://jena.apache.org/riot/jsonld#";
    /**
     * value: the option object expected by JsonLdProcessor (instance of JsonLdOptions)
     */
    public static final Symbol JSONLD_OPTIONS = SystemARQ.allocSymbol(SYMBOLS_NS, "options");

    public static JsonLdOptions get(String baseURI, Context jenaContext) {
        JsonLdOptions opts = jenaContext.get(JSONLD_OPTIONS);
        if ( opts == null )
            opts = new JsonLdOptions();
        if ( baseURI != null )
            opts.setBase(URI.create(baseURI));
        return opts;
    }
}
