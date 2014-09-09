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

import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetCSV ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetJSON ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetTSV ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetText ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetThrift ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetXML ;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Objects ;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.thrift.BinRDF ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.resultset.* ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Context ;

public class ResultSetWriterRegistry {

    private static Map<Lang, ResultSetWriterFactory> registry = new HashMap<>() ;
    
    /** Lookup a {@linkplain Lang} to get the registered {@linkplain ResultSetReaderFactory} (or null) */
    public static ResultSetWriterFactory lookup(Lang lang) {
        Objects.requireNonNull(lang) ;
        return registry.get(lang) ;
    }

    /** Register a {@linkplain ResultSetReaderFactory} for a {@linkplain Lang} */
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

//        RDFLanguages.register(SPARQLResultSetXML) ;
//        RDFLanguages.register(SPARQLResultSetJSON) ;
//        RDFLanguages.register(SPARQLResultSetCSV) ;
//        RDFLanguages.register(SPARQLResultSetTSV) ;
//        RDFLanguages.register(SPARQLResultSetThrift) ;
//        // Not text. 
        
        ResultSetWriterFactory factory = new ResultSetWriterFactoryStd() ;
        register(SPARQLResultSetXML,    factory) ;
        register(SPARQLResultSetJSON,   factory) ;
        register(SPARQLResultSetCSV,    factory) ;
        register(SPARQLResultSetTSV,    factory) ;
        register(SPARQLResultSetThrift, new ResultSetWriterThriftFactory()) ;
        register(SPARQLResultSetText,   factory) ;
    }
 
    static { ResultSetLang.init(); }
    
    private static ResultSetWriter writerXML = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) { 
            XMLOutput xOut = new XMLOutput(null) ;
            xOut.format(out, resultSet) ;
        }
        @Override public void write(Writer out, ResultSet resultSet, Context context) {throw new NotImplemented("Writer") ; } 
    } ;

    private static ResultSetWriter writerJSON = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {
            JSONOutput jOut = new JSONOutput() ;
            jOut.format(out, resultSet) ; 
        }
        @Override public void write(Writer out, ResultSet resultSet, Context context) {throw new NotImplemented("Writer") ; } 
    } ;
    
    private static ResultSetWriter writerCSV = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {
            CSVOutput fmt = new CSVOutput() ;
            fmt.format(out, resultSet) ;
        }
        @Override public void write(Writer out, ResultSet resultSet, Context context) {throw new NotImplemented("Writer") ; } 
    } ;

    private static ResultSetWriter writerTSV = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {
            TSVOutput fmt = new TSVOutput() ;
            fmt.format(out, resultSet) ;
        }
        @Override public void write(Writer out, ResultSet resultSet, Context context) {throw new NotImplemented("Writer") ; } 
    } ;

    private static ResultSetWriter writerNo = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {}
        @Override public void write(Writer out, ResultSet resultSet, Context context)       {}
    } ;

    private static ResultSetWriter writerText = new ResultSetWriter() {
        @Override public void write(OutputStream out, ResultSet resultSet, Context context) {
            // Prefix mapp
            TextOutput tFmt = new TextOutput(new SerializationContext((Prologue)null)) ;
            tFmt.format(out, resultSet) ; 
        }
        @Override public void write(Writer out, ResultSet resultSet, Context context) {throw new NotImplemented("Writer") ; } 
    } ;
    
    private static class ResultSetWriterFactoryStd implements ResultSetWriterFactory {
        @Override
        public ResultSetWriter create(Lang lang) {
            lang = Objects.requireNonNull(lang, "Language must not be null") ;
            if ( lang.equals(SPARQLResultSetXML) )      return writerXML ;
            if ( lang.equals(SPARQLResultSetJSON) )     return writerJSON ;
            if ( lang.equals(SPARQLResultSetCSV) )      return writerCSV ;
            if ( lang.equals(SPARQLResultSetTSV) )      return writerTSV ;
            if ( lang.equals(SPARQLResultSetText) )     return writerText ;
            throw new RiotException("Lang not registered (ResultSet writer)") ;
        }
    }
    
    private static class ResultSetWriterThriftFactory implements ResultSetWriterFactory {
        @Override
        public ResultSetWriter create(Lang lang) {
            return new ResultSetWriter() {
                @Override
                public void write(OutputStream out, ResultSet resultSet, Context context)
                { BinRDF.writeResultSet(out, resultSet) ; }
                
                @Override
                public void write(Writer out, ResultSet resultSet, Context context) {
                    throw new NotImplemented("Writing binary data to a java.io.Writer is not possible") ;

                }
            } ;
        }
    }
}

