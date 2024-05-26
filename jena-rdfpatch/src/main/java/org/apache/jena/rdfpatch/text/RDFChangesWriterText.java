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

import static org.apache.jena.rdfpatch.changes.PatchCodes.*;

import java.io.OutputStream;

import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.sparql.core.Quad;

/**
 * Write out a changes as a stream of syntax tokens.
 */
public class RDFChangesWriterText implements RDFChanges, AutoCloseable {
    protected TokenWriter tok;

    /** Create a {@code RDFChangesWriter} with standard text output. */
    public static RDFChangesWriterText create(OutputStream out) {
        return new RDFChangesWriterText(TokenWriterText.create(out));
    }

    public RDFChangesWriterText(TokenWriter out) {
        this.tok = out;
    }

    @Override
    public void start() { }

    @Override
    public void finish() { tok.flush(); }

    @Override
    public void header(String field, Node value) {
        tok.startTuple();
        tok.sendWord("H");
        tok.sendWord(field);
        output(value);
        tok.endTuple();
    }

    @Override
    public void close() {
        tok.close();
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        output(ADD_DATA, g, s, p, o);
    }

    private void output(String code, Node g, Node s, Node p, Node o) {
        tok.startTuple();
        outputWord(code);
        output(s);
        output(p);
        output(o);
        if ( g != null && ! Quad.isDefaultGraph(g) )
            output(g);
        tok.endTuple();
    }

    private void output(Node node) {
        tok.sendNode(node);
    }

    private void outputWord(String code) {
        tok.sendWord(code);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        output(DEL_DATA, g, s, p, o);
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        tok.startTuple();
        outputWord(ADD_PREFIX);
        tok.sendString(prefix);
        tok.sendString(uriStr);
        if ( gn != null )
            tok.sendNode(gn);
        tok.endTuple();
    }

    @Override
    public void deletePrefix(Node gn, String prefix) {
        tok.startTuple();
        outputWord(DEL_PREFIX);
        tok.sendString(prefix);
        if ( gn != null )
            tok.sendNode(gn);
        tok.endTuple();
    }

    private void oneline(String code) {
        tok.startTuple();
        tok.sendWord(code);
        tok.endTuple();
    }

    @Override
    public void txnBegin() {
        oneline(TXN_BEGIN);
    }

    @Override
    public void txnCommit() {
        oneline(TXN_COMMIT);
        tok.flush();
    }

    @Override
    public void txnAbort() {
        oneline(TXN_ABORT);
        tok.flush();
    }

    @Override
    public void segment() {
        oneline(SEGMENT);
    }
}
