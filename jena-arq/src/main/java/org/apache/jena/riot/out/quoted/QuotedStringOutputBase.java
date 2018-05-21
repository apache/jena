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

package org.apache.jena.riot.out.quoted;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.lib.CharSpace ;
import org.apache.jena.atlas.lib.EscapeStr ;

/** Quoted string output - single line, settable quote character and char space. */
public class QuotedStringOutputBase implements QuotedStringOutput {
    protected final CharSpace charSpace ;
    protected final char quoteChar;
    
    protected QuotedStringOutputBase(char quoteChar, CharSpace charSpace) {
        this.charSpace = charSpace ;
        this.quoteChar = quoteChar ;
    } 

    @Override
    public char getQuoteChar() { return quoteChar ; } 
    
    @Override
    public void writeStr(AWriter writer, String str) {
        // Only " strings in N-Triples/N-Quads
        writer.print(getQuoteChar());
        EscapeStr.stringEsc(writer, str, getQuoteChar(), true, charSpace) ;
        writer.print(getQuoteChar());
    }

    @Override
    public void writeStrMultiLine(AWriter writer, String str) {
        // No multiline strings in N-Triples/N-Quads.
        writeStr(writer, str) ;
    }
}

