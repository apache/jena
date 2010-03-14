/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.out;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetEncoder;

import org.openjena.atlas.io.BufferingWriter ;
import org.openjena.atlas.lib.Chars ;
import org.openjena.atlas.lib.Sink ;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.riot.Prologue;

/** A class that print triples, N-triples style */ 
public class SinkTripleOutput implements Sink<Triple>
{
    private CharsetEncoder encoder ;
    private Prologue prologue = null ;
    private BufferingWriter out ;

    public SinkTripleOutput(OutputStream outs)
    {
        this(outs, null) ;
    }
    
    public SinkTripleOutput(OutputStream outs, Prologue prologue)
    {
        encoder = Chars.charsetUTF8.newEncoder() ;
        Sink<ByteBuffer> dest = new BufferingWriter.SinkOutputStream(outs) ; 
        out = new BufferingWriter(dest) ;
        setPrologue(prologue) ;
    }
    
    // Need to do this later sometimes to sort out the plumbing.
    public void setPrologue(Prologue prologue)
    {
        this.prologue = prologue ;
    }
    
    public void flush()
    {
        out.flush() ;
    }

    private Node lastS = null ;
    private Node lastP = null ;
    private Node lastO = null ;
    
    public void send(Triple triple)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
//        if ( ! ( s.isURI() || s.isBlank() ) )
//            throw new TurtleParseException("["+line+", "+col+"] : Error: Subject is not a URI or blank node") ;
//        if ( ! p.isURI() )
//            throw new TurtleParseException("["+line+", "+col+"] : Error: Predicate is not a URI") ;
//        if ( ! ( o.isURI() || o.isBlank() || o.isLiteral() ) ) 
//            throw new TurtleParseException("["+line+", "+col+"] : Error: Object is not a URI, blank node or literal") ;
      
        if ( false )
        {
            if ( s.equals(lastS) )
                out.output("*") ;
            else
                OutputLangUtils.output(out, s, prologue) ;
            
            out.output(" ") ;
            
            if ( p.equals(lastP) )
                out.output("*") ;
            else
                OutputLangUtils.output(out, p, prologue) ;
    
            out.output(" ") ;
    
            if ( o.equals(lastO) )
                out.output("*") ;
            else
                OutputLangUtils.output(out, o, prologue) ;
            out.output(" .") ;
            out.output("\n") ;
            
            lastS = s ;
            lastP = p ;
            lastO = o ;
            return ;
        }

        // N-triples.
        OutputLangUtils.output(out, s, prologue) ;
        out.output(" ") ;
        OutputLangUtils.output(out, p, prologue) ;
        out.output(" ") ;
        OutputLangUtils.output(out, o, prologue) ;
        out.output(" .") ;
        out.output("\n") ;
    }

    public void close()
    {
        flush();
    }
}
/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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