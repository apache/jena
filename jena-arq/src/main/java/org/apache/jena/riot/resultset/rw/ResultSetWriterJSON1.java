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
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.resultset.ResultSetApply;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.ResultSetProcessor;
import org.apache.jena.sparql.util.Context;

public class ResultSetWriterJSON1 implements ResultSetWriter {

    public static ResultSetWriterFactory factory = lang->{
        if (!Objects.equals(lang, ResultSetLang.SPARQLResultSetJSON ) )
            throw new ResultSetException("ResultSetWriter for JSON asked for a "+lang); 
        return new ResultSetWriterJSON1(); 
    };
    
    private ResultSetWriterJSON1() {}
    
    @Override
    public void write(Writer out, ResultSet resultSet, Context context) {
        throw new UnsupportedOperationException("Writing JSON results to a java.io.Writer. Use an OutputStream.") ;
    }

    @Override
    public void write(OutputStream outStream, boolean result, Context context) {
        JSWriter out = new JSWriter(outStream);
        out.startOutput();
        out.startObject();
        out.key(kHead);
        out.startObject();
        out.finishObject();
        out.pair(kBoolean, result);
        out.finishObject();
        out.finishOutput();
        IO.flush(outStream);
    }
    
    @Override
    public void write(OutputStream out, ResultSet resultSet, Context context) {
        JSONOutputResultSet jsonOut = new JSONOutputResultSet(out, context);
        ResultSetApply a = new ResultSetApply(resultSet, jsonOut);
        a.apply();
    }

    private static class JSONOutputResultSet implements ResultSetProcessor {
        private static final boolean multiLineValues   = false;
        private static final boolean multiLineVarNames = false;

        private final IndentedWriter out;
        private final NodeToLabel    labels;

        private JSONOutputResultSet(OutputStream outStream, Context context) {
            this(new IndentedWriter(outStream), context);
        }

        private JSONOutputResultSet(IndentedWriter indentedOut, Context context) {
            out = indentedOut;
            boolean outputGraphBNodeLabels = (context != null) && context.isTrue(ARQ.outputGraphBNodeLabels);
            labels = outputGraphBNodeLabels
                ? SyntaxLabels.createNodeToLabelAsGiven()
                : SyntaxLabels.createNodeToLabel();
        }

        @Override
        public void start(ResultSet rs) {
            println("{");
            out.incIndent();
            doHead(rs);
            println(quoteName(kResults), ": {");
            out.incIndent();
            println(quoteName(kBindings), ": [");
            out.incIndent();
            firstSolution = true;
        }

        @Override
        public void finish(ResultSet rs) {
            // Close last binding.
            out.println();

            out.decIndent();    // bindings
            println("]");
            out.decIndent();
            println("}");       // results
            out.decIndent();
            println("}");       // top level {}
            out.flush();
        }

        private void doHead(ResultSet rs) {
            println(quoteName(kHead),": {");
            out.incIndent();
            doLink(rs);
            doVars(rs);
            out.decIndent();
            println("} ,");
        }

        private void doLink(ResultSet rs) {
            // ---- link
            // out.println("\"link\": []") ;
        }

        private void doVars(ResultSet rs) {
            // On one line.
            print(quoteName(kVars), ": [ ");
            if ( multiLineVarNames )
                out.println();
            out.incIndent();
            for ( Iterator<String> iter = rs.getResultVars().iterator() ; iter.hasNext() ; ) {
                String varname = iter.next();
                print("\"", varname, "\"");
                if ( multiLineVarNames )
                    println();
                if ( iter.hasNext() )
                    print(" , ");
            }
            println(" ]");
            out.decIndent();
        }

        boolean firstSolution          = true;
        boolean firstBindingInSolution = true;

        // NB assumes are on end of previous line.
        @Override
        public void start(QuerySolution qs) {
            if ( !firstSolution )
                println(" ,");
            firstSolution = false;
            println("{");
            out.incIndent();
            firstBindingInSolution = true;
        }

        @Override
        public void finish(QuerySolution qs) {
            println(); // Finish last binding
            out.decIndent();
            print("}"); // NB No newline
        }

        @Override
        public void binding(String varName, RDFNode value) {
            if ( value == null )
                return;

            if ( !firstBindingInSolution )
                println(" ,");
            firstBindingInSolution = false;

            // Do not use quoteName - varName may not be JSON-safe as a bare name.
            print(quote(varName), ": { ");
            if ( multiLineValues )
                out.println();

            out.incIndent();
            // Old, explicit unbound
            // if ( value == null )
            //     printUnbound() ;
            // else
            if ( value.isLiteral() )
                printLiteral((Literal)value);
            else if ( value.isResource() )
                printResource((Resource)value);
            else
                Log.warn(this, "Unknown RDFNode type in result set: " + value.getClass());
            out.decIndent();

            if ( !multiLineValues )
                print(" ");
            print("}"); // NB No newline
        }

         private void printUnbound() {
             print(quoteName(kType), ": ", quote(kUnbound), " , ") ;
             if ( multiLineValues ) 
                 println() ;
             print(quoteName(kValue), ": null") ;
             if ( multiLineValues )
                 println() ;
         }

        private void printLiteral(Literal literal) {
            String datatype = literal.getDatatypeURI();
            String lang = literal.getLanguage();

            if ( Util.isSimpleString(literal) || Util.isLangString(literal) ) {
                print(quoteName(kType), ": ", quote(kLiteral), " , ");
                if ( multiLineValues )
                    println();

                if ( lang != null && !lang.equals("") ) {
                    print(quoteName(kXmlLang), ": ", quote(lang), " , ");
                    if ( multiLineValues )
                        println();
                }
            } else {
                print(quoteName(kType), ": ", quote(kLiteral), " , ");
                if ( multiLineValues )
                    println();

                print(quoteName(kDatatype), ": ", quote(datatype), " , ");
                if ( multiLineValues )
                    println();
            }

            print(quoteName(kValue), ": ", quote(literal.getLexicalForm()));
            if ( multiLineValues )
                println();
        }

        private void printResource(Resource resource) {
            if ( resource.isAnon() ) {
                String label = labels.get(null, resource.asNode());
                // Comes with leading "_:"
                label = label.substring(2);

                print(quoteName(kType), ": ", quote(kBnode), " , ");
                if ( multiLineValues )
                    out.println();

                print(quoteName(kValue), ": ", quote(label));

                if ( multiLineValues )
                    println();
            } else {
                print(quoteName(kType), ": ", quote(kUri), " , ");
                if ( multiLineValues )
                    println();
                print(quoteName(kValue), ": ", quote(resource.getURI()));
                if ( multiLineValues )
                    println();
                return;
            }
        }
        
        private void print(String... strings) {
            for ( String s : strings )
                out.print(s);
        }

        private void println(String... strings) {
            print(strings);
            out.println();
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
    }
}
