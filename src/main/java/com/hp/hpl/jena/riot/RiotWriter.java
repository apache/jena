/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.io.IOException ;
import java.io.OutputStreamWriter ;
import java.io.PrintStream ;
import java.io.UnsupportedEncodingException ;
import java.io.Writer ;
import java.util.Iterator ;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import static com.hp.hpl.jena.riot.out.OutputLangUtils.* ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class RiotWriter
{
    // Work in progress
    public static void writeNQuads(PrintStream out, DatasetGraph dsg)
    {
        Writer w ;
        try
        {
            w = new OutputStreamWriter(out, "ASCII") ;
        } catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            return ;
        }
        
        outputOneGraph(w, dsg.getDefaultGraph(), null) ;
        
        Iterator<Node> iterGraphs = dsg.listGraphNodes() ;
        for ( ; iterGraphs.hasNext(); )
        {
            Node gn = iterGraphs.next() ;
            outputOneGraph(w, dsg.getGraph(gn), gn) ;
        }
        try { w.flush() ; } catch (IOException ex) { }
        
    }
    
    private static void outputOneGraph(Writer w, Graph graph, Node graphNode)
    {
        ExtendedIterator<Triple> iter = graph.find(null, null, null) ;
        for ( ; iter.hasNext() ; )
        {
            Triple triple = iter.next();
            output(w, triple, graphNode, null) ;
        }
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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