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

package com.hp.hpl.jena.sparql.core.assembler ;

import java.util.List ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

public class DatasetAssembler extends AssemblerBase implements Assembler {
    public static Resource getType() {
        return DatasetAssemblerVocab.tDataset ;
    }

    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        Dataset ds = createDataset(a, root, mode) ;
        return ds ;
    }

    public Dataset createDataset(Assembler a, Resource root, Mode mode) {
        // Expanding version.
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        AssemblerUtils.setContext(root, dsg.getContext()) ;

        Dataset ds = DatasetFactory.create(dsg) ;

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

        for ( RDFNode n : nodes )
        {
            if ( !( n instanceof Resource ) )
            {
                throw new DatasetAssemblerException( root, "Not a resource: " + FmtUtils.stringForRDFNode( n ) );
            }
            Resource r = (Resource) n;

            String gName = GraphUtils.getAsStringValue( r, DatasetAssemblerVocab.pGraphName );
            Resource g = GraphUtils.getResourceValue( r, DatasetAssemblerVocab.pGraph );
            if ( g == null )
            {
                g = GraphUtils.getResourceValue( r, DatasetAssemblerVocab.pGraphAlt );
                if ( g != null )
                {
                    Log.warn( this, "Use of old vocabulary: use :graph not :graphData" );
                }
                else
                {
                    throw new DatasetAssemblerException( root, "no graph for: " + gName );
                }
            }

            Model m = a.openModel( g );
            ds.addNamedModel( gName, m );
        }

        AssemblerUtils.setContext(root, ds.getContext()) ;
        return ds ;
    }
}
