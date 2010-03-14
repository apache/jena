/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import java.util.Iterator ;

import org.openjena.atlas.lib.Sink ;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphMem ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph ;
import com.hp.hpl.jena.sparql.util.IndentedWriter ;

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
    
    /** Work in progress */
    public static Sink<Quad> datasetSink(DatasetGraph dataset)
    {
        return new SinkQuadsToDataset(dataset) ;
    }
    
    // A DatasetGraph that creates memory graphs on mention */
    public static DatasetGraph createDatasetGraphMem()
    {
        return new DatasetGraphMem() ;
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