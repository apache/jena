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

package jena;

import java.io.PrintStream;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.iri3986.provider.IRIProvider3986;
import org.apache.jena.iri3986.provider.IRIProvider3986.IRIx3986;
import org.apache.jena.irix.*;
import org.apache.jena.rfc3986.IRI3986;
import org.apache.jena.rfc3986.IRIParseException;

/**
 * Parse and print IRIs
 */
public class iri
{
    private static PrintStream out = System.out;
    // Errors and warnings.
    private static PrintStream err = System.err;

    public static void main(String... args)
    {
        boolean first = true ;
        for ( String iriStr : args )
        {
            if ( iriStr.startsWith("<") && iriStr.endsWith(">") )
                iriStr = iriStr.substring(1, iriStr.length()-1) ;

            if ( ! first )
                System.out.println() ;
            first = false ;

            String setting = Lib.getenv(SystemIRIx.sysPropertyProvider, SystemIRIx.envVariableProvider);

            IRIProvider provider = null;
            if ( setting != null ) {
                provider = switch(setting) {
                    case "IRI3986" -> new IRIProvider3986();
                    default -> {
                        System.err.println("Unknown IRI Provider: "+setting);
                        System.exit(1);
                        yield null;
                    }
                };
            }

            if ( provider == null )
                provider = SystemIRIx.getProvider();

            IRIx irix;
            try {
                irix = provider.create(iriStr);
            } catch (IRIException ex) {
                System.err.println(ex.getMessage());
                continue;
            }

            // jena-iri3986
            if ( irix instanceof IRIx3986 iri3986 ) {
                print(iri3986, iriStr);
                continue;
            }
            print(irix, iriStr);
        }
    }

    private static void print(IRIx3986 irix, String iriStr) {
        try {
            IRI3986 iri = irix.getImpl();
            IRI3986 iri1 = iri.normalize();

            out.printf("Input: <%s>\n", iriStr);
            out.printf("    Parsed:       %s\n", iri.rebuild()) ;
            out.printf("    Absolute:     %s\n", iri.isAbsolute());
            out.printf("    Relative:     %s\n", iri.isRelative());
            out.printf("    Hierarchical: %s\n", iri.isHierarchical());
            out.printf("    Rootless:     %s\n", iri.isRootless());
            if ( ! iri.equals(iri1) )
              out.printf("    Normalized:   %s\n", iri1) ;
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

    private static void print(IRIx irix, String iriStr) {
        try {
            IRIx iri1 = irix.normalize();

            out.printf("Input: <%s>\n", iriStr);
            out.printf("    Absolute:     %s\n", irix.isAbsolute());
            out.printf("    Relative:     %s\n", irix.isRelative());
            if ( ! irix.equals(iri1) )
              out.printf("    Normalized:   %s\n", iri1) ;
            out.printf("\n");
            out.printf("%s|%s|  ", "Scheme",     irix.scheme());
            out.println();
            if ( irix.hasViolations() ) {
                out.println();
                out.println("Scheme specific warnings:");
                irix.handleViolations((error, msg)->{
                    String type = (error? "Error:" : "Warn:");
                    out.print("   ");
                    err.printf("%-6s\n", type, msg);
                });
            }
        } catch (IRIParseException ex) {
            System.err.printf("Error: %s\n", ex.getMessage());
        }

    }


}
