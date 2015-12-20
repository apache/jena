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
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.atlas.lib.EscapeStr ;

public class QuotedURI {
    private final CharSpace charSpace ;

    public QuotedURI() {
        this(CharSpace.UTF8) ;
    }
    
    public QuotedURI(CharSpace charSpace) {
        this.charSpace = charSpace ; 
    } 
    
    /** Write a string for a URI on one line. */
    public void writeURI(AWriter w, String s) {
        // URIs do not have an escape mechanism. %-is an encoding.
        // We can either print as-is or convert to %-encoding.
        // (Ignoring host names which be in puny code).
        w.print(Chars.CH_LT);
        if ( CharSpace.isAscii(charSpace) )
            EscapeStr.writeASCII(w, s) ;
        else
            w.print(s) ;
        w.print(Chars.CH_GT);
    }
}
