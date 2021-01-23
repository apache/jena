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

package org.apache.jena.sparql.system ;

import java.io.OutputStream ;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.writer.WriterStreamRDFPlain ;

/**
 * A class that print triples, N-triples style
 * 
 * @see WriterStreamRDFPlain
 * @see RDFDataMgr#writeTriples
 */
public class SinkTripleOutput implements Sink<Triple>
{
    private final AWriter out ;
    private NodeToLabel   labelPolicy = null ;

    private NodeFormatter nodeFmt     = new NodeFormatterNT() ;

    public SinkTripleOutput(OutputStream outs, NodeToLabel labels) {
        out = IO.wrapUTF8(outs) ;
        setLabelPolicy(labels) ;
    }

    public void setLabelPolicy(NodeToLabel labels) {
        this.labelPolicy = labels ;
    }

    @Override
    public void send(Triple triple) {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;

        nodeFmt.format(out, s) ;
        out.print(" ") ;
        nodeFmt.format(out, p) ;
        out.print(" ") ;
        nodeFmt.format(out, o) ;
        out.print(" .\n") ;
    }

    @Override
    public void flush() {
        IO.flush(out) ;
    }

    @Override
    public void close() {
        IO.flush(out) ;
        // Don't close the underlying OutputStream that was passed in.
    }

}
