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

package org.apache.jena.riot.resultset.rw;

import static org.apache.jena.riot.resultset.rw.JSONResultsKW.*;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.json.io.JSWriter;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;

/** Write results in {@code application/sparql-results+json} format. */
public class ResultSetWriterJSON implements ResultSetWriter {

    public static ResultSetWriterFactory factory = lang->{
        if (!Objects.equals(lang, ResultSetLang.SPARQLResultSetJSON ) )
            throw new ResultSetException("ResultSetWriter for JSON asked for a "+lang);
        return new ResultSetWriterJSON();
    };

    private ResultSetWriterJSON() { }

    // We use an inner object for writing of one result set so that the
    // ResultSetWriter has no per-write state variables.

    @Override
    public void write(Writer out, ResultSet resultSet, Context context) {
        throw new UnsupportedOperationException("Writing JSON results to a java.io.Writer. Use an OutputStream.") ;
    }

    @Override
    public void write(OutputStream outStream, boolean result, Context context) {
        try {
            JSWriter out = new JSWriter(outStream);
            out.startOutput();
            out.startObject();
            out.key(kHead);
            out.startObject();
            out.finishObject();
            out.pair(kBoolean, result);
            out.finishObject();
            out.finishOutput();
        } finally {
            IO.flush(outStream);
        }
    }

    @Override
    public void write(OutputStream outStream, ResultSet resultSet, Context context) {
        IndentedWriter out = new IndentedWriter(outStream);
        try {
            ResultSetWriterTableJSON x = new ResultSetWriterTableJSON(out, context);
            x.write(resultSet);
        }
        finally {
            IO.flush(out);
        }
    }

    // Create once per write call.
    // This holds the state of the writing of one ResultSet.
    static class ResultSetWriterTableJSON {
        private final NodeToLabel    labels;
        private final  IndentedWriter out;
        /** Control whether the type/literal/fileds all go on one line. */
        private static final boolean MultiLineValues   = false;
        /** Control whether variables in header are one per line (minor). */
        private static final boolean MultiLineVarNames = false;

        private static final int MainIndent = 2 ;
        private static final int VarIndent = 2 ;
        private static final int OuterIndent = Math.min(2, MainIndent);

        private ResultSetWriterTableJSON(OutputStream outStream, Context context) {
            this(new IndentedWriter(outStream), context);
        }

        private ResultSetWriterTableJSON(IndentedWriter indentedOut, Context context) {
            out = indentedOut;
            out.setUnitIndent(MainIndent);

            boolean outputGraphBNodeLabels = (context != null) && context.isTrue(ARQ.outputGraphBNodeLabels);
            labels = outputGraphBNodeLabels
                ? SyntaxLabels.createNodeToLabelAsGiven()
                    : SyntaxLabels.createNodeToLabel();
        }

        private void write(ResultSet resultSet) {
            print(out, "{");
            out.incIndent(OuterIndent);
            //incIndent(out);
            writeHeader(resultSet);
            println(out, " ,");
            writeRows(out, resultSet);
            out.decIndent(OuterIndent);
            println(out, "}");      // top level {}
        }


        private void writeRows(IndentedWriter out2, ResultSet resultSet) {
            println(out, quoteName(kResults), ": {");
            incIndent(out);
            println(out, quoteName(kBindings), ": [");
            incIndent(out);

            boolean firstRow = true;
            for ( ; resultSet.hasNext() ; ) {
                Binding binding = resultSet.nextBinding();
                if ( !firstRow )
                    println(out, " ,");
                writeRow(out, resultSet, binding);
                firstRow = false;
            }
            println(out);
            decIndent(out);
            println(out, "]");      // bindings
            decIndent(out);
            println(out, "}");      // results
            //decIndent(out);
        }

        // Write policy - write objects, leave point of output just after the closing "}"

        private void writeHeader(ResultSet rs) {
            println(out, quoteName(kHead),": {");
            incIndent(out);
            writeHeaderLink(out, rs);
            writeHeaderVars(out, rs);
            decIndent(out);
            print(out, "}");
        }

