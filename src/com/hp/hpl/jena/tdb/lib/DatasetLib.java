/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.riot.RiotException ;
import com.hp.hpl.jena.riot.lang.LangRIOT ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.shared.LockMRSW ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph ;
import com.hp.hpl.jena.sparql.util.IndentedWriter ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;

public class DatasetLib
{
    public static void dump(DatasetGraph dataset)
    {
        WriterGraph.output(IndentedWriter.stdout, dataset, null) ;
        IndentedWriter.stdout.flush();
    }
    
    /**
     * Return true if the datasets are isomorphic - same names for graphs, graphs isomorphic. 
     */
    public static boolean isomorphic(Dataset dataset1, Dataset dataset2)
    {
        return isomorphic(dataset1.asDatasetGraph(), dataset2.asDatasetGraph()) ;
    }
    
    /**
     * Return true if the datasets are isomorphic - same names for graphs, graphs isomorphic. 
     */
    public static boolean isomorphic(DatasetGraph dataset1, DatasetGraph dataset2)
    {
        int x1 = dataset1.size() ;
        int x2 = dataset2.size() ;
        if ( x1 >=0 && x1 != x2 )
            return false ;
        
        boolean b = dataset1.getDefaultGraph().isIsomorphicWith(dataset2.getDefaultGraph()) ;
        if ( ! b )
            return b ;
        
        for ( Iterator<Node> iter1 = dataset1.listGraphNodes() ; iter1.hasNext() ; )
        {
            Node gn = iter1.next() ;
            Graph g1 = dataset1.getGraph(gn) ;
            Graph g2 = dataset2.getGraph(gn) ;
            if ( g2 == null )
                return false ;
            if ( ! g1.isIsomorphicWith(g2) )
                return false ;
        }
        
        return true ;
    }
    
    public static void read(InputStream input, DatasetGraph dataset, String language)
    {
        Sink<Quad> sink = datasetSink(dataset) ;
        
        
        // NAMES!
        if ( language.equalsIgnoreCase("NQUADS") || language.equalsIgnoreCase("N-QUADS") )
        {
            LangRIOT parser = Lang.createParserNQuads(input, sink) ;
            parser.parse() ;
            sink.flush();
            return ;
        }
//        if ( language.equalsIgnoreCase("TRIG") )
//        {}
        throw new RiotException("Language not supported for quads: "+language) ;
    }
    
    /** Work in progress */
    public static Sink<Quad> datasetSink(DatasetGraph dataset)
    {
        return new QuadsToDataset(dataset) ;
    }
    
    // A DatasetGraph that creates memort graphs on mention */
    public static DatasetGraph createDatasetGraphMem()
    {
        return new DatasetGraphMem() ;
    }
    
    private static class DatasetGraphMem implements DatasetGraph
    {
        Graph defaultGraph = newGraph() ;
        Map<Node, Graph> graphs = new HashMap<Node, Graph>() ;
        
        public boolean containsGraph(Node graphNode)
        {
            return true ;
        }

        public Graph getDefaultGraph()
        {
            return defaultGraph ;
        }

        public Graph getGraph(Node graphNode)
        {
            Graph g = graphs.get(graphNode) ;
            if ( g == null )
            {
                g = newGraph() ;
                graphs.put(graphNode, g) ;
            }
            
            return g ;
        }

        private Lock lock = new LockMRSW() ;
        public Lock getLock()
        {
            return lock ;
        }

        public Iterator<Node> listGraphNodes()
        {
            return graphs.keySet().iterator() ;
        }

        public int size()
        {
            return graphs.size() ;
        }

        public void close()
        {}
        
        private static Graph newGraph()
        {
            return Factory.createDefaultGraph() ; 
        }
    }
    
    // Not sure yet where this wil go.
    /** @See SinkToGraphTriples */ 
    private static class QuadsToDataset implements Sink<Quad>
    {
        
        private final DatasetGraph dataset ;
        private Node graphNode = null ;
        private Graph graph = null ;

        QuadsToDataset(DatasetGraph dataset)
        {
            this.dataset = dataset ;
        }
        
        public void send(Quad quad)
        {
            if ( graph == null || ! Utils.equal(quad.getGraph(), graphNode) )
            {
                // graph == null ==> Uninitialized
                // not equals ==> different graph to last time.
                graphNode = quad.getGraph() ;
                if ( quad.isTriple() )
                    graph = dataset.getDefaultGraph() ;
                else
                    graph = dataset.getGraph(graphNode) ;
            }
            graph.add(quad.asTriple()) ;
        }

        public void flush()
        {
            TDB.sync(dataset) ;
        }

        public void close()
        {}
        
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