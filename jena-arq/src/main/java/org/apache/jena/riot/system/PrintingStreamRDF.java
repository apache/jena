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

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.writer.WriterStreamRDFFlat;
import org.apache.jena.riot.writer.WriterStreamRDFPlain;
import org.apache.jena.sparql.core.Quad;

/**
 * A StreamRDF which displays the items sent to the stream.
 * It is primarily for development purposes.
 * <p>
 * The output is not a legal syntax. Do not consider this
 * format to be stable.
 * <p>
 * It is not optimized for throughput and it flushes every line.
 * Consider using {@link WriterStreamRDFFlat} for performance.
 * <p>
 *
 *
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
        // Always flush on each items.
        // Too many points provide buffering or automatic newline
        // handling  in different ways to get implicit consistent behaviour.
        // This is a development helper.
    }

    public PrintingStreamRDF(AWriter out) {
        super(out);
    }

    @Override
    protected NodeFormatter getFmt() { return pretty; }

    // No prefix formatting.
    private static void printDirectURI(AWriter out, String iriStr) {
        out.print("<") ;
        out.print(iriStr) ;
        out.print(">") ;
    }

    @Override
    public void base(String base) {
        out.print("BASE") ;
        out.print("  ") ;
        printDirectURI(out, base);
        out.println();
        flush();
        // Reset the formatter because of the new base URI.
        pretty = new NodeFormatterTTL(base, prefixMap);
    }

    @Override
    public void prefix(String prefix, String iri) {
        out.print("PREFIX") ;
        out.print("  ") ;
        out.print(prefix) ;
        out.print(":  ") ;
        printDirectURI(out, iri);
        out.println();
        prefixMap.add(prefix, iri);
        flush();
    }

    @Override
    public void triple(Triple triple) {
        super.triple(triple);
        flush();
    }

    @Override
    public void quad(Quad quad) {
        super.quad(quad);
        flush();
    }

    public void flush() {
        IO.flush(out) ;
    }
}
