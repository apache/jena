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

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.out.CharSpace ;
import org.apache.jena.riot.out.NodeFormatter ;
import org.apache.jena.riot.out.NodeFormatterNT ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/**
 * An output of triples / quads that is streaming. It writes N-triples/N-quads.
 */

public class WriterStreamRDFPlain implements StreamRDF {
    // This class is the overall structure - the NodeFormatter controls the
    // appearance of the Nodes themselves.

    protected final AWriter       out ;
    protected final NodeFormatter nodeFmt ;

    /**
     * Output tuples, using UTF8 output See {@linkplain StreamRDFLib#writer} for
     * ways to create a AWriter object.
     */
    public WriterStreamRDFPlain(AWriter w) {
        this(w, CharSpace.UTF8) ;
    }

    /**
     * Output tuples, choosing ASCII or UTF8 See
     * {@linkplain StreamRDFLib#writer} for ways to create a AWriter object.
     */
    public WriterStreamRDFPlain(AWriter w, CharSpace charSpace) {
        out = w ;
        nodeFmt = new NodeFormatterNT(charSpace) ;
    }

    @Override
    public void start() {}

    @Override
    public void finish() {
        IO.flush(out) ;
    }

    @Override
    public void triple(Triple triple) {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;

        format(s) ;
        out.print(" ") ;
        format(p) ;
        out.print(" ") ;
        format(o) ;
        out.print(" .\n") ;
    }

    @Override
    public void quad(Quad quad) {
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        Node g = quad.getGraph() ;

        format(s) ;
        out.print(" ") ;
        format(p) ;
        out.print(" ") ;
        format(o) ;

        if ( outputGraphSlot(g) ) {
            out.print(" ") ;
            format(g) ;
        }
        out.print(" .\n") ;
    }

    private void format(Node n) {
        nodeFmt.format(out, n) ;
    }

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {}

    private static boolean outputGraphSlot(Node g) {
        return (g != null && g != Quad.tripleInQuad && !Quad.isDefaultGraph(g)) ;
    }
}
