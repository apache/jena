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

package org.apache.jena.riot.tokens;

import java.io.InputStream;
import java.io.Reader;

import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;

/** Builder for TokenizeText */
public class TokenizeTextBuilder {
    
    // One of these.
    private PeekReader   peekReader   = null;
    private InputStream  input        = null;
    private Reader       reader       = null;
    private String       string       = null;
    
    private boolean      lineMode     = false;
    private boolean      utf8         = true;
    private ErrorHandler errorHandler = null;

    TokenizeTextBuilder() {}

    private void clearInput() {
        this.peekReader = null;
        this.input = null;
        this.reader = null;
        this.string = null;
    }

    public TokenizeTextBuilder source(InputStream input) {
        clearInput();
        this.input = input;
        return this;
    }

    public TokenizeTextBuilder source(Reader reader) {
        clearInput();
        this.reader = reader;
        return this;
    }

    public TokenizeTextBuilder source(PeekReader peekReader) {
        clearInput();
        this.peekReader = peekReader;
        return this;
    }

    public TokenizeTextBuilder fromString(String string) {
        clearInput();
        this.string = string;
        return this;
    }

    public TokenizeTextBuilder lineMode(boolean lineMode) {
        this.lineMode = lineMode;
        return this;
    }

    public TokenizeTextBuilder asciiOnly(boolean asciiOnly) {
        this.utf8 = !asciiOnly;
        return this;
    }

    public TokenizeTextBuilder errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    private static int countNulls(Object ... objs) {
        int x = 0;
        for ( Object obj : objs )
            if ( obj == null )
                x++;
        return x;
    }

    private static int countNotNulls(Object ... objs) {
        int x = 0;
        for ( Object obj : objs )
            if ( obj != null )
                x++;
        return x;
    }

    public Tokenizer build() {
        ErrorHandler errHandler = (errorHandler != null) ? errorHandler : ErrorHandlerFactory.errorHandlerExceptions();
        int x = countNotNulls(peekReader, input, reader, string);
        if ( x > 1 )
            throw new InternalErrorException("Too many data sources");
        PeekReader pr;
        if ( input != null ) {
            pr = utf8 ? PeekReader.makeUTF8(input) : PeekReader.makeASCII(input);
        } else if ( string != null ) {
            pr = PeekReader.readString(string);
        } else if ( reader != null ) {
            pr = PeekReader.make(reader);
        } else if ( peekReader != null ) {
            pr = peekReader;
        } else {
            throw new IllegalStateException("No data source");
        }

        return TokenizerText.internal(pr, lineMode, errHandler);
    }
}
