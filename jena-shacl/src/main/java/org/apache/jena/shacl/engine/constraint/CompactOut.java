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

package org.apache.jena.shacl.engine.constraint;

import java.util.Collection;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;

/** Helper for SHACL Compact Syntax output */
public class CompactOut {

    public static void compactQuotedString(IndentedWriter out, String param, String value) {
        out.print(param);
        out.print("=");
        printQuotedString(out, value);
    }

    public static void compactUnquotedString(IndentedWriter out, String param, String value) {
        out.print(param);
        out.print("=");
        out.print(value);
    }

    public static void compact(IndentedWriter out, String param, int value) {
        out.print(param);
        out.print("=");
        out.print(Integer.toString(value));
    }

    public static void compact(IndentedWriter out, NodeFormatter nodeFmt, String param, Node value) {
        out.print(param);
        out.print("=");
        nodeFmt.format(out, value);
    }

    public static void compactArrayNodes(IndentedWriter out, NodeFormatter nodeFmt, String param, Collection<Node> values) {
        out.print(param);
        out.print("=");
        out.print("[ ");
        boolean first = true;
        for ( Node n : values ) {
            if ( ! first )
                out.print(" ");
            nodeFmt.format(out, n);
            first = false;
        }
        out.print(" ]");
    }

    public static void compactArrayString(IndentedWriter out, NodeFormatter nodeFmt, String param, Collection<String> values) {
        out.print(param);
        out.print("=");
        out.print("(");
        boolean first = true;
        for ( String str : values ) {
            if ( ! first )
                out.print(" ");
            printQuotedString(out, str);
            first = false;
        }
        out.print(")");
    }

    private static void printQuotedString(IndentedWriter out, String str) {
        out.print('"');
        EscapeStr.stringEsc(out, str, '"', true, CharSpace.UTF8);
        out.print('"');
    }
}
