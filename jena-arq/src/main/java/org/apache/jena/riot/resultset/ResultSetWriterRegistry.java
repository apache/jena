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

import static org.apache.jena.riot.resultset.ResultSetLang.*;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Objects ;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.resultset.rw.ResultSetWriterJSON;
import org.apache.jena.riot.resultset.rw.ResultSetWriterThrift;
import org.apache.jena.riot.resultset.rw.ResultSetWriterXML;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.resultset.CSVOutput;
import org.apache.jena.sparql.resultset.TSVOutput;
import org.apache.jena.sparql.resultset.TextOutput;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.util.Context ;

public class ResultSetWriterRegistry {

    private static Map<Lang, ResultSetWriterFactory> registry = new HashMap<>() ;
    
    /** Lookup a {@link Lang} to get the registered {@link ResultSetReaderFactory} (or null) */
    public static ResultSetWriterFactory getFactory(Lang lang) {
        Objects.requireNonNull(lang) ;
        return registry.get(lang) ;
    }

    public static boolean isRegistered(Lang lang) {
        Objects.requireNonNull(lang) ;
        return registry.containsKey(lang) ;
    }

    /** Register a {@link ResultSetReaderFactory} for a {@link Lang} */
    public static void register(Lang lang, ResultSetWriterFactory factory) {
        Objects.requireNonNull(lang) ;
        Objects.requireNonNull(factory) ;
        registry.put(lang, factory) ;
    }
    
    private static boolean initialized = false ;
    public static void init() {
        if ( initialized )
            return ;
        initialized = true ;

        ResultSetWriterFactory factory = new ResultSetWriterFactoryStd() ;
        register(SPARQLResultSetXML,    ResultSetWriterXML.factory) ;
        register(SPARQLResultSetJSON,   ResultSetWriterJSON.factory) ;
        register(SPARQLResultSetThrift, ResultSetWriterThrift.factory) ;
        // Build-in std factory (below).
        register(SPARQLResultSetCSV,    factory) ;
        register(SPARQLResultSetTSV,    factory) ;
        register(SPARQLResultSetText,   factory) ;
        register(SPARQLResultSetNone,   factory) ;
    }
 
    private static ResultSetWriter writerCSV = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {
            CSVOutput fmt = new CSVOutput() ;
            fmt.format(out, resultSet) ;
        }
        @Override public void write(Writer out, ResultSet resultSet, Context context)   { throw new NotImplemented("Writer") ; }
        @Override public void write(OutputStream out, boolean result, Context context)  {
            CSVOutput fmt = new CSVOutput() ;
            fmt.format(out, result) ;
        }
    } ;

    private static ResultSetWriter writerTSV = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {
            TSVOutput fmt = new TSVOutput() ;
            fmt.format(out, resultSet) ;
        }
        @Override public void write(Writer out, ResultSet resultSet, Context context)   {throw new NotImplemented("Writer") ; }
        @Override public void write(OutputStream out, boolean result, Context context)  {
            TSVOutput fmt = new TSVOutput() ;
            fmt.format(out, result) ;
        }
    } ;

    private static ResultSetWriter writerNone = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {}
        @Override public void write(Writer out, ResultSet resultSet, Context context)       {}
        @Override public void write(OutputStream out, boolean result, Context context)      {}
    } ;

    private static ResultSetWriter writerText = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {
            Prologue prologue = choosePrologue(resultSet, context);
            TextOutput tFmt = new TextOutput(new SerializationContext(prologue)) ;
            tFmt.format(out, resultSet) ; 
        }
        @Override public void write(Writer out, ResultSet resultSet, Context context) {throw new NotImplemented("Writer") ; }
        @Override public void write(OutputStream out, boolean result, Context context) {
            TextOutput tFmt = new TextOutput(new SerializationContext((Prologue)null)) ;
            tFmt.format(out, result) ; 
        }
    } ;
    
    /** Establish a prologue for formatting output.  Return "null" for none found. */ 
    private static Prologue choosePrologue(ResultSet resultSet, Context context) {
        try {
            if ( context != null && context.get(ARQConstants.symPrologue) != null )
                return context.get(ARQConstants.symPrologue);
            Model m = resultSet.getResourceModel();
            if ( m != null )
                return new Prologue(m);
        } catch (Exception ex) {
            Log.warn(ARQ.getExecLogger(), "Failed to establish a 'Prologue' for text output: "+ex.getMessage()); 
        }
        return null;
    }

    private static class ResultSetWriterFactoryStd implements ResultSetWriterFactory {
        @Override
        public ResultSetWriter create(Lang lang) {
            lang = Objects.requireNonNull(lang, "Language must not be null");
//            if ( lang.equals(SPARQLResultSetXML) )      return writerXML;
//            if ( lang.equals(SPARQLResultSetJSON) )     return writerJSON;
            if ( lang.equals(SPARQLResultSetCSV) )      return writerCSV;
            if ( lang.equals(SPARQLResultSetTSV) )      return writerTSV;
            if ( lang.equals(SPARQLResultSetText) )     return writerText;
            if ( lang.equals(SPARQLResultSetNone) )     return writerNone;
            throw new RiotException("Lang not registered (ResultSet writer)") ;
        }
    }
}

