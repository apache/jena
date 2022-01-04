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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.jena.atlas.csv.CSVParser;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetReader;
import org.apache.jena.riot.rowset.RowSetReaderFactory;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetStream;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowSetReaderCSV implements RowSetReader {

    private static Logger log = LoggerFactory.getLogger(RowSetReaderCSV.class);

    public static final RowSetReaderFactory factory = lang -> {
        if ( !Objects.equals(lang, ResultSetLang.RS_CSV) )
            throw new ResultSetException("RowSetReaderCSV for CSV asked for a " + lang);
        return new RowSetReaderCSV();
    };

    private RowSetReaderCSV() {}

    @Override
    public QueryExecResult readAny(InputStream in, Context context) {
        return csvResult(in);
    }

    @Override
    public RowSet read(InputStream in, Context context) {
        return resultSetFromCSV(in);
    }

    /** Expect either a RowSet or a boolean */
    private static QueryExecResult csvResult(InputStream in) {
        CSVParser parser = CSVParser.create(in);
        final List<Var> vars = vars(parser);
        if ( isBooleanResult(vars) ) {
            boolean booleanResult = booleanFromCSV(parser);
            return new QueryExecResult(booleanResult);
        }
        RowSet resultSet = resultSetFromCSV(vars, parser);
        return new QueryExecResult(resultSet);
    }

    /** Expect a RowSet */
    private static RowSet resultSetFromCSV(InputStream in) {
        CSVParser parser = CSVParser.create(in);
        final List<Var> vars = vars(parser);
        return resultSetFromCSV(vars, parser);
    }

    /** Read RowSet after header */
    private static RowSet resultSetFromCSV(List<Var> vars, CSVParser parser) {
        BindingBuilder builder = Binding.builder();
        Function<List<String>, Binding> transform = new Function<List<String>, Binding>() {
            private int count = 1;
            @Override
            public Binding apply(List<String> row) {
                if ( row.size() != vars.size() )
                    FmtLog.warn(log, "Row %d: Length=%d: expected=%d", count, row.size(), vars.size());
                builder.reset();
                // Check.
                for ( int i = 0 ; i < vars.size() ; i++ ) {
                    Var v = vars.get(i);
                    String field = (i < row.size()) ? row.get(i) : "";
                    Node n = NodeFactory.createLiteral(field);
                    builder.add(v, n);
                }
                count++;
                return builder.build();
            }
        };
        Iterator<Binding> bindings = Iter.map(parser.iterator(), transform);
        return RowSetStream.create(vars, bindings);
    }

    private static List<Var> vars(CSVParser parser) {
        final List<Var> vars = new ArrayList<>();
        List<String> varNames = parser.parse1();
        if ( varNames == null )
            throw new ARQException("SPARQL CSV Results malformed, input is empty");
        for ( String vn : varNames ) {
            vars.add(Var.alloc(vn));
        }
        return vars;
    }

    private static boolean isBooleanResult(List<Var> vars) {
        if ( vars.size() != 1 ) {
            return false;
            // throw new ARQException("CSV Boolean Results malformed: variables line='"+vars+"'") ;
        }
        if ( !vars.get(0).getName().equals("_askResult") ) {
            return false;
            // FmtLog.warn(log, "Boolean result variable is '%s', not '_askResult'",
            // vars.get(0).getName()) ;
        }
        return true;
    }

    /** Read boolean after header */
    private static boolean booleanFromCSV(CSVParser parser) {

        List<String> line = parser.parse1();
        if ( line.size() != 1 ) {
            throw new ARQException("CSV Boolean Results malformed: data line='" + line + "'");
        }
        String str = line.get(0);
        boolean b;
        if ( str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") )
            b = true;
        else if ( str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no") )
            b = false;
        else {
            throw new ARQException("CSV Boolean Results malformed, expected one of - true yes false no - but got " + str);
        }

        List<String> line2 = parser.parse1();
        if ( line2 != null ) {
            FmtLog.warn(log, "Extra rows: first is " + line2);
        }
        return b;
    }
}
