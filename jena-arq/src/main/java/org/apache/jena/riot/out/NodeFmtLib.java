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

package org.apache.jena.riot.out;

import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.Chars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotChars;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Quad;

/** Presentation utilities for Nodes, Triples, Quads and more.
 * <p>
 * Methods {@code str} generate a re-parseable string.
 * <p>
 * Methods {@code displayStr} do not guarantee a re-parseable string
 * e.g. may use abbreviations or common prefixes.
 */
public class NodeFmtLib
{
    // Replaces FmtUtils
    // See and use EscapeStr

    // Turtle formatter, no prefix map or base. Does literal abbreviations.
    private static final NodeFormatter ttlFormatter = new NodeFormatterTTL();
    // N-triples formatter, no prefix map or base. Does literal abbreviations.
    private static final NodeFormatter ntFormatter = new NodeFormatterNT();
    private static final String nullStr = "<null>";

    // Used for displayStr.
    private static PrefixMap dftPrefixMap = PrefixMapFactory.create();
    static {
        PrefixMapping pm = ARQConstants.getGlobalPrefixMap();
        Map<String, String> map = pm.getNsPrefixMap();
        for ( Map.Entry<String, String> e : map.entrySet() )
            dftPrefixMap.add(e.getKey(), e.getValue() );
    }

    /** Format a triple, using Turtle literal abbreviations. */
    public static String str(Triple t) {
        return strNodesTTL(t.getSubject(), t.getPredicate(), t.getObject());
    }

    /** Format a quad, using Turtle literal abbreviations. */
    public static String str(Quad q) {
        return strNodesTTL(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
    }

    /** @deprecated Use {@link #strNT}. */
    @Deprecated
    public static String str(Node node) {
        return strNT(node);
    }

    /** With Turtle abbreviation for literals, no prefixes of base URI */
    public static String strTTL(Node node) {
        return strNode(node, ttlFormatter);
    }

    /** Format in N-triples style. */
    public static String strNT(Node node) {
        return strNode(node, ttlFormatter);
    }

    /** Format in N-triples style. */
    private static void strNT(IndentedWriter w, Node node) {
        formatNode(w, node, ntFormatter);
    }

    /** Format in Turtle style. */
    private static void strTTL(IndentedWriter w, Node node) {
        formatNode(w, node, ttlFormatter);
    }

    /** Format in Turtle style, using the prefix map. */
    public static String str(Node node, PrefixMap prefixMap) {
        return str(node, null, prefixMap);
    }

    /** Format in Turtle style, using the base URI and prefix map. */
    public static String str(Node node, String base, PrefixMap prefixMap) {
        IndentedLineBuffer sw = new IndentedLineBuffer();
        formatNode(sw, node, base, prefixMap);
        return sw.toString();
    }

    /** Use {@link #strNodesNT} or {@link #strNodesTTL} */
    @Deprecated
    public static String strNodes(Node...nodes) {
        return strNodesNT(nodes);
    }

    public static String strNodesNT(Node...nodes) {
        return strNodes(NodeFmtLib::strNT, nodes);
    }

    public static String strNodesTTL(Node...nodes) {
        return strNodes(NodeFmtLib::strTTL, nodes);
    }

    private static String strNodes(BiConsumer<IndentedWriter, Node> output, Node...nodes) {
        IndentedLineBuffer sw = new IndentedLineBuffer();
        boolean first = true;
        for ( Node n : nodes ) {
            if ( !first )
                sw.append(" ");
            first = false;
            if ( n == null ) {
                sw.append("null");
                continue;
            }
            output.accept(sw, n);
        }
        return sw.toString();
    }

    /** @deprecated To be removed. */
    @Deprecated
    public static void serialize(IndentedWriter w, Node node, String base, PrefixMap prefixMap) {
        formatNode(w, node, base, prefixMap);
    }

    private static void formatNode(IndentedWriter w, Node node, NodeFormatter formatter) {
        formatter.format(w, node);
    }

    private static void formatNode(IndentedWriter w, Node node, String base, PrefixMap prefixMap) {
        NodeFormatter formatter;
        if ( base == null && prefixMap == null )
            formatter = ntFormatter;
        else
            formatter = new NodeFormatterTTL(base, prefixMap);
        formatter.format(w, node);
    }

    private static String strNode(Node node, NodeFormatter formatter) {
        IndentedLineBuffer sw = new IndentedLineBuffer();
        formatter.format(sw, node);
        return sw.toString();
    }

    /** A displayable string for an RDFNode. Includes common abbreviations */
    public static String displayStr(RDFNode obj) {
        if ( obj == null )
            return nullStr;
        return displayStr(obj.asNode());
    }

    public static String displayStr(Triple t) {
        if ( t == null )
            return nullStr;
        return displayStrNodes(t.getSubject(), t.getPredicate(), t.getObject());
    }

    public static String displayStr(Node node) {
        if ( node == null )
            return nullStr;
        return str(node, null, dftPrefixMap);
    }

    private static String displayStrNodes(Node...nodes) {
        StringJoiner sj = new StringJoiner(" ");
        for ( Node n : nodes )
            sj.add(displayStr(n));
        return sj.toString();
    }

    // ---- Blank node labels.

    // Strict N-triples only allows [A-Za-z][A-Za-z0-9]
    static char encodeMarkerChar = 'X';

    // These two form a pair to convert bNode labels to a safe (i.e. legal N-triples form) and back agains.

    // Encoding is:
    // 1 - Add a Letter
    // 2 - Hexify, as Xnn, anything outside ASCII A-Za-z0-9
    // 3 - X is encoded as XX

    private static char LabelLeadingLetter = 'B';

    public static String encodeBNodeLabel(String label) {
        StringBuilder buff = new StringBuilder();
        // Must be at least one char and not a digit.
        buff.append(LabelLeadingLetter);

        for ( int i = 0 ; i < label.length() ; i++ ) {
            char ch = label.charAt(i);
            if ( ch == encodeMarkerChar ) {
                buff.append(ch);
                buff.append(ch);
            } else if ( RiotChars.isA2ZN(ch) )
                buff.append(ch);
            else
                Chars.encodeAsHex(buff, encodeMarkerChar, ch);
        }
        return buff.toString();
    }

    // Assumes that blank nodes only have characters in the range of 0-255
    public static String decodeBNodeLabel(String label) {
        StringBuilder buffer = new StringBuilder();

        if ( label.charAt(0) != LabelLeadingLetter )
            return label;

        // Skip first.
        for ( int i = 1 ; i < label.length() ; i++ ) {
            char ch = label.charAt(i);

            if ( ch != encodeMarkerChar ) {
                buffer.append(ch);
            } else {
                // Maybe XX or Xnn.
                char ch2 = label.charAt(i + 1);
                if ( ch2 == encodeMarkerChar ) {
                    i++;
                    buffer.append(ch);
                    continue;
                }

                // Xnn
                i++;
                char hiC = label.charAt(i);
                int hi = Bytes.hexCharToInt(hiC);
                i++;
                char loC = label.charAt(i);
                int lo = Bytes.hexCharToInt(loC);

                int combined = ((hi << 4) | lo);
                buffer.append((char)combined);
            }
        }

        return buffer.toString();
    }
}