        private static void writeHeaderLink(IndentedWriter out, ResultSet rs) {
            // ---- link
            // out.println("\"link\": [] ,") ;
        }

        //  "var": [  ... ]
        private static void writeHeaderVars(IndentedWriter out, ResultSet rs) {
            // On one line.
            print(out, quoteName(kVars), ": [ ");
            if ( MultiLineVarNames )
                println(out);
            incIndent(out);
            for ( Iterator<String> iter = rs.getResultVars().iterator() ; iter.hasNext() ; ) {
                String varname = iter.next();
                print(out, "\"", varname, "\"");
                if ( iter.hasNext() )
                    print(out, " , ");
                if ( MultiLineVarNames )
                    println(out);
            }
            decIndent(out);
            println(out, " ]");
        }

        private void writeRow(IndentedWriter out, ResultSet resultSet, Binding binding) {
            print(out, "{ ");
            //incIndent(out);
            out.incIndent(VarIndent);
            boolean firstInRow = true;
            // Print in the order seen in the header.
            for ( Iterator<String> iter = resultSet.getResultVars().iterator() ; iter.hasNext() ; ) {
                Var var = Var.alloc(iter.next());
                Node value = binding.get(var);
                if ( value == null )
                    continue;
                if ( ! firstInRow )
                    print(out, " ,");
                writeVarValue(out, var, value, firstInRow);
                firstInRow = false;
            }

            if ( false ) {
                // Print "missing" variables.
                // If the header is right, this should not be necessary.
                for ( Iterator<Var> vars = binding.vars() ; vars.hasNext() ; ) {
                    Var var = vars.next();
                    if ( ! resultSet.getResultVars().contains(var.getName()) ) {
                        Node value = binding.get(var);
                        if ( ! firstInRow )
                            println(out, " ,");
                        writeVarValue(out, var, value, firstInRow);
                        firstInRow = false;
                    }
                }
            }
            println(out);
            out.decIndent(VarIndent);
            //decIndent(out);
            print(out, "}");
        }

        /* Write one JSON member (key, value pair in a JSON object)
         * i.e.    "var": { ... term ... }
         * Separating comma handled by caller writeRow
         */
        private void writeVarValue(IndentedWriter out, Var var, Node value, boolean firstInRow) {
            if ( value == null )
                // Skip if no value.
                return;
            println(out);
            // Do not use quoteName - varName may not be JSON-safe as a bare name.
            print(out, quote(var.getVarName()), ": ");
            writeValue(out, value, MultiLineValues);
            // No newline - allow for " ,"
        }

        private void writeValue(IndentedWriter out, Node value, boolean multiLine) {
            if ( multiLine || value.isNodeTriple() )
                println(out, "{");
            else
                print(out, "{ ");
            incIndent(out);
            // Explicit unbound
            // if ( value == null )
            //     writeValueUnbound(out) ;
            // else
            if ( value.isLiteral() )
                writeValueLiteral(out, value, multiLine);
            else if ( value.isURI() )
                writeValueURI(out, value, multiLine);
            else if ( value.isBlank() )
                writeValueBlankNode(out, value, multiLine);
            else if ( value.isNodeTriple() )
                writeValueNodeTriple(out, value, multiLine);
            else if ( value.isNodeGraph() )
                writeValueNodeGraph(out, value, multiLine);
            else
                Log.warn(ResultSetWriterJSON.class, "Unknown RDFNode type in result set: " + value.getClass());
            if ( multiLine || value.isNodeTriple()) // OR triple
                println(out) ;
            else
                print(out, " ");
            decIndent(out);
            print(out, "}");
        }

        // On entry to writeTerm : ready to start the "type" field.

        private void writeValueUnbound(IndentedWriter out, boolean multiLine) {
            print(out, quoteName(kType), ": ", quote(kUnbound), " , ") ;
            if ( MultiLineValues )
                println(out) ;
            print(out, quoteName(kValue), ": null") ;
        }

