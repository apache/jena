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

package org.apache.jena.riot.rowset.rw;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetWriter;
import org.apache.jena.riot.rowset.RowSetWriterFactory;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;

public class RowSetWriterCSV implements RowSetWriter {

    public static RowSetWriterFactory factory = lang -> {
        if ( !Objects.equals(lang, ResultSetLang.RS_CSV) )
            throw new ResultSetException("RowSetWriter for CSV asked for a " + lang);
        return new RowSetWriterCSV();
    };

    static final String NL          = "\r\n";
    static final String headerBytes = "_askResult" + NL;
    static final String yesString   = "true";
    static final String noString    = "false";

    private RowSetWriterCSV() {}

    @Override
    public void write(OutputStream out, RowSet resultSet, Context context) {
        output(IO.wrapUTF8(out), resultSet, context);
    }

    @Override
    public void write(Writer out, RowSet resultSet, Context context) {
        output(IO.wrap(out), resultSet, context);
    }

    @Override
    public void write(OutputStream out, boolean result, Context context) {
        output(IO.wrapUTF8(out), result);
    }

    private static void output(AWriter out, boolean booleanResult) {
        try {
            out.write(headerBytes);
            if ( booleanResult )
                out.write(yesString);
            else
                out.write(noString);
            out.write(NL);
        } finally {
            out.flush();
        }
    }

    private static void output(AWriter out, RowSet rowSet, Context context) {
        try {
            boolean outputGraphBNodeLabels = (context != null) && context.isTrue(ARQ.outputGraphBNodeLabels);
            NodeToLabel bnodes = outputGraphBNodeLabels
                    ? SyntaxLabels.createNodeToLabelAsGiven()
                    : SyntaxLabels.createNodeToLabel();

            String sep = null;
            List<Var> vars = rowSet.getResultVars();

            // Convert to Vars and output the header line.
            for ( Var var : vars ) {
                String v = var.getVarName();
                if ( sep != null )
                    out.write(sep);
                else
                    sep = ",";
                out.write(csvSafe(v));
            }
            out.write(NL);

            // Data output
            for ( ; rowSet.hasNext() ; ) {
                sep = null;
                Binding b = rowSet.next();

                for ( Var v : vars ) {
                    if ( sep != null )
                        out.write(sep);
                    sep = ",";

                    Node n = b.get(v);
                    if ( n != null )
                        output(out, n, bnodes);
                }
                out.write(NL);
            }
        } finally { out.flush(); }
    }

    private static void output(AWriter w, Node n, NodeToLabel bnodes) {
        // String str = FmtUtils.stringForNode(n) ;
        String str = "?";
        if ( n.isLiteral() )
            str = n.getLiteralLexicalForm();
        else if ( n.isURI() )
            str = n.getURI();
        else if ( n.isBlank() ) {
            str = bnodes.get(null, n);
            // Comes with leading "_:"
            str = str.substring(2);
        }

        str = csvSafe(str);
        w.write(str);
        w.flush();
    }

    static protected String csvSafe(String str) {
        // Apparently, there are CSV parsers that only accept "" as an escaped quote
        // if inside a "..."
        if ( str.contains("\"") || str.contains(",") || str.contains("\r") || str.contains("\n") )
            str = "\"" + str.replaceAll("\"", "\"\"") + "\"";
        else if ( str.isEmpty() )
            // Return the quoted empty string.
            str = "\"\"";
        return str;
    }
}
