/**
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

import static org.apache.jena.atlas.lib.Chars.CH_QUOTE1;
import static org.apache.jena.atlas.lib.Chars.CH_QUOTE2;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.out.quoted.QuotedStringOutput;
import org.apache.jena.riot.out.quoted.QuotedStringOutputTTL_MultiLine;
import org.apache.jena.riot.system.PrefixMap;

/**
 * Node formatter for pretty-printed Turtle. This {@link NodeFormatter} switches
 * between " and ' quotes to avoid escapes. This code writes multiline literals with
 * """ or '''.
 */
public class NodeFormatterTTL_MultiLine extends NodeFormatterTTL {
    /// For '-quoted and "-quoted literals
    private final QuotedStringOutput escapeProc2 = new QuotedStringOutputTTL_MultiLine(CH_QUOTE2);
    private final QuotedStringOutput escapeProc1 = new QuotedStringOutputTTL_MultiLine(CH_QUOTE1);

    private boolean writeAsMultiLine(AWriter w, String lex) {
        return (lex.contains("\n"));
    }

    public NodeFormatterTTL_MultiLine(String baseIRI, PrefixMap prefixMap) {
        super(baseIRI, prefixMap);
    }

    public NodeFormatterTTL_MultiLine(String baseIRI, PrefixMap prefixMap, NodeToLabel nodeToLabel) {
        super(baseIRI, prefixMap, nodeToLabel);
    }

    private Runnable noop = () -> {};

    @Override
    public void formatLitString(AWriter w, String lex) {
        if ( !writeAsMultiLine(w, lex) ) {
            // super.formatLitString(w, lex);
            writeLexicalSingleLine(w, lex, noop); // switching quotes as needed.
            return;
        }
        writeLexicalMultiLine(w, lex, noop);
    }

    @Override
    public void formatLitLang(AWriter w, String lex, String langTag) {
        Runnable runnable = () -> {
            w.print('@');
            w.print(langTag);
        };

        if ( !writeAsMultiLine(w, lex) ) {
            // super Not OK.
            // super.formatLitLang(w, lex, langTag);
            writeLexicalSingleLine(w, lex, runnable);
            return;
        }
        writeLexicalMultiLine(w, lex, runnable);
    }

    @Override
    protected void writeLiteralWithDT(AWriter w, String lex, String datatypeURI) {
        Runnable runnable = () -> {
            w.print("^^");
            formatURI(w, datatypeURI);
        };
        if ( !writeAsMultiLine(w, lex) ) {
            writeLexicalSingleLine(w, lex, runnable);
            return;
        }
        writeLexicalMultiLine(w, lex, runnable);
    }

    private QuotedStringOutput chooseEscapeProcessor(String str) {
        QuotedStringOutput proc = escapeProc2;
        if ( str.indexOf(CH_QUOTE2) >= 0 && str.indexOf(CH_QUOTE1) < 0 )
            // Contains " but not ' so print using '-quotes.
            proc = escapeProc1;
        return proc;
    }

    /** Output a string and run the Runnable at the same indentation level */
    private void writeLexicalSingleLine(AWriter writer, String str, Runnable action) {
        QuotedStringOutput proc = chooseEscapeProcessor(str);
        proc.writeStr(writer, str);
        if ( action != null )
            action.run();
    }

    /** Output a string and run the Runnable at the same indentation level */
    private void writeLexicalMultiLine(AWriter writer, String str, Runnable action) {
        QuotedStringOutput escapeProc = chooseEscapeProcessor(str);
        int indent = -1;
        IndentedWriter iw = null;
        if ( writer instanceof IndentedWriter ) {
            iw = (IndentedWriter)writer;
            iw.pad();
            indent = iw.getAbsoluteIndent();
            iw.setAbsoluteIndent(0);
        }
        escapeProc.writeStrMultiLine(writer, str);

        if ( action != null )
            action.run();
        if ( iw != null && indent >= 0 )
            iw.setAbsoluteIndent(indent);
    }
}
