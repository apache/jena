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

package org.apache.jena.riot.system;

import java.io.OutputStream ;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.writer.WriterStreamRDFPlain;

/**
 * A StreamRDF which displays the items sent to the stream. It is primarily for
 * development purposes.
 * <p>
 * The output is not a legal syntax. Do not consider this
 * format to be stable. It is "N-Quads with abbreviations".
 * <p>
 * Use via
 * <pre>
 * StreamRDFLib.print(System.out);
 * </pre>
 */
public class PrintingStreamRDF extends WriterStreamRDFPlain
{
    private PrefixMap prefixMap = PrefixMapFactory.create();
    private NodeFormatter pretty =  new NodeFormatterTTL(null, prefixMap);

    public PrintingStreamRDF(OutputStream out) {
        super(IO.wrapUTF8(out));
    }

    @Override
    protected NodeFormatter getFmt() { return pretty; }

    @Override
    public void base(String base) {
        out.print("BASE") ;
        out.print("    ") ;
        getFmt().formatURI(out, base);
        out.println();
        // Reset the formatter because of the new base URI.
        pretty = new NodeFormatterTTL(base, prefixMap);
    }

    @Override
    public void prefix(String prefix, String iri) {
        out.print("PREFIX") ;
        out.print("  ") ;
        out.print(prefix) ;
        out.print(":  ") ;
        getFmt().formatURI(out, iri);
        out.println();
        prefixMap.add(prefix, iri);
    }
}
