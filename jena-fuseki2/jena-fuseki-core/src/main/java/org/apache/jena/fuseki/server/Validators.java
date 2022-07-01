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

package org.apache.jena.fuseki.server;

import java.util.function.Function;

import org.apache.jena.irix.Chars3986;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIException;

import static org.apache.jena.fuseki.server.Validators.ValidationPolicy.*;

public class Validators {

    enum ValidationPolicy { SERVICE("service"), ENDPOINT("endpoint"), GRAPH("graph");
        final String label;
        ValidationPolicy(String string) {
            this.label = string;
        }
    }

    // ---- Service Names - must be a path.
    private static Function<String, Boolean> fServiceName = Validators::isPath;
    private static Validator vServiceName = new Validator(SERVICE, Validators::isPath);

    public static ValidString serviceName(String name) {
        return ValidString.create(name, vServiceName);
    }

    // ---- Endpoints - must be a path
    private static Function<String, Boolean> fEndpointName = (epName) -> {
        return epName == null || Validators.isPath(epName);
    };
    private static Validator vEndpointName = new Validator(ENDPOINT, fEndpointName);

    public static ValidString endpointName(String name) {
        return ValidString.create(name, vEndpointName);
    }

    // ---- Graph names - any legal IRI.
    private static Function<String, Boolean> fGraphName = str -> {
        try {
            return IRIs.check(str);
        } catch (IRIException ex) { return false; }
    };
    private static Validator vGraphName = new Validator(GRAPH, fGraphName);

    public static ValidString graphName(String name) {
        return ValidString.create(name, vGraphName);
    }

    // test whether str is multiple segments suitable for an IRI path
    private static boolean isPath(String str) {
        if ( str == null )
            return false;
        int N = str.length();
        for ( int i = 0 ; i < N ; i++ ) {
            char ch = str.charAt(i);
            if ( ch != '/' && ! isSegmentChar(ch, str, i) )
                return false;
        }
        return true;
    }

    // segment rule from RFC3986.
    private static boolean isSegment(String str) {
        if ( str == null )
            return false;
        int N = str.length();
        for ( int i = 0 ; i < N ; i++ ) {
            char ch = str.charAt(i);
            if ( ! isSegmentChar(ch, str, i) )
                return false;
        }
        return true;
    }

    private static boolean isSegmentChar(char ch, String str, int posn) {
        return Chars3986.isPChar(ch, str, posn);
    }
}
