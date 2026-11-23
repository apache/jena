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

package org.apache.jena.sparql.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.sparql.lang.arq.ARQCharStream;
import org.apache.jena.sparql.lang.arq.javacc.ARQParserConstants;
import org.apache.jena.sparql.lang.arq.javacc.ARQParserTokenManager;

/**
 * Tests of ARQCharStream, the char stream of the ARQ parser.
 */
public class TestARQCharStream {

    /** Counts the times the buffer grows. */
    static class CountingCharStream extends ARQCharStream {
        int expansions = 0;

        CountingCharStream(Reader reader) {
            super(reader, 1, 1);
        }

        @Override
        protected void ExpandBuff(boolean wrapAround) {
            expansions++;
            super.ExpandBuff(wrapAround);
        }
    }

    @Test
    public void bufferGrowsByDoubling() throws IOException {
        // The first token moves the start of the second one past 2048 chars,
        // so the buffer wraps round before it grows.
        String first = "a".repeat(3000);
        String second = "b".repeat(1_000_000);
        CountingCharStream chars = new CountingCharStream(new StringReader(first + second));
        assertEquals(first, readToken(chars, first.length()));
        assertEquals(second, readToken(chars, second.length()));
        // Doubling from 4096 chars takes 8 expansions to hold 1,000,000 chars.
        // Growing by 2048 chars at a time takes about 490, and the time is quadratic.
        assertTrue(chars.expansions <= 10, "Buffer expansions: " + chars.expansions);
    }

    // A buffer of no chars never grows, so the token manager would take the input as empty.
    @Test
    public void bufferSizeZero() {
        assertThrows(IllegalArgumentException.class, ()->new ARQCharStream(new StringReader("SELECT ?x"), 1, 1, 0));
        ARQCharStream chars = new ARQCharStream(new StringReader(""), 1, 1);
        assertThrows(IllegalArgumentException.class, ()->chars.ReInit(new StringReader("SELECT ?x"), 1, 1, 0));
    }

    private static String readToken(ARQCharStream chars, int length) throws IOException {
        chars.BeginToken();
        for ( int i = 1 ; i < length ; i++ )
            chars.readChar();
        return chars.GetImage();
    }

    /** How a reader fails. */
    interface ReadFailure {
        void fail() throws IOException;
    }

    private static final ReadFailure UNCHECKED = ()->{ throw new IllegalStateException("Read failed"); };
    private static final ReadFailure MALFORMED = ()->{ throw new MalformedInputException(1); };

