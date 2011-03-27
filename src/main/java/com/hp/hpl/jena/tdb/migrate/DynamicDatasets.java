/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.migrate;

import java.util.Collection ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphMap ;

public class DynamicDatasets
{
//    // See QueryEngineTDB.dynamicDatasetOp
//    public static Op dynamicDatasetOp(Query query, Op op)
//    {
    // problems with paths and property functions? which access the active graph. 
//        return TransformDynamicDataset.transform(query, op) ;
//    }
    
    /** Given a DatasetGraph and a query, form a DatasetGraph that 
     * is the dynamic dataset from the query.
     * Returns the original DatasetGraph if the query has no dataset description.
     */ 
    public static DatasetGraph dynamicDataset(Query query, DatasetGraph dsg)
    {
        if ( query.hasDatasetDescription() )
        {
            Set<Node> defaultGraphs = NodeUtils2.convertToNodes(query.getGraphURIs()) ; 
            Set<Node> namedGraphs = NodeUtils2.convertToNodes(query.getNamedGraphURIs()) ;
            return dynamicDataset(defaultGraphs, namedGraphs, dsg) ; 
        }
        return dsg ;
    }
    
    
    /** Given a Dataset and a query, form a Dataset that 
     * is the dynamic dataset from the query.
     * Returns the original Dataset if the query has no dataset description.
     */ 
    public static Dataset dynamicDataset(Query query, Dataset ds)
    {
        DatasetGraph dsg = ds.asDatasetGraph() ;
        DatasetGraph dsg2 = dynamicDataset(query, dsg) ;
        if ( dsg == dsg2 )
            return ds ;
        return DatasetFactory.create(dsg2) ;
    }

    /** Given a DatasetGraph and a query, form a DatasetGraph that 
     * is the dynamic dataset from the collection of graphs from the dataset
     * that go to make up the default graph (union) and named graphs.  
     */
    public static DatasetGraph dynamicDataset(Collection<Node> defaultGraphs, Collection<Node> namedGraphs, DatasetGraph dsg)
    {
        Graph dft = new GraphUnionRead(dsg, defaultGraphs) ;
        DatasetGraph dsg2 = new DatasetGraphMap(dft) ;
        for ( Node gn : namedGraphs )
            dsg2.addGraph(gn, dsg.getGraph(gn)) ;
        
        return dsg2 ;
    }
    
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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