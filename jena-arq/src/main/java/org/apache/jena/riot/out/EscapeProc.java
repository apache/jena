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

package org.apache.jena.riot.out;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.lib.EscapeStr ;

public class EscapeProc {
    private final boolean ascii ;

    public EscapeProc(CharSpace charSpace) {
        this.ascii = ( charSpace == CharSpace.ASCII ) ; 
    } 

    public void writeURI(AWriter w, String s) {
        if ( ascii )
            EscapeStr.stringEsc(w, s, true, ascii) ;
        else
            // It's a URI - assume legal.
            w.print(s) ;
    }

    public void writeStr(AWriter w, String s) {
        EscapeStr.stringEsc(w, s, true, ascii) ;
    }

    public void writeStrMultiLine(AWriter w, String s) {
        // N-Triples does not have """
        EscapeStr.stringEsc(w, s, false, ascii) ;
    }

}

