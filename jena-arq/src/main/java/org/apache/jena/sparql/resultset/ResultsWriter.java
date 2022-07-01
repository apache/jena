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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.rowset.RowSetWriter;
import org.apache.jena.riot.rowset.RowSetWriterFactory;
import org.apache.jena.riot.rowset.RowSetWriterRegistry;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;

public class ResultsWriter {
    static { JenaSystem.init(); }

    /** Create a {@code ResultsWriter.Builder}. */
    public static Builder create() { return new Builder() ; }

    public static class Builder {
        private Lang lang = null;
        private Context context = null;

        /** Provide a {@link Lang} for the writer.*/
        public Builder lang(Lang lang) {
            this.lang = lang;
            return this;
        }

        /** Set the {@link Context}.
         * The global settings of {@code ARQ.getContext()} are used unless otherwise changed.
         * This call replaces any previous {@link #set}.
         */
        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        private void ensureContext() {
            if ( context == null )
                context = new Context();
        }

        /** Set a value in the writing context. */
        public Builder set(Symbol symbol, Object value) {
            if ( context == null )
                context = ARQ.getContext().copy();
            context.set(symbol, value);
            return this;
        }

        /** Remove a context setting. */
        public Builder unset(Symbol symbol) {
            if ( context == null )
                return this;
            context.unset(symbol);
            return this;
        }

        /** Build a {@code ResultWriter} */
        public ResultsWriter build() {
            return new ResultsWriter(lang, context);
        }

        /** Short form equivalent to {@code build().write(url, ResultSet)} */
        public void write(String url, ResultSet resultSet) {
            build().write(url, resultSet);
        }

        /** Short form equivalent to {@code build().write(OutputStream, ResultSet)} */
        public void write(OutputStream output, ResultSet resultSet) {
            build().write(output, resultSet);
        }

        /** Short form equivalent to {@code build().write(url, RowSet)} */
        public void write(String url, RowSet rowSet) {
            build().write(url, rowSet);
        }

        /** Short form equivalent to {@code build().write(OutputStream, RowSet)} */
        public void write(OutputStream output, RowSet rowSet) {
            build().write(output, rowSet);
        }

        /** Short form equivalent to {@code build().write(url, boolValue)} */
        public void write(String url, boolean booleanResult) {
            build().write(url, booleanResult);
        }

        /** Short form equivalent to {@code build().write(OutputStream, booleanResult)} */
        public void write(OutputStream output, boolean booleanResult) {
            build().write(output, booleanResult);
        }
    }

    private final Lang lang;
    private final Context context;

    private ResultsWriter(Lang lang, Context context) {
        super();
        this.lang = lang;
        this.context = context;
    }

    /** Write a result set, using the configuration of the {@code ResultsWriter}, to a file */
    public void write(String filename, ResultSet resultSet) {
        Objects.requireNonNull(filename);
        Objects.requireNonNull(resultSet);
        write(filename, RowSet.adapt(resultSet));
    }

    /** Write a result set, using the configuration of the {@code ResultsWriter}, to a file */
    public void write(String filename, RowSet rowSet) {
        Objects.requireNonNull(filename);
        Objects.requireNonNull(rowSet);
        try ( OutputStream out = openURL(filename) ) {
            write(out, rowSet);
        } catch (IOException ex) { IO.exception(ex); }
    }

    /** Write a result set, using the configuration of the {@code ResultWriter}, to an {@code OutputStream}. */
    public void write(OutputStream output, ResultSet resultSet) {
        Objects.requireNonNull(output);
        Objects.requireNonNull(resultSet);
        write(output, RowSet.adapt(resultSet));
    }

    /** Write a result set, using the configuration of the {@code ResultWriter}, to an {@code OutputStream}. */
    public void write(OutputStream output, RowSet rowSet) {
        Objects.requireNonNull(output);
        Objects.requireNonNull(rowSet);
        write(output, rowSet, null, lang);
    }

    /** Write a boolean result, using the configuration of the {@code ResultWriter}, to a file */
    public void write(String filename, boolean booleanResult) {
        Objects.requireNonNull(filename);
        try ( OutputStream out = openURL(filename) ) {
            write(out, booleanResult);
        } catch (IOException ex) { IO.exception(ex); }
    }

    /** Write a boolean result, using the configuration of the {@code ResultWriter}, to an {@code OutputStream}. */
    public void write(OutputStream output, boolean booleanResult) {
        Objects.requireNonNull(output);
        write(output, null, booleanResult, lang);
    }

    private void write(OutputStream output, RowSet resultSet, Boolean result, Lang lang) {
        if ( resultSet == null && result == null )
            throw new RiotException("No result set and no boolean result");
        if ( resultSet != null && result != null )
            throw new RiotException("Both result set and boolean result supplied");
        if ( ! RowSetWriterRegistry.isRegistered(lang) )
            throw new RiotException("Not registered as a SPARQL result set output syntax: "+lang);

        RowSetWriterFactory factory = RowSetWriterRegistry.getFactory(lang);
        if ( factory == null )
            throw new RiotException("No ResultSetReaderFactory for "+lang);
        RowSetWriter writer = factory.create(lang);
        if ( resultSet != null )
            writer.write(output, resultSet, context);
        else
            writer.write(output, result, context);
    }

    private OutputStream openURL(String filename) {
        return IO.openOutputFile(filename);
    }
}
