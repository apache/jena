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

package org.apache.jena.shacl.compact.writer;

import java.io.OutputStream;
import java.io.Writer;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.writer.WriterGraphRIOTBase;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.compact.ShaclcWriter;
import org.apache.jena.sparql.util.Context;

public class WriterRIOTShaclc extends WriterGraphRIOTBase {

    public WriterRIOTShaclc() {}

    @Override
    public Lang getLang() {
        return Lang.SHACLC;
    }

    @Override
    public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = RiotLib.create(out) ;
        try {
            iOut.setAbsoluteIndent(4);
            write(iOut, graph, prefixMap, baseURI, context);
        } finally { iOut.flush(); }
    }

    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = new IndentedWriter(out);
        try {
            iOut.setUnitIndent(4);
            write(iOut, graph, prefixMap, baseURI, context);
        } finally { iOut.flush(); }
    }

    private void write(IndentedWriter out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        Shapes shapes = Shapes.parse(graph);
        ShaclcWriter.print(out, shapes);
    }
}
