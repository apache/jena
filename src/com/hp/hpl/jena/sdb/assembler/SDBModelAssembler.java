/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.assembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sparql.util.GraphUtils;

import com.hp.hpl.jena.query.Dataset;

import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;

public class SDBModelAssembler extends AssemblerBase implements Assembler
{
    DatasetStoreAssembler datasetAssem = new DatasetStoreAssembler() ;
    
    @Override
    public Model open(Assembler a, Resource root, Mode mode)
    {
        // Make a model.
        // [] rdf:type sdb:Graph ;
        //    sdb:dataset <dataset> ;
        //    sdb:graphName <someURI> .
        
        // A model (graph) is a (dataset, name) pair where the name can be absent
        // meaning the default graph of the dataset.
        
        Resource dataset = GraphUtils.getResourceValue(root, AssemblerVocab.pDataset) ;
        if ( dataset == null )
            throw new MissingException(root, "No dataset for model or graph") ;
        
        // Attempt to find a graph name - may be absent.
        Resource x = GraphUtils.getResourceValue(root, AssemblerVocab.pNamedGraph) ;
        if ( x != null && ! x.isURIResource() )
            throw new BadDescriptionException(root, "Graph name not a URI: "+x) ;

        // Is there a better way? 
        
        Dataset ds = (Dataset)datasetAssem.open(a, dataset, mode) ;
        DatasetStoreGraph dsg = (DatasetStoreGraph)ds.asDatasetGraph() ;
        
        // No name - default model.
        Graph g = null ;
        if ( x == null )
            g = dsg.getDefaultGraph() ;
        else
            g = dsg.getGraph(x.asNode()) ;
        
        // Named graph
        return ModelFactory.createModelForGraph(g) ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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