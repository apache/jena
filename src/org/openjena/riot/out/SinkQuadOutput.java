/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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
import com.hp.hpl.jena.sparql.core.Quad ;

/** A class that print quads, N-Quads style */ 
public class SinkQuadOutput implements Sink<Quad>
{
    private CharsetEncoder encoder ;
    private Prologue prologue = null ;
    private BufferingWriter out ;
    private NodeToLabel labelPolicy = null ;
    private NodeFormatter nodeFmt = new NodeFormatterNT() ;

    public SinkQuadOutput(OutputStream outs)
    {
        this(outs, null, SyntaxLabels.createNodeToLabel()) ;
    }
    
    public SinkQuadOutput(OutputStream outs, Prologue prologue, NodeToLabel labels)
    {
        encoder = Chars.charsetUTF8.newEncoder() ;
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


    public void flush()
    {
        out.flush() ;
    }

    public void send(Quad quad)
    {
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        Node g = quad.getGraph() ;
        
        nodeFmt.format(out, s) ;
        out.output(" ") ;
        nodeFmt.format(out, p) ;
        out.output(" ") ;
        nodeFmt.format(out, o) ;
        
        if ( outputGraphSlot(g) ) 
        {
            out.output(" ") ;
            nodeFmt.format(out, g) ;
        }
            
        out.output(" .\n") ;
    }
    
    private static boolean outputGraphSlot(Node g)
    {
        return ( g != null && g != Quad.tripleInQuad && ! Quad.isDefaultGraph(g) ) ;
    }

    public void close()
    {
        flush();
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */