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

import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.atlas.lib.Chars;

/** Escape processor for Turtle. */
public class QuotedStringOutputTTL extends QuotedStringOutputBase {

    public QuotedStringOutputTTL() {
        this(Chars.CH_QUOTE2, CharSpace.UTF8);
    }

    /** Always use the given quote character (0 means use " or ' as needed) */
    public QuotedStringOutputTTL(char quoteChar) {
        this(quoteChar, CharSpace.UTF8);
    }
    
    /** Turtle is UTF-8 : ASCII is for non-standard needs */
    protected QuotedStringOutputTTL(char quoteChar, CharSpace charSpace) {
        super(quoteChar, charSpace);
    }
}

