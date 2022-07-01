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

package org.apache.jena.shex.expressions;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

class ShexPrintOps {

    private static PrefixMap displayPrefixMap = PrefixMapFactory.createForOutput();
    static {
        displayPrefixMap.add("owl",  OWL.getURI());
        displayPrefixMap.add("rdf",  RDF.getURI());
        displayPrefixMap.add("rdfs", RDFS.getURI());
        displayPrefixMap.add("xsd",  XSD.getURI());
    }

    public static void print(ShexPrintable printable) {
        IndentedWriter iOut = IndentedWriter.clone(IndentedWriter.stdout);
        NodeFormatter nFmt = new NodeFormatterTTL(null, PrefixMapFactory.create(SSE.getPrefixMapRead()));
        printable.print(iOut, nFmt);
    }

    public static NodeFormatter nodeFmtAbbrev = new NodeFormatterTTL(null, displayPrefixMap);

    public static String asString(ShexPrintable printable) {
        IndentedLineBuffer x = new IndentedLineBuffer();
        x.setFlatMode(true);
        printable.print(x, nodeFmtAbbrev);
        return x.asString();
    }

}
