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

package org.apache.jena.riot.resultset;

import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFactory ;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.util.Context ;

public interface ResultSetReader {

    /**
     * Read from an {@code InputStream} and produce a {@link ResultSet}.
     * Note that return result may stream and so the input stream may be read
     * while the {@link ResultSet} is used.
     * See {@link ResultSetFactory#copyResults(ResultSet)} for a ResultSet that is detached from the {@code InputStream}.
     * @param in InputStream to read from.
     * @param context
     * @return ResultSet
     */
    public default ResultSet read(InputStream in, Context context) {
        return readAny(in, context).getResultSet();
    }

    /**
     * Read from an {@code InputStream} and produce a {@link SPARQLResult}.
     * Note that return result may stream and so the input stream may be read
     * while the {@link ResultSet} is used.
     * See {@link #read(InputStream, Context)} for more details
     * @param in InputStream to read from.
     * @param context
     * @return SPARQLResult
     */
    public SPARQLResult readAny(InputStream in, Context context) ;

    /**
     * <em>Using {@link #read(InputStream, Context)} is preferred.</em>
     * <p>
     * Not all formats support reading from a {@code java.io.Reader}.
     * <p>
     * Read from an {@code Reader} and produce a {@link ResultSet}.
     * Note that return result may stream and so the reader may be read
     * while the ResultSet is used.
     * See {@link ResultSetFactory#copyResults(ResultSet)} for a ResultSet that is detached from the {@code InputStream}.
     * @param in Reader
     * @param context
     * @return ResultSet
     */
    public default ResultSet read(Reader in, Context context) {
        throw new UnsupportedOperationException("ResultSetReader.read - input from a Java Reader not supported.  Use an InputStream.");
    }
}

