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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.sparql.util.Context;
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

        /** Set the {@link Context}. This defaults to the global settings of {@code ARQ.getContext()}. */
        public Builder context(Context context) {
            if ( context != null )
                context = context.copy();
            this.context = context;
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
    }
    
    private final Lang lang;
    private final Context context;
    
    private ResultsWriter(Lang lang, Context context) {
        super();
        this.lang = lang;
        this.context = context;
    }

    /** Write a result set, using the configurartion of the {@code ResultWriter}, to a file */ 
    public void write(String filename, ResultSet resultSet) {
        Objects.requireNonNull(filename);
        Objects.requireNonNull(resultSet);
        try ( OutputStream out = openURL(filename) ) {
            write(out, resultSet);
        } catch (IOException ex) { IO.exception(ex); }
    }
    
    /** Write a result set, using the configurartion of the {@code ResultWriter}, to an {@code OutputStream}. */ 
    public void write(OutputStream output, ResultSet resultSet) {
        Objects.requireNonNull(output);
        Objects.requireNonNull(resultSet);
        write(output, resultSet, null, lang);
    }
    
    /** Write a boolean result, using the configurartion of the {@code ResultWriter}, to a file */ 
    public void write(String filename, boolean booleanResult) {
        Objects.requireNonNull(filename);
        try ( OutputStream out = openURL(filename) ) {
            write(out, booleanResult);
        } catch (IOException ex) { IO.exception(ex); }
    }
    
    /** Write a boolean result, using the configurartion of the {@code ResultWriter}, to an {@code OutputStream}. */ 
    public void write(OutputStream output, boolean booleanResult) {
        Objects.requireNonNull(output);
        write(output, null, booleanResult, lang);
    }
    
    private void write(OutputStream output, ResultSet resultSet, Boolean result, Lang lang) {
        if ( resultSet == null && result == null )
            throw new RiotException("No result set and no boolean result");
        if ( resultSet != null && result != null )
            throw new RiotException("Both result set and boolean result supplied");
        if ( ! ResultSetWriterRegistry.isRegistered(lang) )
            throw new RiotException("Not registered as a SPARQL result set output syntax: "+lang);
        
        ResultSetWriterFactory factory = ResultSetWriterRegistry.getFactory(lang);
        if ( factory == null )
            throw new RiotException("No ResultSetReaderFactory for "+lang);
        ResultSetWriter writer = factory.create(lang);
        if ( resultSet != null )
            writer.write(output, resultSet, context);
        else
            writer.write(output, result, context);
    }

    private OutputStream openURL(String filename) {
        return IO.openOutputFile(filename);
    }
}
