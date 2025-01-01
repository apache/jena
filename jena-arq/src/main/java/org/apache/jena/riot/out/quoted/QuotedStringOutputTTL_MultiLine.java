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

package org.apache.jena.riot.out.quoted;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.atlas.lib.EscapeStr;

public class QuotedStringOutputTTL_MultiLine extends QuotedStringOutputTTL {

    public QuotedStringOutputTTL_MultiLine() {
        super();
    }

    /** Always use the given quote character */
    public QuotedStringOutputTTL_MultiLine(char quoteChar) {
        super(quoteChar);
    }
    
    /** Turtle is UTF-8 : ASCII is for non-standard needs */
    protected QuotedStringOutputTTL_MultiLine(char quoteChar, CharSpace charSpace) {
        super(quoteChar, charSpace);
    } 
    
    /** Write a string using triple quotes. Try to avoid escapes by looking for the quote character first. */
    @Override
    public void writeStrMultiLine(AWriter w, String s) {
        quote3(w);
        EscapeStr.stringEsc(w, s, quoteChar, false, charSpace); 
        quote3(w);
    }
    
    private void quote3(AWriter w) {
        w.print(quoteChar);
        w.print(quoteChar);
        w.print(quoteChar);
    }
}

