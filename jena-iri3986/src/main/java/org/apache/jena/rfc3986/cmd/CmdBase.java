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

import org.apache.jena.rfc3986.IRI3986;
import org.apache.jena.rfc3986.IRIParseException;
import org.apache.jena.rfc3986.RFC3986;

class CmdBase {

    protected static IRI3986 createOrExit(String iriStr, String errMsg) {
        try {
            return RFC3986.create(iriStr);
        } catch (IRIParseException ex) {
            System.err.println(errMsg+" IRI: " + ex.getMessage());
            System.exit(1);
        }
        return null;
    }

    protected static void print(IRI3986 iri) {
        if ( iri == null )
            return ;
        try {

//                    System.out.println("Absolute: "+iri.isAbsolute());
//                    System.out.println("Relative: "+iri.isRelative());
//                    System.out.println("Hierarchical: "+iri.isHierarchical());
//                    System.out.println("Rootless: "+iri.isRootless());

            System.out.print("  ");
            System.out.printf("%s|%s|  ", "Scheme",     iri.scheme());
            System.out.printf("%s|%s|  ", "Authority",  iri.authority());
            System.out.printf("%s|%s|  ", "Path",       iri.path());
            System.out.printf("%s|%s|  ", "Query",      iri.query());
            System.out.printf("%s|%s|"  , "Fragment",   iri.fragment());
            System.out.println();
            if ( iri.hasViolations() ) {
                iri.forEachViolation(v->{
                    System.out.println();
                    System.err.println("Scheme specific error:");
                    System.err.println("    "+v.message());
                });
            }
        } catch (IRIParseException ex) {
            System.err.println(ex.getMessage());
        }
    }

    static String fixup(String iriStr) {
        if ( iriStr.startsWith("<") && iriStr.endsWith(">") )
            iriStr = iriStr.substring(1, iriStr.length()-1);
        return iriStr;
    }
}
