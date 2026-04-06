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

package org.apache.jena.riot.writer.c14n;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.riot.out.NodeFormatterBase;
import org.apache.jena.riot.out.quoted.QuotedStringOutput;
import org.apache.jena.riot.out.quoted.QuotedStringOutputNT;
import org.apache.jena.riot.out.quoted.QuotedURI;

/** Formatting for canonical NTriples/NQuads */
public class NodeFormatter_C14N extends NodeFormatterBase
{
    private final QuotedStringOutput quotedStringProc;
    private final QuotedURI quotedUriProc;

    public NodeFormatter_C14N() { this(CharSpace.UTF8); }

    public NodeFormatter_C14N(CharSpace charSpace) {
        quotedStringProc = new QuotedStringOutputNT(charSpace) {
            @Override
            public void writeStr(AWriter writer, String str) {
                // Only " strings in N-Triples/N-Quads
                writer.print(getQuoteChar());
                // C14N
                EscapeStr_C14N.stringEsc(writer, str, getQuoteChar(), true, charSpace);
                writer.print(getQuoteChar());
            }
        };
        quotedUriProc = new QuotedURI(charSpace);
    }

    @Override
    public void formatURI(AWriter w, String uriStr) {
        quotedUriProc.writeURI(w, uriStr);
    }

    @Override
    public void formatVar(AWriter w, String name) {
        w.print('?');
        EscapeStr.stringEsc(w, name, false);
    }

    @Override
    public void formatBNode(AWriter w, String label) {
        w.print("_:");
        // C14N : Write label as given. Assumes label is valid.
        String lab = label;
        w.print(lab);
    }

    @Override
    public void formatLitString(AWriter w, String lex) {
        writeEscaped(w, lex);
    }

    private void writeEscaped(AWriter w, String lex) {
        quotedStringProc.writeStr(w, lex);
    }

    @Override
    public void formatLitLang(AWriter w, String lex, String langTag) {
        writeEscaped(w, lex);
        w.print('@');
        w.print(langTag);
    }

    @Override
    public void formatLitLangDir(AWriter w, String lex, String langTag, String direction) {
        writeEscaped(w, lex);
        w.print('@');
        w.print(langTag);
        w.print("--");
        w.print(direction);
    }

    @Override
    public void formatLitDT(AWriter w, String lex, String datatypeURI) {
        writeEscaped(w, lex);
        w.print("^^");
        formatURI(w, datatypeURI);
    }
}
