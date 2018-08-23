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

package org.apache.jena.fuseki.access;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;

/** Build graph data for the filter/security tests. */

class GraphData {
    private static String dataStr = StrUtils.strjoinNL 
        ("PREFIX : <http://test/>"
        ,""
        ,":s0 :p 0 ."
        ,":g1 { :s1 :p 1 }"
        ,":g2 { :s2 :p 2 }"
        ,":g3 { :s3 :p 3 }"
        ,":g4 { :s4 :p 4 }"
        );
    
    
    static Node s0 = SSE.parseNode("<http://test/s0>"); 
    static Node s1 = SSE.parseNode("<http://test/s1>"); 
    static Node s2 = SSE.parseNode("<http://test/s2>"); 
    static Node s3 = SSE.parseNode("<http://test/s3>"); 
    static Node s4 = SSE.parseNode("<http://test/s4>"); 
 
    static Node g1 = SSE.parseNode("<http://test/g1>"); 
    static Node g2 = SSE.parseNode("<http://test/g2>"); 
    static Node g3 = SSE.parseNode("<http://test/g3>"); 
    static Node g4 = SSE.parseNode("<http://test/g4>"); 

    public static void fill(DatasetGraph dsg) {
         Txn.executeWrite(dsg, ()->{
             RDFParser.create().fromString(dataStr).lang(Lang.TRIG).parse(dsg);
         });
    }
}
