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

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.jena.irix.IRIx;

public class PrefixUtils {
    public static final String PREFIX = "prefix" ;
    public static final String URI = "uri" ;
    public static final Set<String> PARAMS = Set.of(PREFIX, URI);

    private PrefixUtils() {}
    private static final Pattern regex = Pattern.compile("\\p{Alpha}([\\w.-]*\\w)?");

    public static boolean isPrefixesParam(String param) {
        return PARAMS.contains(param);
    }

    public static boolean prefixIsValid(String prefix) {
        if ( prefix.isEmpty() )
            return true;
        return regex.matcher(prefix).matches();
    }

    public static boolean uriIsValid(String uri) {
        try {
            IRIx iri = IRIx.create(uri);
            return iri.isReference();
        }
        catch (Exception ex) {
            return false;
        }
    }
}
