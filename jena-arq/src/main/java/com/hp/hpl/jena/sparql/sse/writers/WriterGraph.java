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

package com.hp.hpl.jena.sparql.sse.writers;

import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

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
        
//        out.println() ;
//        out.incIndent() ;
        boolean first = true ; 
        for ( ; iter.hasNext() ; )
        {
//            if ( ! first )
                out.println();
            first = false ;
            Triple triple = iter.next();
            WriterNode.output(out, triple, naming) ;
        }
//        out.decIndent() ;
        if ( ! first ) out.println();
        WriterLib.finish(out, Tags.tagGraph) ;
    }
}
