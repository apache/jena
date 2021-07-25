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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetReader;
import org.apache.jena.riot.resultset.ResultSetReaderFactory;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.resultset.TSVInputIterator;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSetReaderTSV implements ResultSetReader {

    private static Logger log = LoggerFactory.getLogger(ResultSetReaderTSV.class);

    public static final ResultSetReaderFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_TSV ) )
            throw new ResultSetException("ResulResultSetReadertSet for TSV asked for a "+lang);
        return new ResultSetReaderTSV();
    };

    static Pattern pattern = Pattern.compile("\t");

    private ResultSetReaderTSV() {}

    @Override
    public SPARQLResult readAny(InputStream in, Context context) {
        ResultSet resultSet = resultSetFromTSV(in);
        return new SPARQLResult(resultSet);
    }

    @Override
    public ResultSet read(InputStream in, Context context) {
        return resultSetFromTSV(in);
    }

    /**
     * Reads SPARQL Results from TSV format into a {@link ResultSet} instance
     * @param in Input Stream
     */
    public static ResultSet resultSetFromTSV(InputStream in) {
        BufferedReader reader = IO.asBufferedUTF8(in);
        List<Var> vars = new ArrayList<>();
        List<String> varNames = new ArrayList<>();

        String str = null;
        try {

            // Here we try to parse only the Header Row
            str = reader.readLine();
            if ( str == null )
                throw new ARQException("TSV Results malformed, input is empty (no header row)");
            if ( !str.isEmpty() ) {
                String[] tokens = pattern.split(str, -1);
                for ( String token : tokens ) {
                    Node v;
                    try {
                        v = NodeFactoryExtra.parseNode(token);
                        if ( v == null || !v.isVariable() )
                            throw new ResultSetException("TSV Results malformed, not a variable: " + token);
                    }
                    catch (RiotException ex) {
                        throw new ResultSetException("TSV Results malformed, variable names must begin with a ? in the header: " + token);
                    }

                    Var var = Var.alloc(v);
                    vars.add(var);
                    varNames.add(var.getName());
                }
            }
        }
        catch (IOException ex) {
            throw new ARQException(ex);
        }

        // Generate an instance of ResultSetStream using TSVInputIterator
        // This will parse actual result rows as needed thus minimising memory usage
        return ResultSetStream.create(varNames, null, new TSVInputIterator(reader, vars));
    }

    /**
     * Reads SPARQL Boolean result from TSV
     * @param in Input Stream
     * @return boolean
     */
    public static boolean booleanFromTSV(InputStream in) {
        BufferedReader reader = IO.asBufferedUTF8(in);
        String str = null;
        try {
            // First try to parse the header
            str = reader.readLine();
            if ( str == null )
                throw new ARQException("TSV Boolean Results malformed, input is empty");
            str = str.trim(); // Remove extraneous white space

            // Expect a header row with single ?_askResult variable
            if ( !str.equals("?_askResult") )
                throw new ARQException("TSV Boolean Results malformed, did not get expected ?_askResult header row");

            // end header.

            // Then try to parse the boolean result
            str = reader.readLine();
            if ( str == null )
                throw new ARQException("TSV Boolean Results malformed, unexpected end of input after header row");
            str = str.trim();

            if ( str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") ) {
                return true;
            } else if ( str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no") ) {
                return false;
            } else {
                throw new ARQException("TSV Boolean Results malformed, expected one of - true yes false no - but got " + str);
            }
        }
        catch (IOException ex) {
            throw new ARQException(ex);
        }
    }
}
