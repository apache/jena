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

package org.apache.jena.sparql.resultset;

import java.io.IOException ;
import java.io.OutputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.riot.out.NodeFormatter ;
import org.apache.jena.riot.out.NodeFormatterTTL ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;

/**
 * Tab Separated Values.
 * 
 * First row is variable names (with ?).
 * Subsequent rows are RDF terms, written Turtle style.
 */
public class TSVOutput extends OutputBase
{
    // Tab Separated Values
    // http://www.iana.org/assignments/media-types/text/tab-separated-values 
    
    static String NL   = "\n" ;
    static String SEP  = "\t" ;
    
    @Override
    public void format(OutputStream out, ResultSet resultSet) {
        NodeFormatter formatter = createNodeFormatter();

        AWriter w = IO.wrapUTF8(out);

        String sep = null;
        List<String> varNames = resultSet.getResultVars();
        List<Var> vars = new ArrayList<>(varNames.size());

        // writes the variables on the first line
        for ( String v : varNames ) {
            if ( sep != null )
                w.write(sep);
            else
                sep = SEP;
            Var var = Var.alloc(v);
            w.write(var.toString());
            vars.add(var);
        }
        w.write(NL);

        // writes one binding by line
        for ( ; resultSet.hasNext() ; ) {
            sep = null;
            Binding b = resultSet.nextBinding();

            for ( Var v : vars ) {
                if ( sep != null )
                    w.write(sep);
                sep = SEP;

                Node n = b.get(v);
                if ( n != null ) {
                    // This will not include a raw tab.
                    formatter.format(w, n);
                }
            }
            w.write(NL);
        }

        w.flush();
    }

    protected NodeFormatter createNodeFormatter() {
        // Use a Turtle formatter to format terms
        return new NodeFormatterTTL(null, null);
    }

    static final byte[] headerBytes = StrUtils.asUTF8bytes("?_askResult" + NL);
    static final byte[] yesBytes    = StrUtils.asUTF8bytes("true");
    static final byte[] noBytes     = StrUtils.asUTF8bytes("false");
    static final byte[] NLBytes     = StrUtils.asUTF8bytes(NL);

    @Override
    public void format(OutputStream out, boolean booleanResult) {
        try {
            out.write(headerBytes);
            if ( booleanResult )
                out.write(yesBytes);
            else
                out.write(noBytes);
            out.write(NLBytes);
        }
        catch (IOException ex) {
            throw new ARQException(ex);
        }
    }
}
