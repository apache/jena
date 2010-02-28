/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core.assembler;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.DatasetFactory;

public class DataSourceAssembler extends AssemblerBase implements Assembler
{
    public static Resource getType() { return DatasetAssemblerVocab.tDataset ; }
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        DataSource ds = DatasetFactory.create() ;

        // -------- Default graph
        // Can use ja:graph or ja:defaultGraph
        Resource dftGraph = GraphUtils.getResourceValue(root, DatasetAssemblerVocab.pDefaultGraph) ;
        if ( dftGraph == null )
            dftGraph = GraphUtils.getResourceValue(root, DatasetAssemblerVocab.pGraph) ;
        
        Model dftModel = null ;
        if ( dftGraph != null )
            dftModel = a.openModel(dftGraph) ;
        else
            // Assembler description did not define one - make a dummy.
            dftModel = GraphFactory.makePlainModel() ;

        ds.setDefaultModel(dftModel) ;

        // -------- Named graphs
        List<RDFNode> nodes = GraphUtils.multiValue(root, DatasetAssemblerVocab.pNamedGraph) ; 
        
        for ( Iterator<RDFNode> iter= nodes.iterator() ; iter.hasNext() ; )
        {
            RDFNode n = iter.next();
            if ( ! ( n instanceof Resource ) )
                throw new DatasetAssemblerException(root, "Not a resource: "+FmtUtils.stringForRDFNode(n)) ;
            Resource r = (Resource)n ;

            String gName = GraphUtils.getAsStringValue(r, DatasetAssemblerVocab.pGraphName) ;
            Resource g = GraphUtils.getResourceValue(r, DatasetAssemblerVocab.pGraph) ;
            if ( g == null )
            {
                g = GraphUtils.getResourceValue(r, DatasetAssemblerVocab.pGraphAlt) ;
                if ( g != null )
                    ALog.warn(this, "Use of old vocabulary: use :graph not :graphData") ;
                else
                    throw new DatasetAssemblerException(root, "no graph for: "+gName) ;
            }
            
            Model m = a.openModel(g) ;
            ds.addNamedModel(gName, m) ;
        }
        
        return ds ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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