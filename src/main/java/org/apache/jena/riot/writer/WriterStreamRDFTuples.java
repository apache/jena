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

package org.apache.jena.riot.writer;

import java.io.OutputStream ;
import java.io.Writer ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.riot.out.NodeFormatter ;
import org.apache.jena.riot.out.NodeFormatterNT ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** An output of triples / quads that is streaming.
 *  It writes N-triples/N-quads.
 */

public class WriterStreamRDFTuples implements StreamRDF
{
    private final IndentedWriter out ;
    
    public WriterStreamRDFTuples(OutputStream outs)
    {
        this(new IndentedWriter(outs)) ;
    }

    public WriterStreamRDFTuples(Writer w)
    {
        this(RiotLib.create(w)) ;
    }

    public WriterStreamRDFTuples(IndentedWriter w)
    {
        out = w ;
    }
    
    @Override
    public void start()
    {}

    @Override
    public void finish()
    { IO.flush(out) ; }

    @Override
    public void triple(Triple triple)
    {
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
    public void quad(Quad quad)
    {
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        Node g = quad.getGraph() ;

        format(s) ;
        out.print(" ") ;
        format(p) ;
        out.print(" ") ;
        format(o) ;

        if ( outputGraphSlot(g) ) 
        {
            out.print(" ") ;
            format(g) ;
        }
        out.print(" .\n") ;
    }

    @Override
    public void tuple(Tuple<Node> tuple)
    {
        boolean first = true ;
        for ( Node n : tuple )
        {
            if ( ! first )
                out.print(" ") ;
            first = false ;
            format(n) ;
        }
        out.print(" .\n") ;
    }

    private static final NodeFormatter nodeFmt = new NodeFormatterNT() ;

    private void format(Node n)
    {
        nodeFmt.format(out, n) ;
    }

    @Override
    public void base(String base)
    {}

    @Override
    public void prefix(String prefix, String iri)
    {}

    private static boolean outputGraphSlot(Node g)
    {
        return ( g != null && g != Quad.tripleInQuad && ! Quad.isDefaultGraph(g) ) ;
    }
}
