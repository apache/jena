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

package com.hp.hpl.jena.sparql.resultset;


import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.* ;

import java.io.OutputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.io.JSWriter ;

/** JSON Output (ASK format) */

public class JSONOutputASK
{
    private OutputStream outStream ;
    
    public JSONOutputASK(OutputStream outStream) {
        this.outStream = outStream;
        
    }

    public void exec(boolean result)
    {
        JSWriter out = new JSWriter(outStream) ;
        
        out.startOutput() ;

        out.startObject() ;
        out.key(kHead) ;
        out.startObject() ;
        out.finishObject() ;
        out.pair(kBoolean, result) ;
        out.finishObject() ;
        
        out.finishOutput() ;

        IO.flush(outStream) ;
    }
}
