/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.writers;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class WriterGraph
{
    public static final int NL = WriterLib.NL ;
    public static final int NoNL = WriterLib.NoNL ;
    public static final int NoSP = WriterLib.NoSP ;
    
    public static void output(IndentedWriter out, Graph graph, SerializationContext naming)
    { output(out, graph, null, naming) ; }
    
    public static void output(IndentedWriter out, Graph graph, String uri, SerializationContext naming)
    { writeGraph(out, graph, uri, naming) ; }

    public static void output(IndentedWriter out, DatasetGraph dataset, SerializationContext naming)
    { writeDataset(out, dataset, naming) ; }

    public static void output(IndentedWriter out, BasicPattern pattern, SerializationContext sCxt)
    {
        for ( Triple triple : pattern )
        {
            WriterNode.output(out, triple, sCxt) ;
            out.println() ;
        }
    }
    
    // ---- Workers
    
    private static void writeDataset(IndentedWriter out, DatasetGraph ds, SerializationContext naming)
    {
        WriterLib.start(out, Tags.tagDataset, NL) ;
        writeGraph(out, ds.getDefaultGraph(), naming) ;
        out.ensureStartOfLine() ;
        for ( Iterator<Node> iter = ds.listGraphNodes() ; iter.hasNext() ; )
        {
            Node node = iter.next() ;  
            out.ensureStartOfLine() ;
            Graph g = ds.getGraph(node) ;
            writeGraph(out, g, node, naming) ;
        }
        WriterLib.finish(out, Tags.tagDataset) ;
        out.ensureStartOfLine() ;
    }
    
//    private static void writeModel(OutputContext out, Model m)
//    { writeGraph(out, m.getGraph(), null) ; }
    
    private static void writeGraph(IndentedWriter out, Graph g, SerializationContext naming)
    { _writeGraph(out, g,  null, naming) ; }

//    private static void writeGraph(OutputContext out, Model m, String uri)
//    { writeGraph(out, m.getGraph(), uri) ; }
    
    private static void writeGraph(IndentedWriter out, Graph g, String uri, SerializationContext naming)
    {
        String x = null ;
        if ( uri != null )
            x = FmtUtils.stringForURI(uri) ;
        _writeGraph(out, g, x, naming) ;
    }
    
    private static void writeGraph(IndentedWriter out, Graph g, Node node, SerializationContext naming)
    { 
        String x = null ;
        if ( node != null )
            x = FmtUtils.stringForNode(node) ;
        _writeGraph(out, g, x, naming) ;
    }

    private static void _writeGraph(IndentedWriter out, Graph g, String label, SerializationContext naming)
    {
        WriterLib.start(out, Tags.tagGraph, NoSP) ;
        if ( label != null )
        {
            out.print(" ") ;
            out.print(label) ;
        }
        
        Iterator<Triple> iter = g.find(Node.ANY, Node.ANY, Node.ANY) ;
        if ( ! iter.hasNext() )
        {
            // Empty.
            WriterLib.finish(out, Tags.tagGraph) ;
            return ;
        }
        
        out.println() ;
        out.incIndent() ;
        boolean first = true ; 
        for ( ; iter.hasNext() ; )
        {
            if ( ! first )
                out.println();
            first = false ;
            Triple triple = iter.next();
            WriterNode.output(out, triple, naming) ;
        }
        out.decIndent() ;
        if ( ! first ) out.println();
        WriterLib.finish(out, Tags.tagGraph) ;
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