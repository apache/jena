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

package org.apache.jena.riot.system;

import static org.apache.jena.riot.writer.WriterConst.PREFIX_IRI ;
import static org.apache.jena.riot.writer.WriterConst.RDF_type ;
import static org.apache.jena.riot.writer.WriterConst.rdfNS ;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.WriterDatasetRIOT ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.writer.WriterGraphRIOTBase ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** Support code for the RIOT */ 
public class RiotWriterLib
{
    public static Collection<Triple> triplesOfSubject(Graph graph, Node subj)
    {
        return triples(graph, subj, Node.ANY, Node.ANY) ;
    }

    /* Get all the triples for the graph.find */
    public static List<Triple> triples(Graph graph, Node s, Node p, Node o)
    {
        List<Triple> acc =  new ArrayList<Triple>() ;
        accTriples(acc, graph, s, p, o) ;
        return acc ;
    }

    /* Get all the triples for the graph.find */
    public static long countTriples(Graph graph, Node s, Node p, Node o)
    {
        ExtendedIterator<Triple> iter = graph.find(s, p, o) ;
        try { return Iter.count(iter) ; }
        finally { iter.close() ; }
    }

    public static void accTriples(Collection<Triple> acc, Graph graph, Node s, Node p, Node o)
    {
        ExtendedIterator<Triple> iter = graph.find(s, p, o) ;
        for ( ; iter.hasNext() ; )
            acc.add(iter.next()) ;
        iter.close() ;
    }

    /* Get a triple or null. */
    public static Triple triple1(Graph graph, Node s, Node p, Node o)
    {
        ExtendedIterator<Triple> iter = graph.find(s, p, o) ;
        try {
            if ( ! iter.hasNext() )
                return null ;
            Triple t = iter.next() ;
            if (  iter.hasNext() )
                return null ;
            return t ;
        } finally { iter.close() ; }
    }

    public static boolean strSafeFor(String str, char ch) { return str.indexOf(ch) == -1 ; }

    public static void writeBase(IndentedWriter out, String base)
    {
        if ( base != null )
        {
            out.print("@base ") ;
            out.pad(PREFIX_IRI) ;
            out.print("<") ;
            out.print(base) ;
            out.print(">") ;
            out.print(" .") ;
            out.println() ;
        }
    }
    
    public static void writePrefixes(IndentedWriter out, PrefixMap prefixMap)
    {        
        if ( prefixMap != null && ! prefixMap.isEmpty() )
        {
            for ( Map.Entry <String, String> e : prefixMap.getMappingCopyStr().entrySet() )
            {
                out.print("@prefix ") ;
                out.print(e.getKey()) ;
                out.print(": ") ;
                out.pad(PREFIX_IRI) ;
                out.print("<") ;
                out.print(e.getValue()) ;
                out.print(">") ;
                out.print(" .") ;
                out.println() ;
            }
        }
    }

    /** Returns dataset that wraps a graph */
    public static DatasetGraph dataset(Graph graph)
    {
        return DatasetGraphFactory.createOneGraph(graph) ;
    }        
    
    public static PrefixMap prefixMap(DatasetGraph dsg)
    {
        return PrefixMapFactory.create(dsg.getDefaultGraph().getPrefixMapping()) ;
    }
    
    private static int calcWidth(PrefixMap prefixMap, String baseURI, Node p)
    {
        if ( ! prefixMap.contains(rdfNS) && RDF_type.equals(p) )
            return 1 ;
        
        String x = prefixMap.abbreviate(p.getURI()) ;
        if ( x == null )
            return p.getURI().length()+2 ;
        return x.length() ;
    }
    
    public static int calcWidth(PrefixMap prefixMap, String baseURI, Collection<Node> nodes, int minWidth, int maxWidth)
    {
        Node prev = null ; 
        int nodeMaxWidth = minWidth ;
        
        for ( Node n : nodes )
        {
            if ( prev != null && prev.equals(n) )
                continue ;
            int len = calcWidth(prefixMap, baseURI, n) ;
            if ( len > maxWidth )
                continue ;
            if ( nodeMaxWidth < len )
                nodeMaxWidth = len ;
            prev = n ;
        }
        return nodeMaxWidth ; 
    }
    
    public static int calcWidthTriples(PrefixMap prefixMap, String baseURI, Collection<Triple> triples, int minWidth, int maxWidth)
    {
        Node prev = null ; 
        int nodeMaxWidth = minWidth ;

        for ( Triple triple : triples )
        {
            Node n = triple.getPredicate() ;
            if ( prev != null && prev.equals(n) )
                continue ;
            int len = calcWidth(prefixMap, baseURI, n) ;
            if ( len > maxWidth )
                continue ;
            if ( nodeMaxWidth < len )
                nodeMaxWidth = len ;
            prev = n ;
        }
        return nodeMaxWidth ;
    }

    
    private static class IndentedWriterWriter extends IndentedWriter
    {
        IndentedWriterWriter(Writer w) { super(w) ; }
    }

    public static IndentedWriter create(Writer writer)  { return new IndentedWriterWriter(writer) ; }

    public static PrefixMap prefixMap(Graph graph)      { return PrefixMapFactory.create(graph.getPrefixMapping()) ; }
    
    public static WriterGraphRIOTBase adapter(WriterDatasetRIOT writer)
    { return new WriterAdapter(writer) ; }
    
    private static class WriterAdapter extends WriterGraphRIOTBase
    {
        private WriterDatasetRIOT writer ;

        WriterAdapter(WriterDatasetRIOT writer) { this.writer = writer ; }
        @Override
        public Lang getLang()
        { return writer.getLang() ; }

        @Override
        public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
        { writer.write(out, RiotWriterLib.dataset(graph), prefixMap, baseURI, context) ; }
        
        @Override
        public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
        { writer.write(out, RiotWriterLib.dataset(graph), prefixMap, baseURI, context) ; }
    }
}

