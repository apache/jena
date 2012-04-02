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

package org.openjena.riot.out;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.Writer ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.system.Prologue ;
import org.openjena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** A class that print quads, N-Quads style */ 
public class SinkQuadOutput implements Sink<Quad>
{
    private Prologue prologue = null ;
    private Writer out ;
    private NodeToLabel labelPolicy = null ;
    private NodeFormatter nodeFmt = new NodeFormatterNT() ;

    public SinkQuadOutput(OutputStream outs)
    {
        this(outs, null, SyntaxLabels.createNodeToLabel()) ;
    }
    
    public SinkQuadOutput(OutputStream outs, Prologue prologue, NodeToLabel labels)
    {
        out = IO.asBufferedUTF8(outs) ;
        setPrologue(prologue) ;
        setLabelPolicy(labels) ;
    }
    
    // Need to do this later sometimes to sort out the plumbing.
    public void setPrologue(Prologue prologue)
    {
        this.prologue = prologue ;
    }
    
    public void setLabelPolicy(NodeToLabel labels)
    {
        this.labelPolicy = labels ;
    }

    @Override
    public void send(Quad quad)
    {
        try {
            Node s = quad.getSubject() ;
            Node p = quad.getPredicate() ;
            Node o = quad.getObject() ;
            Node g = quad.getGraph() ;

            nodeFmt.format(out, s) ;
            out.write(" ") ;
            nodeFmt.format(out, p) ;
            out.write(" ") ;
            nodeFmt.format(out, o) ;

            if ( outputGraphSlot(g) ) 
            {
                out.write(" ") ;
                nodeFmt.format(out, g) ;
            }

            out.write(" .\n") ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }
    
    private static boolean outputGraphSlot(Node g)
    {
        return ( g != null && g != Quad.tripleInQuad && ! Quad.isDefaultGraph(g) ) ;
    }

    @Override
    public void flush()
    {
        try { out.flush() ; } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void close()
    { 
        IO.flush(out) ;
        // Don't close the underlying OutputStream that was passed in.
    }
}
