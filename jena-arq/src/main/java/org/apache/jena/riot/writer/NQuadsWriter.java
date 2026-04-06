/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.riot.writer;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

public class NQuadsWriter extends WriterDatasetRIOTBase {

    public static void write(OutputStream out, Iterator<Quad> iter) {
        write(out, iter, CharSpace.UTF8);
    }

    public static void write(OutputStream out, Iterator<Quad> iter, CharSpace charSpace) {
        StreamRDF s = StreamRDFLib.writer(out, charSpace);
        write$(s, iter);
    }

    /** @deprecated Use RIOT and language {@link Lang#NQUADS} */
    @Deprecated(forRemoval = true)
    public static void write(Writer out, Iterator<Quad> iter) {
        write(out, iter, CharSpace.UTF8);
    }

    /** @deprecated Use RIOT and language {@link Lang#NQUADS} */
    @Deprecated(forRemoval = true)
    public static void write(Writer out, Iterator<Quad> iter, CharSpace charSpace) {
        StreamRDF s = StreamRDFLib.writer(out, charSpace);
        write$(s, iter);
    }

    private static void write$(StreamRDF s, Iterator<Quad> iter) {
        s.start();
        StreamRDFOps.sendQuadsToStream(iter, s);
        s.finish();
    }

    protected final CharSpace charSpace;

    public NQuadsWriter() {
        this(CharSpace.UTF8);
    }

    public NQuadsWriter(CharSpace charSpace) {
        this.charSpace = charSpace;
    }

    @Override
    public Lang getLang() {
        return Lang.NQUADS;
    }

    @Override
    public void write(Writer out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) {
        Iterator<Quad> iter = dataset.find();
        NodeFormatter nodeFmt = createNodeFormatter();
        AWriter w = IO.wrap(out);
        StreamRDF s = new WriterStreamRDFPlain(IO.wrap(out), nodeFmt);
        write$(s, iter);
    }

    @Override
    public void write(OutputStream out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) {
        Iterator<Quad> iter = dataset.find();
        NodeFormatter nodeFmt = createNodeFormatter();
        AWriter w = createAWriter(out);
        StreamRDF s = new WriterStreamRDFPlain(w, nodeFmt);
        write$(s, iter);
    }

    protected NodeFormatter createNodeFormatter() {
        NodeFormatter nodeFmt = new NodeFormatterNT(charSpace);
        return nodeFmt;
    }

    protected AWriter createAWriter(OutputStream out) {
        return switch(charSpace) {
            case ASCII -> IO.wrapASCII(out);
            case UTF8 -> IO.wrapUTF8(out);
        };
    }
}
