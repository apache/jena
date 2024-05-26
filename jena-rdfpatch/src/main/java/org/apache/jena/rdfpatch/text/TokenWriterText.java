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

package org.apache.jena.rdfpatch.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.BufferingWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Chars;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.sparql.util.FmtUtils;

public class TokenWriterText implements TokenWriter {
    // Whether to space out the tuples a bit for readability.
    private static final boolean GAPS      = true;

    private final AWriter        out;
    private boolean              inTuple   = false;
    private boolean              inSection = false;

    private final NodeFormatter  fmt;

    private String               label;

    public static TokenWriter create(OutputStream out) {
        return new TokenWriterText(out);
    }

    public static TokenWriter create(StringWriter out) {
        return new TokenWriterText(out);
    }

    public static TokenWriter create(AWriter out) {
        return new TokenWriterText(out);
    }

    /**
     * Create a TokenOutputStreamWriter going to a OutputStream.
     *
     * @param out
     */
    private TokenWriterText(OutputStream out) {
        this(writer(out));
    }

    private static Writer writer(OutputStream out) {
        return IO.asUTF8(out);
    }

    /**
     * Create a TokenOutputStreamWriter going to a StringWriter.
     *
     * @param out
     */
    private TokenWriterText(Writer out) {
        this(null, null, IO.wrap(out));
    }

    /**
     * Create a TokenOutputStreamWriter going to a Writer, ideally one that
     * buffers (e.g. {@linkplain BufferingWriter}).
     *
     * @param out
     */
    private TokenWriterText(AWriter out) {
        this(null, null, out);
    }

    /**
     * Create a TokenOutputStreamWriter going to a Writer, with a given
     * NodeFormatter policy ideally one that buffers (e.g.
     * {@linkplain BufferingWriter}).
     */

    private TokenWriterText(String label, NodeFormatter formatter, AWriter out) {
        //formatter = new NodeFormatterBNode(formatter);
        formatter = new NodeFormatterBNode1();
        this.fmt = formatter;
        this.out = out;
        this.label = label;
    }

    // NodeFormatterTTL to get the number abbreviations.
    static class NodeFormatterBNode1 extends NodeFormatterTTL {

        public NodeFormatterBNode1() {
            super(null, PrefixMapFactory.emptyPrefixMap());
        }

        @Override
        public void formatBNode(AWriter w, Node n) {
            formatBNode(w, n.getBlankNodeLabel());
        }

        @Override
        public void formatBNode(AWriter w, String label) {
            w.print("<_:");
            w.print(label);
            w.print(">");
        }
    }

    // Temporary - for reference.
    // See notes about NodeTriple

//    static class NodeFormatterBNode extends NodeFormatterWrapper {
//        public NodeFormatterBNode(NodeFormatter other) {
//            super(other);
//        }
//
//        @Override
//        public void format(AWriter w, Node n) {
//            if ( n.isBlank() ) {
//                formatBNode(w, n);
//                return;
//            }
//            // Wrapped does not work with RDF-star because we need to override the bnode format recusively.
//            // IF wrapped.
////            if ( n.isNodeTriple() ) {
////                // Need to print blank nodes with this code.
////                Triple t = n.getTriple();
////                w.print("<< ");
////                format(w, t.getSubject());
////                w.print(" ");
////                format(w, t.getPredicate());
////                w.print(" ");
////                format(w, t.getObject());
////                // Need to write bnodes "our way".
////                w.print(" >>");
////                return;
////            }
//
//            super.format(w, n);
//        }
//
//        @Override
//        public void formatBNode(AWriter w, Node n) {
//            formatBNode(w, n.getBlankNodeLabel());
//        }
//
//        @Override
//        public void formatBNode(AWriter w, String label) {
//            w.print("<_:");
//            w.print(label);
//            w.print(">");
//        }
//    }
//
//    static class NodeFormatterWrapper implements NodeFormatter {
//        private final NodeFormatter fmt;
//
//        public NodeFormatterWrapper(NodeFormatter other) {
//            this.fmt = other;
//        }
//
//        @Override
//        public void format(AWriter w, Node n) {
//            fmt.format(w, n);
//        }
//
//        @Override
//        public void formatURI(AWriter w, Node n) {
//            fmt.formatURI(w, n);
//        }
//
//        @Override
//        public void formatURI(AWriter w, String uriStr) {
//            fmt.formatURI(w, uriStr);
//        }
//
//        @Override
//        public void formatVar(AWriter w, Node n) {
//            fmt.formatVar(w, n);
//        }
//
//        @Override
//        public void formatVar(AWriter w, String name) {
//            fmt.formatVar(w, name);
//        }
//
//        @Override
//        public void formatBNode(AWriter w, Node n) {
//            fmt.formatBNode(w, n);
//        }
//
//        @Override
//        public void formatBNode(AWriter w, String label) {
//            fmt.formatBNode(w, label);
//        }
//
//        @Override
//        public void formatLiteral(AWriter w, Node n) {
//            fmt.formatLiteral(w, n);
//        }
//
//        @Override
//        public void formatLitString(AWriter w, String lex) {
//            fmt.formatLitString(w, lex);
//        }
//
//        @Override
//        public void formatLitLang(AWriter w, String lex, String langTag) {
//            fmt.formatLitLang(w, lex, langTag);
//        }
//
//        @Override
//        public void formatLitDT(AWriter w, String lex, String datatypeURI) {
//            fmt.formatLitDT(w, lex, datatypeURI);
//        }
//    }

