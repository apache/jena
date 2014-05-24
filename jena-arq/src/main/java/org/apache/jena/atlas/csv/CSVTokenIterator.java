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

package org.apache.jena.atlas.csv ;

import static org.apache.jena.atlas.csv.CSVTokenType.* ;

import java.io.InputStream;
import java.io.Reader;

import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.iterator.IteratorSlotted ;

public class CSVTokenIterator extends IteratorSlotted<CSVToken>
{
    private PeekReader in ;

    // One EOF?

    public CSVTokenIterator(InputStream input) {
        this.in = PeekReader.makeUTF8(input) ;
    }
    
    public CSVTokenIterator(Reader reader) {
        this.in = PeekReader.make(reader);
    }

    @Override
    protected CSVToken moveToNext() {
        int ch = in.peekChar() ;
        if ( ch == '\r' ) {
            in.readChar() ;
            ch = in.peekChar() ;
            if ( ch != '\n' )
                return new CSVToken(in.getLineNum(), in.getColNum(), NL, "\r") ;
            // '\n' = drop through.
        }

        if ( ch == '\n' ) {
            in.readChar() ;
            return new CSVToken(in.getLineNum(), in.getColNum(), NL, "\n") ;
        }

        if ( ch == ',' ) {
            in.readChar() ;
            return new CSVToken(in.getLineNum(), in.getColNum(), COMMA, ",") ;
        }

        long line = in.getLineNum() ;
        long col = in.getColNum() ;

        // Not -1
        if ( ch == '"' || ch == '\'' )
            return new CSVToken(line, col, QSTRING, readQuotedString()) ;
        else
            return new CSVToken(line, col, STRING, readUnquotedString()) ;
    }

    StringBuilder builder = new StringBuilder() ;

    private String readQuotedString() {
        builder.setLength(0) ;
        int qCh = in.readChar() ;
        int ch = qCh ;
        while (true) {
            ch = in.readChar() ;
            if ( ch == -1 )
                CSVParser.exception("Unterminated quoted string at end-of-file", in.getLineNum(), in.getColNum()) ;
            // Newlines are allowed in quoted strings.
            // if ( ch == '\r' || ch == '\n' )
            // exception("Unterminated quoted string", in.getLineNum(),
            // in.getColNum()) ;
            if ( ch == qCh ) {
                int ch2 = in.peekChar() ;
                if ( ch2 != qCh )
                    break ;
                // Escaped quote
                in.readChar() ;
                // Fall through.
            }
            builder.append((char)ch) ;
        }
        return builder.toString() ;
    }

    private String readUnquotedString() {
        builder.setLength(0) ;
        while (true) {
            int ch = in.peekChar() ;
            if ( ch == -1 || ch == '\r' || ch == '\n' )
                break ;
            if ( ch == ',' )
                break ;
            in.readChar() ;
            builder.append((char)ch) ;
        }
        return builder.toString() ;
    }

    @Override
    protected boolean hasMore() {
        return !in.eof() && in.peekChar() != -1 ;
    }
}
