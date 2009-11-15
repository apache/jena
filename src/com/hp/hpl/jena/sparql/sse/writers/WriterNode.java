/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.writers;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class WriterNode
{
    private static final int NL = WriterLib.NL ;
    private static final int NoNL = WriterLib.NoNL ;
    private static final int NoSP = WriterLib.NoSP ;
    
    public static void output(IndentedWriter out, Triple triple, SerializationContext naming)
    {
        WriterLib.start(out, Tags.tagTriple, NoNL) ;
        outputPlain(out, triple, naming) ;
        WriterLib.finish(out, Tags.tagTriple) ;
    }
    
    public static void outputPlain(IndentedWriter out, Triple triple, SerializationContext naming)
    {
        // No tag
        output(out, triple.getSubject(), naming) ;
        out.print(" ") ;
        output(out, triple.getPredicate(), naming) ;
        out.print(" ") ;
        output(out, triple.getObject(), naming) ;
    }
    
    public static void output(IndentedWriter out, Quad qp, SerializationContext naming)
    {
        WriterLib.start(out, Tags.tagQuad, NoNL) ;
        outputPlain(out, qp, naming) ;
        WriterLib.finish(out, Tags.tagQuad) ;
    }
    
    public static void outputPlain(IndentedWriter out, Quad qp, SerializationContext naming)
    {
        output(out, qp.getGraph(), naming) ;
        out.print(" ") ;
        output(out, qp.getSubject(), naming) ;
        out.print(" ") ;
        output(out, qp.getPredicate(), naming) ;
        out.print(" ") ;
        output(out, qp.getObject(), naming) ;
    }
    
    public static void output(IndentedWriter out, Node node, SerializationContext naming)
    {
        out.print(FmtUtils.stringForNode(node, naming)) ;
    }
    
    public static void output(IndentedWriter out, List<Node> nodeList, SerializationContext naming)
    {
        out.print("(") ;
        boolean first = true ;
        for ( Node node : nodeList )
        {
            if ( ! first )
                out.print(" ") ;
            out.print(FmtUtils.stringForNode(node, naming)) ;
            first = false ;
        }
        out.print(")") ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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