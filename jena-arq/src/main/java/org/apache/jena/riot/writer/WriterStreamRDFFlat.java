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

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** An output of triples / quads that is streaming.
 *  It writes one line per triple/quads, with prefixes and literal short forms.
 */

public class WriterStreamRDFFlat extends WriterStreamRDFBase
{
    public WriterStreamRDFFlat(OutputStream output)
    { 
        super(output) ;
    }

    public WriterStreamRDFFlat(IndentedWriter output)
    { 
        super(output) ;
    }

    public WriterStreamRDFFlat(Writer output)
    { 
        super(output) ;
    }

    @Override
    protected void startData()
    {}

    @Override
    protected void endData()
    {}

    @Override
    protected void reset()
    {}

    @Override
    protected void print(Triple triple)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        outputNode(s) ;
        out.print(' ') ;
        outputNode(p) ;
        out.print(' ') ;
        outputNode(o) ;
        out.println(" .") ;
    }

    @Override
    protected void print(Quad quad)
    {
        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        
        if ( g != null && ! Quad.isDefaultGraph(g) )
        {
            outputNode(g) ;
            out.print(" { ") ;
        }
        else
            out.print("{ ") ;
        outputNode(s) ;
        out.print(' ') ;
        outputNode(p) ;
        out.print(' ') ;
        outputNode(o) ;
        out.println(" }") ;
    }
}
