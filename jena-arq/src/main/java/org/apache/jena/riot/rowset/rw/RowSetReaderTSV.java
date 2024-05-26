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

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetReader;
import org.apache.jena.riot.rowset.RowSetReaderFactory;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetStream;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowSetReaderTSV implements RowSetReader {

    private static Logger log = LoggerFactory.getLogger(RowSetReaderTSV.class);

    public static final RowSetReaderFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_TSV ) )
            throw new ResultSetException("RowSetReader for TSV asked for a "+lang);
        return new RowSetReaderTSV();
    };

    static Pattern pattern = Pattern.compile("\t");

    private RowSetReaderTSV() {}

    @Override
    public QueryExecResult readAny(InputStream in, Context context) {
        RowSet resultSet = resultSetFromTSV(in);
        return new QueryExecResult(resultSet);
    }

    @Override
    public RowSet read(InputStream in, Context context) {
        return resultSetFromTSV(in);
    }

    /**
     * Reads SPARQL Results from TSV format into a {@link RowSet} instance
     * @param in Input Stream
     */
    public static RowSet resultSetFromTSV(InputStream in) {
        BufferedReader reader = IO.asBufferedUTF8(in);
        List<Var> vars = new ArrayList<>();

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
                }
            }
        }
        catch (IOException ex) {
            throw new ARQException(ex);
        }

        // Generate an instance of RowSetStream using TSVInputIterator
        // This will parse actual result rows as needed thus minimising memory usage
        return RowSetStream.create(vars, new TSVInputIterator(reader, vars));
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

   /**
    * Class used to do streaming parsing of actual result rows from the TSV
    */
   static class TSVInputIterator implements Iterator<Binding>
   {
       private BufferedReader reader;
       private Binding currentBinding;
       private int expectedItems;
       private List<Var> vars;
       private long lineNum = 1;
       private boolean finished = false;

       /**
        * Creates a new TSV Input Iterator
        * <p>
        * Assumes the Header Row has already been read and that the next row to be read from the reader will be a Result Row
        * </p>
        */
       TSVInputIterator(BufferedReader reader, List<Var> vars) {
           Objects.requireNonNull(reader);
           this.reader = reader;
           this.expectedItems = vars.size();
           this.vars = vars;
       }

       @Override
       public boolean hasNext() {
           if ( finished )
               return false;
           if ( currentBinding == null )
               currentBinding = parseNextBinding();
           if ( currentBinding == null ) {
               IO.close(reader);
               finished = true;
           }
           return ! finished;
       }

       @Override
       public Binding next() {
           if ( ! hasNext() )
               throw new NoSuchElementException();
           Binding row = currentBinding;
           currentBinding = null;
           return row;
       }

       @Override
       public void forEachRemaining(Consumer<? super Binding> action) {
           if ( finished )
               return;
           if ( null != currentBinding ) {
               action.accept(currentBinding);
               currentBinding = null;
           }
           Binding row;
           while (null != (row = parseNextBinding())) {
               action.accept(row);
           }
           IO.close(reader);
           finished = true;
       }

       private Binding parseNextBinding() {
           String line;
           try {
               line = reader.readLine();
               // Once EOF has been reached we'll see null for this call so we can
               // return false because there are no further bindings
               if ( line == null )
                   return null;
               this.lineNum++;
           }
           catch (IOException e) {
               throw new ResultSetException("Error parsing TSV results - " + e.getMessage());
           }

           if ( line.isEmpty() ) {
               // Empty input line - no bindings.
               // Only valid when we expect zero/one values as otherwise we should
               // get a sequence of tab characters
               // which means a non-empty string which we handle normally
               if ( expectedItems > 1 )
                   throw new ResultSetException(format("Error Parsing TSV results at Line %d - The result row had 0/1 values when %d were expected",
                                                       this.lineNum, expectedItems));
               return BindingFactory.empty();
           }

           String[] tokens = pattern.split(line, -1);

           if ( tokens.length != expectedItems )
               throw new ResultSetException(format("Error Parsing TSV results at Line %d - The result row '%s' has %d values instead of the expected %d.",
                                                   this.lineNum, line, tokens.length, expectedItems));
           BindingBuilder builder = Binding.builder();

           for ( int i = 0 ; i < tokens.length ; i++ ) {
               String token = tokens[i];

               // If we see an empty string this denotes an unbound value
               if ( token.equals("") )
                   continue;

               // Bound value so parse it and add to the binding
               try {
                   Node node = NodeFactoryExtra.parseNode(token);
                   if ( !node.isConcrete() )
                       throw new ResultSetException(format("Line %d: Not a concrete RDF term: %s", lineNum, token));
                   builder.add(this.vars.get(i), node);
               }
               catch (RiotException ex) {
                   throw new ResultSetException(format("Line %d: Data %s contains error: %s", lineNum, token, ex.getMessage()));
               }
           }
           return builder.build();
       }
   }
}