    /** A reader that returns the text and then fails. */
    private static Reader failingReader(String text, ReadFailure failure) {
        return new Reader() {
            private final Reader reader = new StringReader(text);

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                int n = reader.read(cbuf, off, len);
                if ( n == -1 )
                    failure.fail();
                return n;
            }

            @Override
            public void close() {}
        };
    }

    // The token manager takes an exception from the char stream as the end of the
    // input. A failing reader must be an error instead, or the rest of the input is
    // silently dropped.

    @Test
    public void readerFailure() {
        readerFailure(UNCHECKED);
        readerFailure(MALFORMED);
    }

    private static void readerFailure(ReadFailure failure) {
        ARQParserTokenManager tokens =
            new ARQParserTokenManager(new ARQCharStream(failingReader("<a> ; ", failure), 1, 1));
        assertEquals(ARQParserConstants.IRIref, tokens.getNextToken().kind);
        assertEquals(ARQParserConstants.SEMICOLON, tokens.getNextToken().kind);
        assertThrows(org.apache.jena.sparql.lang.arq.javacc.TokenMgrError.class, ()->tokens.getNextToken());
    }

    // An unchecked exception from reading the input, such as a cancellation, is not a
    // parse error: the parser passes it on unchanged.
    @Test
    public void readerRuntimeException() {
        IllegalStateException failure = new IllegalStateException("Cancelled");
        InputStream input = new InputStream() {
            private final InputStream bytes = new ByteArrayInputStream("INSERT DATA { <a> <b> <c> } ; ".getBytes(StandardCharsets.UTF_8));

            @Override
            public int read() throws IOException {
                int b = bytes.read();
                if ( b == -1 )
                    throw failure;
                return b;
            }
        };
        IllegalStateException ex = assertThrows(IllegalStateException.class, ()->UpdateFactory.read(input, Syntax.syntaxARQ));
        assertSame(failure, ex);
    }

    // An update read as bytes, as Fuseki reads update requests, with a byte that is
    // not valid UTF-8. The decoder fails with MalformedInputException: that must be a
    // parse error, not an empty or shortened update, and keep the exception as its cause.

    @Test
    public void malformedInputUpdate() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.writeBytes("INSERT DATA { <a> <b> <c> } ; INSERT DATA { <x> <y> \"".getBytes(StandardCharsets.UTF_8));
        bytes.write(0xFF);
        bytes.writeBytes("\" }".getBytes(StandardCharsets.UTF_8));
        QueryParseException ex = assertThrows(QueryParseException.class, ()->UpdateFactory.read(new ByteArrayInputStream(bytes.toByteArray()), Syntax.syntaxARQ));
        assertTrue(ex.getCause() instanceof MalformedInputException, "Cause: " + ex.getCause());
    }

    // A failed read of an update request must not commit part of the update. Fuseki
    // parses and executes an update in one write transaction (SPARQL_Update.execute),
    // and aborts it on an exception, as Txn.executeWrite does.

    private static void executeUpdate(DatasetGraph dsg, InputStream body) {
        Txn.executeWrite(dsg, ()->UpdateAction.parseExecute(null, dsg, body, "http://example/", Syntax.syntaxARQ));
    }

    /** The bytes up to cut, then an IOException, as when the client's connection drops. */
    private static InputStream droppedConnection(byte[] bytes, int cut) {
        return new InputStream() {
            private int pos = 0;

            @Override
            public int read() throws IOException {
                byte[] b = new byte[1];
                return read(b, 0, 1) == -1 ? -1 : b[0] & 0xFF;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if ( pos >= cut )
                    throw new IOException("Early EOF");
                int n = Math.min(len, cut - pos);
                System.arraycopy(bytes, pos, b, off, n);
                pos += n;
                return n;
            }
        };
    }

    // A transfer of 50 from Alice to Bob; the connection drops after the second operation.
    @Test
    public void updateConnectionDropped() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Txn.executeWrite(dsg, ()->UpdateAction.parseExecute("""
            INSERT DATA { <http://example/alice> <http://example/balance> 100 .
                          <http://example/bob> <http://example/balance> 0 }
            """, dsg));
        String update = """
            DELETE DATA { <http://example/alice> <http://example/balance> 100 } ;
            INSERT DATA { <http://example/alice> <http://example/balance> 50 } ;
            DELETE DATA { <http://example/bob> <http://example/balance> 0 } ;
            INSERT DATA { <http://example/bob> <http://example/balance> 50 } ;
            """;
        int cut = update.indexOf("DELETE DATA { <http://example/bob>");
        InputStream body = droppedConnection(update.getBytes(StandardCharsets.UTF_8), cut);
        QueryParseException ex = assertThrows(QueryParseException.class, ()->executeUpdate(dsg, body));
        assertTrue(ex.getCause() instanceof IOException, "Cause: " + ex.getCause());
        Txn.executeRead(dsg, ()->{
            assertEquals(2, dsg.getDefaultGraph().size());
            assertTrue(dsg.getDefaultGraph().contains(SSE.parseTriple("(<http://example/alice> <http://example/balance> 100)")));
            assertTrue(dsg.getDefaultGraph().contains(SSE.parseTriple("(<http://example/bob> <http://example/balance> 0)")));
        });
    }

    // 300 operations of 64 bytes; operation 200 has "Bjørn" in Latin-1 (byte F8), which
    // is not valid UTF-8. The decoder reads 8192 bytes at a time, so on main its first
    // read ended after operation 127, and those 128 operations were committed.
    @Test
    public void updateInvalidUTF8() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for ( int i = 0 ; i < 300 ; i++ ) {
            String op = ( i == 200 )
                ? "INSERT DATA { <http://example/s> <http://example/n> \"Bjørn\" } ;"
                : "INSERT DATA { <http://example/s> <http://example/p> " + i + " } ;";
            bytes.writeBytes((op + " ".repeat(63 - op.length()) + "\n").getBytes(StandardCharsets.ISO_8859_1));
        }
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        QueryParseException ex = assertThrows(QueryParseException.class, ()->executeUpdate(dsg, new ByteArrayInputStream(bytes.toByteArray())));
        assertTrue(ex.getCause() instanceof MalformedInputException, "Cause: " + ex.getCause());
        assertEquals(0, Txn.calculateRead(dsg, ()->dsg.getDefaultGraph().size()).intValue());
    }

    // Queries that end on a token the lexer reads past, with no newline after it.
    // The char stream is asked for more input after the end of the input has been reached.

    private static final String[] queriesEndingAtEOF = {
        "SELECT * { ?s ?p ?o } LIMIT 10",
        "SELECT ?x { } ORDER BY ?x",
        "ASK { ?s ?p ?o } # comment"
    };

    @Test
    public void endOfInput() {
        for ( String q : queriesEndingAtEOF )
            QueryFactory.create(q, Syntax.syntaxARQ);
    }

    // Line and column after a multi-line literal that is longer than the initial buffer.

    @Test
    public void positionAfterLongLiteral() {
        String literalLine = "x".repeat(99) + "\n";
        String query = "SELECT * {\n  ?s ?p \"\"\"" + literalLine.repeat(1000) + "\"\"\" .\n  ?a ?b ?lastGood ]\n}\n";
        int lastGood = query.indexOf("?lastGood");
        int bad = query.indexOf(']');
        QueryParseException ex = assertThrows(QueryParseException.class, ()->QueryFactory.create(query, Syntax.syntaxARQ));
        // The exception has the position of the last token parsed; the message has the position of the bad token.
        assertEquals(lineOf(query, lastGood), ex.getLine());
        assertEquals(columnOf(query, lastGood), ex.getColumn());
        String position = "line " + lineOf(query, bad) + ", column " + columnOf(query, bad);
        assertTrue(ex.getMessage().contains(position), "Expected \"" + position + "\" in: " + ex.getMessage());
    }

    private static int lineOf(String text, int index) {
        return (int)text.substring(0, index).chars().filter(ch -> ch == '\n').count() + 1;
    }

    private static int columnOf(String text, int index) {
        return index - text.lastIndexOf('\n', index - 1);
    }
}