    @Override
    public void sendToken(Token token) {
        String string = tokenToString(token);
        write(string);
        gap(true);
    }

    @Override
    public void sendNode(Node node) {
        fmt.format(out, node);
        gap(false);
    }

    @Override
    public void sendString(String string) {
        fmt.formatLitString(out, string);
        gap(false);
    }

    @Override
    public void sendWord(String string) {
        write(string) ; // no escapes, no quotes
        gap(true);
    }

//    @Override
//    public void sendControl(char controlChar) {
//        String x = cntrlAsString(controlChar);
//        write(x);
//        gap(false);
//    }

    @Override
    public void sendNumber(long number) {
        write(Long.toString(number));
        gap(true);
    }

    @Override
    public void startTuple() {}

    @Override
    public void endTuple() {
        if ( !inTuple )
            return;
        out.write(Chars.CH_DOT);
        out.write("\n");
        inTuple = false;
        // If setup so that any added layers are not buffering, only the passed-in
        // InputStream or Writer, then this is not necessary.
        // flush();
    }

    @Override
    public void close() {
        if ( inTuple ) {}
        IO.close(out);
    }

    @Override
    public void flush() {
        out.flush();
    }

    // --------

    private String tokenToString(Token token) {
        switch (token.getType()) {
            // superclass case NODE:
            case IRI :
                return "<" + token.getImage() + ">";
            case PREFIXED_NAME :
                notImplemented(token);
                return null;
            case BNODE :
                return "_:" + token.getImage();
            //case BOOLEAN:
            case STRING :
                return "\"" + FmtUtils.stringEsc(token.getImage()) + "\"";
            case LITERAL_LANG :
                return "\"" + FmtUtils.stringEsc(token.getImage()) + "\"@" + token.getImage2();
            case LITERAL_DT :
                return "\"" + FmtUtils.stringEsc(token.getImage()) + "\"^^" + tokenToString(token.getSubToken2());
            case INTEGER :
            case DECIMAL :
            case DOUBLE :
                return token.getImage();

            // Not RDF
            case KEYWORD :
                return token.getImage();
            case DOT :
                return Chars.S_DOT;
            case VAR :
                return "?" + token.getImage();
            case COMMA :
                return Chars.S_COMMA;
            case SEMICOLON :
                return Chars.S_SEMICOLON;
            case COLON :
                return Chars.S_COLON;
            case LT :
                return Chars.S_LT;
            case GT :
                return Chars.S_GT;
            case LE :
                return Chars.S_LE;
            case GE :
                return Chars.S_GE;
            case UNDERSCORE :
                return Chars.S_UNDERSCORE;
            case LBRACE :
                return Chars.S_LBRACE;
            case RBRACE :
                return Chars.S_RBRACE;
            case LPAREN :
                return Chars.S_LPAREN;
            case RPAREN :
                return Chars.S_RPAREN;
            case LBRACKET :
                return Chars.S_LBRACKET;
            case RBRACKET :
                return Chars.S_RBRACKET;
            case PLUS :
                return Chars.S_PLUS;
            case MINUS :
                return Chars.S_MINUS;
            case STAR :
                return Chars.S_STAR;
            case SLASH :
                return Chars.S_SLASH;
            case RSLASH :
                return Chars.S_RSLASH;
            case HEX :
                return "0x" + token.getImage();
            // Syntax
            // COLON is only visible if prefix names are not being processed.
            case DIRECTIVE :
                return "@" + token.getImage();
            case VBAR :
                return Chars.S_VBAR;
            case AMPERSAND :
                return Chars.S_AMPHERSAND;
            case EQUALS :
                return Chars.S_EQUALS;
            default :
                notImplemented(token);
                return null;

            // case EOF:

// case EQUIVALENT :
// return "==";
// case LOGICAL_AND :
// return "&&";
// case LOGICAL_OR :
// return "||";
// case NL :
// break;
// case NODE :
// break;
// case WS :
// break;
        }
    }

    // A gap is always necessary for items that are not endLog-limited.
    // For example, numbers adjacent to numbers must have a gap but
    // quoted string then quoted string does not require a gap.
    private void gap(boolean required) {
        if ( required || GAPS )
            write(" ");
    }

    // Beware of multiple stringing.
    private void write(String string) {
        inTuple = true;
        out.write(string);
    }

    private static void exception(IOException ex) {
        throw new RiotException(ex);
    }

    private void notImplemented(Token token) {
        throw new RiotException("Unencodable token: " + token);
    }
}
