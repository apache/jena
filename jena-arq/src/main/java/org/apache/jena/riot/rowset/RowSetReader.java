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

package org.apache.jena.riot.rowset;

import java.io.InputStream;
import java.io.Reader;

import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

public interface RowSetReader {

    /**
     * Read from an {@code InputStream} and produce a {@link RowSet}.
     * Note that return row set may stream and so the input stream may be read
     * while the {@link RowSet} is used.
     * See {@link RowSet#materialize} for a RowSet that is detached from the {@code InputStream}.
     * @param in InputStream to read from.
     * @param context
     * @return RowSet
     */
    public default RowSet read(InputStream in, Context context) {
        return readAny(in, context).rowSet();
    }

    /**
     * Read from an {@code InputStream} and produce a {@link QueryExecResult}.
     * Note that return result may stream and so the input stream may be read
     * while the {@link RowSet} is used.
     * See {@link #read(InputStream, Context)} for more details
     * @param in InputStream to read from.
     * @param context
     * @return QueryExecResult
     */
    public QueryExecResult readAny(InputStream in, Context context) ;

    /**
     * <em>Using {@link #read(InputStream, Context)} is preferred.</em>
     * <p>
     * Not all formats support reading from a {@code java.io.Reader}.
     * <p>
     * Read from an {@code Reader} and produce a {@link RowSet}.
     * Note that return result may stream and so the reader may be read
     * while the RowSet is used.
     * See {@link RowSet#materialize} for a RowSet that is detached from the {@code InputStream}.
     * @param in Reader
     * @param context
     * @return RowSet
     */
    public default RowSet read(Reader in, Context context) {
        throw new UnsupportedOperationException("RowSetReader.read - input from a Java Reader not supported.  Use an InputStream.");
    }
}
