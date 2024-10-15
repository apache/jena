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

package org.apache.jena.rfc3986.cmd;

import java.io.PrintStream;

import org.apache.jena.rfc3986.*;

public class CmdIRI {
    public static void main(String... args) {
        if ( args.length == 0 ) {
            System.err.println("No iri string");
            System.exit(1);
        }

        boolean first = true;
        PrintStream out = System.out;
        // Errors and warnings.
        PrintStream err = System.err;

        for (String iriStrInput : args ) {
            if ( iriStrInput.startsWith("<") && iriStrInput.endsWith(">") )
                iriStrInput = iriStrInput.substring(1, iriStrInput.length()-1);
            if ( !first ) {
                first = false;
                out.println();
            }

            String iriStr = iriStrInput;
            try {
                IRI3986 iri = RFC3986.create(iriStr);
                IRI3986 iri1 = iri.normalize();

                out.printf("Input: <%s>\n", iriStr);
                out.printf("  Parsed:       %s\n", iri.rebuild()) ;
                out.printf("  Absolute:     %s\n", iri.isAbsolute());
                out.printf("  Relative:     %s\n", iri.isRelative());
                out.printf("  Hierarchical: %s\n", iri.isHierarchical());
                out.printf("  Rootless:     %s\n", iri.isRootless());
                if ( ! iri.equals(iri1) )
                  out.printf("  Normalized:   %s\n", iri1) ;
                out.printf("\n");
                out.printf("%s|%s|  ", "Scheme",     iri.scheme());
                out.printf("%s|%s|  ", "Authority",  iri.authority());
                out.printf("%s|%s|  ", "Host",       iri.host());
                if ( iri.hasPort() )
                    out.printf("%s|%s|  ", "Port",       iri.port());
                out.printf("%s|%s|  ", "Path",       iri.path());
                out.printf("%s|%s|  ", "Query",      iri.query());
                out.printf("%s|%s|", "Fragment",   iri.fragment());
                out.println();
                if ( iri.hasViolations() ) {
                    out.println();
                    out.println("Scheme specific warnings:");
                    iri.forEachViolation(v->{
                        out.print("   ");
                        err.printf("%s\n", v.message());
                    });
                }
            } catch (IRIParseException ex) {
                System.err.printf("Error: %s\n", ex.getMessage());
            }
        }
    }
}
