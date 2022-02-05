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

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;

public class ResultSetWriterTSV implements ResultSetWriter {

    public static ResultSetWriterFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_TSV ) )
            throw new ResultSetException("ResultSetWriter for TSV asked for a "+lang);
        return new ResultSetWriterTSV();
    };

    private ResultSetWriterTSV() {}

    @Override
    public void write(OutputStream out, ResultSet resultSet, Context context) {
        output(IO.wrapUTF8(out), resultSet);
    }

    @Override
    public void write(Writer out, ResultSet resultSet, Context context) {
        output(IO.wrap(out), resultSet);
    }

    @Override
    public void write(OutputStream out, boolean result, Context context) {
        output(IO.wrapUTF8(out), result);
    }

    private static void output(AWriter out, boolean booleanResult) {
            out.write(headerBytes);
            if ( booleanResult )
                out.write(yesBytes);
            else
                out.write(noBytes);
            out.write(NL);
            out.flush();
    }

    private static void output(AWriter out, ResultSet resultSet) {
        try {
            NodeFormatter formatter = createNodeFormatter();
            String sep = null;
            List<String> varNames = resultSet.getResultVars();
            List<Var> vars = new ArrayList<>(varNames.size());

            // writes the variables on the first line
            for ( String v : varNames ) {
                if ( sep != null )
                    out.write(sep);
                else
                    sep = SEP;
                Var var = Var.alloc(v);
                out.write(var.toString());
                vars.add(var);
            }
            out.write(NL);

            // writes one binding by line
            for ( ; resultSet.hasNext() ; ) {
                sep = null;
                Binding b = resultSet.nextBinding();

                for ( Var v : vars ) {
                    if ( sep != null )
                        out.write(sep);
                    sep = SEP;

                    Node n = b.get(v);
                    if ( n != null ) {
                        // This will not include a raw tab.
                        formatter.format(out, n);
                    }
                }
                out.write(NL);
            }
        } finally { out.flush();}
    }

    protected static NodeFormatter createNodeFormatter() {
        // Use a Turtle formatter to format terms
        return new NodeFormatterTTL(null, null);
    }

    private static final String NL   = "\n" ;
    private static final String SEP  = "\t" ;
            private static final String headerBytes = "?_askResult" + NL;
    private static final String yesBytes    = "true";
    private static final String noBytes     = "false";
}