        private void writeValueLiteral(IndentedWriter out, Node literal, boolean multiLine) {
            String datatype = literal.getLiteralDatatypeURI();
            String lang = literal.getLiteralLanguage();

            if ( Util.isSimpleString(literal) || Util.isLangString(literal) ) {
                print(out, quoteName(kType), ": ", quote(kLiteral), " , ");
                if ( multiLine )
                    println(out);

                if ( lang != null && !lang.equals("") ) {
                    print(out, quoteName(kXmlLang), ": ", quote(lang), " , ");
                    if ( multiLine )
                        println(out);
                }
            } else {
                print(out, quoteName(kType), ": ", quote(kLiteral), " , ");
                if ( MultiLineValues )
                    println(out);

                print(out, quoteName(kDatatype), ": ", quote(datatype), " , ");
                if ( multiLine )
                    println(out);
            }

            print(out, quoteName(kValue), ": ", quote(literal.getLiteralLexicalForm()));
        }

        private void writeValueBlankNode(IndentedWriter out, Node resource, boolean multiLine) {
            String label = labels.get(null, resource);
            // Comes with leading "_:"
            label = label.substring(2);

            print(out, quoteName(kType), ": ", quote(kBnode), " , ");
            if ( multiLine )
                println(out);
            print(out, quoteName(kValue), ": ", quote(label));
        }

        private void writeValueURI(IndentedWriter out, Node resource, boolean multiLine) {
            print(out, quoteName(kType), ": ", quote(kUri), " , ");
            if ( multiLine )
                println(out);
            print(out, quoteName(kValue), ": ", quote(resource.getURI()));
            return;
        }

        private void writeValueNodeTriple(IndentedWriter out, Node value, boolean multiLine) {
            Triple triple = Node_Triple.triple(value);
            print(out, quoteName(kType), ": ", quote(kTriple), " , ");
            println(out);

            //if ( multiLineValues )
            // println(out);
            print(out, quoteName(kValue), ": ");

            // Allow for different multiline choice for triple components
            boolean multiLineInnerValue = multiLine;

            // Start of triple object.
            // ----
            println(out, "{");
            incIndent(out);

            // ---
            print(out, quoteName(kSubject), ": ");
            print(out, "  ");

            writeValue(out, triple.getSubject(), multiLineInnerValue);
            println(out, " ,");

            print(out, quoteName(kPredicate), ": ");
            writeValue(out, triple.getPredicate(), multiLineInnerValue);
            println(out, " ,");

            print(out, quoteName(kObject), ": ");
            print(out, "   ");
            writeValue(out, triple.getObject(), multiLineInnerValue);
            // End of triple object.
            decIndent(out);
            println(out);
            print(out, "}");
            // ----
            return;
        }

        private void writeValueNodeGraph(IndentedWriter out, Node value, boolean multiLine) {
            throw new UnsupportedOperationException("Graph terms");
        }

        private static String quote(String string) {
            // Scope for efficiency improvement.
            return JSWriter.outputQuotedString(string);
        }

        // Quote a name (known to be JSON-safe)
        // Never the RHS of a member entry (for example "false")
        // Some (the Java JSON code for one) JSON parsers accept an unquoted
        // string as a name of a name/value pair.

        private static String quoteName(String string) {
            // All calls to quoteName are builtin keywords which are already safe.
            // but need the "" added.
            //return "\""+string+"\"";
            return quote(string);
        }

        // Intercept all operations - development assistance.

        private static void incIndent(IndentedWriter out) {
            out.incIndent();
        }

        private static void decIndent(IndentedWriter out) {
            out.decIndent();
        }

        private static void print(IndentedWriter out, String... strings) {
            for ( String s : strings )
                out.print(s);
        }

        private static void println(IndentedWriter out, String... strings) {
            print(out, strings);
            out.println();
        }
    }
}
