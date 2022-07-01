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

package org.apache.jena.riot.writer ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.sparql.util.Context ;

public class TurtleWriter extends TurtleWriterBase {

    @Override
    protected void output(IndentedWriter iOut, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        TurtleWriter$ w = new TurtleWriter$(iOut, prefixMap, baseURI, context);
        w.write(graph);
    }

    private static class TurtleWriter$ extends TurtleShell {
        public TurtleWriter$(IndentedWriter out, PrefixMap prefixMap, String baseURI, Context context) {
            super(out, prefixMap, baseURI, context);
        }

        private void write(Graph graph) {
            writeBase(baseURI);
            writePrefixes(prefixMap);
            if ( !prefixMap.isEmpty() && !graph.isEmpty() )
                out.println();
            writeGraphTTL(graph);
        }
    }
}
