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

import java.io.InputStream;
import java.util.Objects;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.*;
import org.apache.jena.riot.resultset.ResultSetReader;
import org.apache.jena.riot.resultset.ResultSetReaderFactory;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.util.Context;

public class ResultsReader {
    
    /** Create a {@code ResultsReader.Builder}. */
    public static Builder create() { return new Builder() ; }
    
    public static class Builder {
        private Lang hintLang = null; 
        private Lang forceLang = null; 
        private Context context = null;

        /** Provide a {@link Lang} for the parser.
         * The declared MIME type takes precedence, 
         * the file extension does not.
         */
        public Builder lang(Lang hintLang) {
            this.hintLang = hintLang;
            return this;
        }

        /** Provide a {@link Lang} for the parser.
         * This setting overrides any declared MIME type or file extension.  
         */
        public Builder forceLang(Lang forceLang) {
            this.forceLang = forceLang;
            return this;
        }

        /** Set the {@link Context}. This defaults to the global settings of {@code ARQ.getContext()}. */
        public Builder context(Context context) {
            if ( context != null )
                context = context.copy();
            this.context = context;
            return this;
        }
        
        /** Build a {@code ResultsReader} */
        public ResultsReader build() {
            return new ResultsReader(hintLang, forceLang, context); 
        }
        
        /** Short form equivalent to {@code .build().read(url)} */
        public ResultSet read(String url) {
          return build().read(url); 
        }
        
        /** Short form equivalent to {@code .build().read(InputStreams)} */
        public ResultSet read(InputStream input) {
          return build().read(input); 
        }
    }
    
    private final Lang hintLang;
    private final Lang forceLang; 
    private final Context context;
    
    private ResultsReader(Lang hintLang, Lang forceLang, Context context) {
        super();
        this.hintLang = hintLang;
        this.forceLang = forceLang;
        this.context = context;
    }

    private Lang determinLang(TypedInputStream in, String url) {
        if ( in == null )
            throw new RiotNotFoundException(url);
        Lang lang = forceLang;
        if ( lang == null ) {
            ContentType ct = WebContent.determineCT(in.getContentType(), hintLang, url);
            lang = RDFLanguages.contentTypeToLang(ct);
        }
        if ( lang == null )
            throw new RiotException("Can't indentify the result set syntax from "+url); 
        return lang;
    }
    
    /** Read a result set from a URL or filename. */
    public ResultSet read(String urlOrFilename) {
        Objects.nonNull(urlOrFilename);
        try ( TypedInputStream in = StreamManager.get(context).open(urlOrFilename) ) {
            Lang lang = determinLang(in, urlOrFilename);
            return readResultSet(in.getInputStream(), lang);
        }
    }
    
    /** Read a result set from an {@code InputStream}. */
    public ResultSet read(InputStream input) {
        Objects.nonNull(input);
        Lang lang = (forceLang!=null) ? forceLang : hintLang;
        if ( lang == null )
            throw new RiotException("Need a syntax to read a result set from an InputStream");
        return readResultSet(input, lang);
    }
    
    /** Read a result set or boolean from a URL or filename. */
    public SPARQLResult readAny(String urlOrFilename) {
        Objects.nonNull(urlOrFilename);
        try ( TypedInputStream in = StreamManager.get(context).open(urlOrFilename) ) {
            Lang lang = determinLang(in, urlOrFilename);
            return readAny(in.getInputStream(), lang);
        }
    }

    /** Read a result set or boolean from an {@code InputStream}. */
    public SPARQLResult readAny(InputStream input) {
        Objects.nonNull(input);
        Lang lang = (forceLang!=null) ? forceLang : hintLang;
        if ( lang == null )
            throw new RiotException("Need a syntax to read a result set from an InputStream");
        return readAny(input, lang);
    }

    private ResultSet readResultSet(InputStream input, Lang lang) {
        return readAny(input, lang).getResultSet();
    }
    
    private SPARQLResult readAny(InputStream input, Lang lang) {
        if ( ! ResultSetReaderRegistry.isRegistered(lang) )
            throw new RiotException("Not registered as a SPARQL result set input syntax: "+lang);
        
        ResultSetReaderFactory factory = ResultSetReaderRegistry.getFactory(lang);
        if ( factory == null )
            throw new RiotException("No ResultSetReaderFactory for "+lang);
        ResultSetReader reader = factory.create(lang);
        SPARQLResult rs = reader.readAny(input, context);
        return rs;
    }
}
