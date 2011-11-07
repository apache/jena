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

import java.io.OutputStream ;
import java.nio.charset.CharsetEncoder ;

import org.openjena.atlas.io.BufferingWriter ;
import org.openjena.atlas.lib.Chars ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.system.Prologue ;
import org.openjena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** A class that print triples, N-triples style */ 
public class SinkTripleOutput implements Sink<Triple>
{
    // TODO ASCII version
    private CharsetEncoder encoder ;
    private Prologue prologue = null ;
    private BufferingWriter out ;
    private NodeToLabel labelPolicy = null ;
    
    private NodeFormatter nodeFmt = new NodeFormatterNT() ;

    public SinkTripleOutput(OutputStream outs)
    {
        this(outs, null, SyntaxLabels.createNodeToLabel()) ;
    }
    
    public SinkTripleOutput(OutputStream outs, Prologue prologue, NodeToLabel labels)
    {
        //encoder = Chars.charsetUTF8.newEncoder() ;
        encoder = Chars.charsetASCII.newEncoder() ;
        out = BufferingWriter.create(outs) ;
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
    public void flush()
    {
        out.flush() ;
    }

    @Override
    public void send(Triple triple)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        nodeFmt.format(out, s) ;
        out.output(" ") ;
        nodeFmt.format(out, p) ;
        out.output(" ") ;
        nodeFmt.format(out, o) ;
        out.output(" .\n") ;
    }

    @Override
    public void close()
    {
        flush();
    }
}
